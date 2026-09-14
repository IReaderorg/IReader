package ireader.domain.services.tts_service.v2

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.core.content.IntentCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.asDrawable
import coil3.request.ImageRequest
import coil3.request.allowHardware
import ireader.core.log.Log
import ireader.domain.models.BookCover
import ireader.domain.models.entities.Book
import ireader.domain.notification.NotificationsIds
import ireader.domain.utils.DrawableResources
import ireader.domain.utils.extensions.launchMainActivityIntent
import ireader.i18n.Args
import ireader.i18n.SHORTCUTS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * TTS V2 Background Service for Android
 * 
 * This service:
 * - Runs TTS playback in the background
 * - Shows media-style notification with controls
 * - Handles audio focus
 * - Integrates with MediaSession for lock screen controls
 * 
 * Usage:
 * 1. Start service with startService(intent)
 * 2. Bind to get TTSController access
 * 3. Use controller to manage playback
 */
class TTSV2Service : Service(), AudioManager.OnAudioFocusChangeListener {
    
    companion object {
        private const val NOTIFICATION_ID = 2001
        
        // Actions
        const val ACTION_PLAY_PAUSE = "ireader.tts.v2.PLAY_PAUSE"
        const val ACTION_STOP = "ireader.tts.v2.STOP"
        const val ACTION_NEXT = "ireader.tts.v2.NEXT"
        const val ACTION_PREVIOUS = "ireader.tts.v2.PREVIOUS"
        
        // Intent extras
        const val EXTRA_BOOK_ID = "book_id"
        const val EXTRA_CHAPTER_ID = "chapter_id"
        const val EXTRA_START_PARAGRAPH = "start_paragraph"
        
        fun createIntent(context: Context, bookId: Long, chapterId: Long, startParagraph: Int = 0): Intent {
            return Intent(context, TTSV2Service::class.java).apply {
                putExtra(EXTRA_BOOK_ID, bookId)
                putExtra(EXTRA_CHAPTER_ID, chapterId)
                putExtra(EXTRA_START_PARAGRAPH, startParagraph)
            }
        }
    }
    
    // Injected dependencies
    private val controller: TTSController by inject()
    
    // Service components
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var audioManager: AudioManager
    private var focusRequest: AudioFocusRequest? = null
    private var isNoisyReceiverRegistered = false
    private var silentPlayer: MediaPlayer? = null
    private var silentAudioTrack: AudioTrack? = null
    private var audioDeviceCallback: AudioDeviceCallback? = null
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var stateObserverJob: Job? = null
    
    // Binder for local binding
    private val binder = LocalBinder()
    
    inner class LocalBinder : Binder() {
        fun getController(): TTSController = controller
    }
    
    // Broadcast receiver for notification actions
    private val actionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_PLAY_PAUSE -> {
                    val state = controller.state.value
                    if (state.isPlaying) {
                        controller.dispatch(TTSCommand.Pause)
                    } else if (state.isPaused) {
                        controller.dispatch(TTSCommand.Resume)
                    } else {
                        controller.dispatch(TTSCommand.Play)
                    }
                }
                ACTION_STOP -> {
                    // Stop and release engine but keep content
                    controller.dispatch(TTSCommand.StopAndRelease)
                    stopSelf()
                }
                ACTION_NEXT -> {
                    val state = controller.state.value
                    if (state.chunkModeEnabled) {
                        controller.dispatch(TTSCommand.NextChunk)
                    } else {
                        controller.dispatch(TTSCommand.NextParagraph)
                    }
                }
                ACTION_PREVIOUS -> {
                    val state = controller.state.value
                    if (state.chunkModeEnabled) {
                        controller.dispatch(TTSCommand.PreviousChunk)
                    } else {
                        controller.dispatch(TTSCommand.PreviousParagraph)
                    }
                }
            }
        }
    }
    
    // Broadcast receiver for headphone disconnection (audio becoming noisy, plug state, bluetooth disconnect)
    private val noisyAudioReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            when (action) {
                AudioManager.ACTION_AUDIO_BECOMING_NOISY -> {
                    Log.info { "TTSV2Service: ACTION_AUDIO_BECOMING_NOISY received, pausing TTS" }
                    controller.dispatch(TTSCommand.Pause)
                }
                Intent.ACTION_HEADSET_PLUG -> {
                    // isInitialStickyBroadcast() is true when the receiver is first registered; ignore it
                    if (!isInitialStickyBroadcast) {
                        val state = intent.getIntExtra("state", -1)
                        if (state == 0) { // 0 = unplugged
                            Log.info { "TTSV2Service: Headset unplugged (ACTION_HEADSET_PLUG), pausing TTS" }
                            controller.dispatch(TTSCommand.Pause)
                        }
                    }
                }
                BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED,
                BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1)
                    if (state == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.info { "TTSV2Service: Bluetooth profile disconnected, pausing TTS" }
                        controller.dispatch(TTSCommand.Pause)
                    }
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    Log.info { "TTSV2Service: Bluetooth ACL disconnected, pausing TTS" }
                    controller.dispatch(TTSCommand.Pause)
                }
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        createNotificationChannel()
        setupMediaSession()
        registerActionReceiver()
        registerNoisyReceiver()
        registerAudioDeviceCallback()
        
        // Start silent audio playback to claim the media audio route.
        // Android's TTS engine plays audio through its own internal AudioTrack,
        // so the system doesn't see our app as "currently playing media".
        // By playing silent audio, MediaSessionManager recognizes our session
        // as active and routes headset button presses to us.
        startSilentPlayback()
        
        // Initialize controller
        controller.dispatch(TTSCommand.Initialize)
        
        // Observe state changes
        observeState()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start as foreground service immediately
        startForeground(NOTIFICATION_ID, createNotification())
        
        // Handle media button intents from MediaButtonReceiver
        // This is how Bluetooth headphone buttons reach our MediaSession callbacks
        if (intent != null && Intent.ACTION_MEDIA_BUTTON == intent.action) {
            try {
                val handled = MediaButtonReceiver.handleIntent(mediaSession, intent)
                if (handled == null) {
                    val keyEvent = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
                    if (keyEvent != null) {
                        mediaSession.controller.dispatchMediaButtonEvent(keyEvent)
                    }
                }
            } catch (e: Exception) {
                Log.error { "TTSV2Service: Error handling media button: ${e.message}" }
            }
            return START_STICKY
        }
        
        // Load chapter if provided and not already loaded
        intent?.let {
            val bookId = it.getLongExtra(EXTRA_BOOK_ID, -1)
            val chapterId = it.getLongExtra(EXTRA_CHAPTER_ID, -1)
            val startParagraph = it.getIntExtra(EXTRA_START_PARAGRAPH, 0)
            
            if (bookId > 0 && chapterId > 0) {
                // Only load if chapter is different from current
                val currentState = controller.state.value
                val isAlreadyLoaded = currentState.chapter?.id == chapterId && currentState.paragraphs.isNotEmpty()
                
                if (!isAlreadyLoaded) {
                    controller.dispatch(TTSCommand.LoadChapter(bookId, chapterId, startParagraph))
                }
            }
        }
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    
    override fun onDestroy() {
        stateObserverJob?.cancel()
        serviceScope.cancel()
        
        stopSilentPlayback()
        unregisterActionReceiver()
        unregisterNoisyReceiver()
        unregisterAudioDeviceCallback()
        abandonAudioFocus()
        
        mediaSession.release()
        // Stop and release engine but keep content for when user returns to TTS screen
        controller.dispatch(TTSCommand.StopAndRelease)
        
        super.onDestroy()
    }
    
    // ========== Audio Focus ==========
    
    override fun onAudioFocusChange(focusChange: Int) {

        
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                controller.dispatch(TTSCommand.Resume)
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                controller.dispatch(TTSCommand.Pause)
                abandonAudioFocus()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                controller.dispatch(TTSCommand.Pause)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Could lower volume, but for TTS we pause
                controller.dispatch(TTSCommand.Pause)
            }
        }
    }
    
    private fun requestAudioFocus(): Boolean {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setOnAudioFocusChangeListener(this)
                .build()
            audioManager.requestAudioFocus(focusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }
    
    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(this)
        }
    }

    
    // ========== Media Session ==========
    
    private fun setupMediaSession() {
        // Capture controller reference to avoid naming conflict with internal coroutines dispatch
        val ttsController = controller
        
        // Use 4-argument constructor with explicit ComponentName pointing to MediaButtonReceiver.
        // This is the AndroidX-documented way to properly register media button routing.
        // Without this, MIUI/OEM ROMs may not route Bluetooth button events properly.
        val mediaButtonReceiverComponent = android.content.ComponentName(
            this, androidx.media.session.MediaButtonReceiver::class.java
        )
        
        mediaSession = MediaSessionCompat(
            this, "TTSV2Service", mediaButtonReceiverComponent, null
        ).apply {
            // Set flags so the system knows this session handles media buttons and transport controls
            @Suppress("DEPRECATION")
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
            
            // Set session activity - some OEMs (MIUI, Samsung) require this for
            // the media session to be properly discovered and prioritized
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                val sessionActivityPi = PendingIntent.getActivity(
                    this@TTSV2Service, 0, launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setSessionActivity(sessionActivityPi)
            }
            
            val pendingMediaButtonIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(
                this@TTSV2Service,
                PlaybackStateCompat.ACTION_PLAY_PAUSE
            )
            setMediaButtonReceiver(pendingMediaButtonIntent)
            
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
                    val keyEvent = if (mediaButtonEvent != null) {
                        IntentCompat.getParcelableExtra(mediaButtonEvent, Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
                    } else null

                    if (keyEvent == null) {
                        return super.onMediaButtonEvent(mediaButtonEvent)
                    }
                    
                    // Only handle ACTION_DOWN (ignore ACTION_UP to avoid double-firing)
                    if (keyEvent.action != KeyEvent.ACTION_DOWN) {
                        return true
                    }
                    
                    when (keyEvent.keyCode) {
                        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                        KeyEvent.KEYCODE_HEADSETHOOK -> {
                            // Single-click: toggle play/pause
                            val currentState = ttsController.state.value
                            if (currentState.isPlaying) {
                                onPause()
                            } else {
                                onPlay()
                            }
                            return true
                        }
                        KeyEvent.KEYCODE_MEDIA_PLAY -> {
                            onPlay()
                            return true
                        }
                        KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                            onPause()
                            return true
                        }
                        KeyEvent.KEYCODE_MEDIA_NEXT -> {
                            onSkipToNext()
                            return true
                        }
                        KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                            onSkipToPrevious()
                            return true
                        }
                        KeyEvent.KEYCODE_MEDIA_STOP -> {
                            onStop()
                            return true
                        }
                    }
                    
                    return super.onMediaButtonEvent(mediaButtonEvent)
                }
                
                override fun onPlay() {
                    if (requestAudioFocus()) {
                        val state = ttsController.state.value
                        if (state.isPaused) {
                            ttsController.dispatch(TTSCommand.Resume)
                        } else {
                            ttsController.dispatch(TTSCommand.Play)
                        }
                    }
                }
                
                override fun onPause() {
                    ttsController.dispatch(TTSCommand.Pause)
                }
                
                override fun onStop() {

                    // Stop and release engine but keep content
                    ttsController.dispatch(TTSCommand.StopAndRelease)
                    stopSelf()
                }
                
                override fun onSkipToNext() {

                    val currentState = ttsController.state.value
                    if (currentState.chunkModeEnabled) {
                        ttsController.dispatch(TTSCommand.NextChunk)
                    } else {
                        ttsController.dispatch(TTSCommand.NextParagraph)
                    }
                }
                
                override fun onSkipToPrevious() {

                    val currentState = ttsController.state.value
                    if (currentState.chunkModeEnabled) {
                        ttsController.dispatch(TTSCommand.PreviousChunk)
                    } else {
                        ttsController.dispatch(TTSCommand.PreviousParagraph)
                    }
                }
                
                override fun onSetPlaybackSpeed(speed: Float) {

                    ttsController.dispatch(TTSCommand.SetSpeed(speed))
                }
            })
            
            // Set active immediately so the session can receive media button events;
            // playback state will be updated via updateMediaSessionState()
            isActive = true
        }
    }
    
    private fun updateMediaSessionState(state: TTSState) {
        val playbackState = when {
            state.isPlaying -> PlaybackStateCompat.STATE_PLAYING
            state.isPaused -> PlaybackStateCompat.STATE_PAUSED
            state.isLoading -> PlaybackStateCompat.STATE_BUFFERING
            else -> PlaybackStateCompat.STATE_STOPPED
        }
        
        val stateBuilder = PlaybackStateCompat.Builder()
            .setState(playbackState, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, state.speed)
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_STOP or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_SET_PLAYBACK_SPEED
            )
        
        mediaSession.setPlaybackState(stateBuilder.build())
        
        // Keep MediaSession always active while service is running.
        // isActive controls whether the session can receive media button events;
        // the playback STATE (playing/paused/stopped) determines the actual behavior.
        // If we conditionally deactivate, Bluetooth buttons stop working until playback resumes.
        mediaSession.isActive = true
        
        // Update metadata - show chapter name and paragraph progress
        val progressText = if (state.totalParagraphs > 0) {
            "${state.currentParagraphIndex + 1}/${state.totalParagraphs}"
        } else {
            ""
        }
        
        val metadataBuilder = android.support.v4.media.MediaMetadataCompat.Builder()
            .putString(
                android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE,
                state.chapter?.name ?: "TTS Playback"
            )
            .putString(
                android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ARTIST,
                progressText
            )
            .putString(
                android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM,
                state.book?.title ?: ""
            )
        
        cachedCoverBitmap?.let { bitmap ->
            metadataBuilder.putBitmap(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
            metadataBuilder.putBitmap(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ART, bitmap)
        }
        
        mediaSession.setMetadata(metadataBuilder.build())
    }
    
    // ========== Notification ==========
    
    // Cached book cover bitmap
    private var cachedCoverBitmap: Bitmap? = null
    private var cachedCoverBookId: Long? = null
    private var cachedCoverUrl: String? = null
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NotificationsIds.CHANNEL_TTS,
                "TTS Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Text-to-speech playback controls"
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val state = controller.state.value
        
        // Build actions - order matters for compact view
        // 0: Previous, 1: Play/Pause, 2: Next, 3: Close
        val prevAction = NotificationCompat.Action(
            DrawableResources.ic_baseline_skip_previous,
            "Previous",
            createActionPendingIntent(ACTION_PREVIOUS)
        )
        
        val playPauseAction = if (state.isPlaying) {
            NotificationCompat.Action(
                DrawableResources.ic_baseline_pause,
                "Pause",
                createActionPendingIntent(ACTION_PLAY_PAUSE)
            )
        } else {
            NotificationCompat.Action(
                DrawableResources.ic_baseline_play_arrow,
                "Play",
                createActionPendingIntent(ACTION_PLAY_PAUSE)
            )
        }
        
        val nextAction = NotificationCompat.Action(
            DrawableResources.ic_baseline_skip_next,
            "Next",
            createActionPendingIntent(ACTION_NEXT)
        )
        
        val closeAction = NotificationCompat.Action(
            DrawableResources.baseline_close_24,
            "Close",
            createActionPendingIntent(ACTION_STOP)
        )
        
        val openAction = NotificationCompat.Action(
            DrawableResources.ic_baseline_open_in_new_24,
            "Open",
            createContentIntent()
        )
        
        // Content text - chapter name as title, paragraph progress as subtitle
        val chapterTitle = state.chapter?.name ?: "TTS Playback"
        val progressText = if (state.totalParagraphs > 0) {
            "${state.currentParagraphIndex + 1}/${state.totalParagraphs}"
        } else {
            ""
        }
        
        val builder = NotificationCompat.Builder(this, NotificationsIds.CHANNEL_TTS)
            .setSmallIcon(DrawableResources.ic_infinity)
            .setContentTitle(chapterTitle)
            .setContentText(progressText)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(state.isPlaying)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColorized(true)
            .setAutoCancel(false)
            // Content intent - opens TTS screen when notification is tapped
            .setContentIntent(createContentIntent())
            // Add actions in order
            .addAction(prevAction)      // index 0
            .addAction(playPauseAction) // index 1
            .addAction(nextAction)      // index 2
            .addAction(openAction)      // index 3 - Open TTS screen
            .addAction(closeAction)     // index 4
            // MediaStyle
            .setStyle(
                MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2) // prev, play/pause, next
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(createActionPendingIntent(ACTION_STOP))
            )
            .setDeleteIntent(createActionPendingIntent(ACTION_STOP))
        
        // Add progress bar
        if (!state.isLoading && state.totalParagraphs > 0) {
            builder.setProgress(
                state.totalParagraphs,
                state.currentParagraphIndex,
                false
            )
        }
        
        // Add book cover if available
        cachedCoverBitmap?.let { bitmap ->
            builder.setLargeIcon(bitmap)
        }
        
        return builder.build()
    }
    
    private fun createActionPendingIntent(action: String): PendingIntent {
        val intent = Intent(action).apply {
            setPackage(packageName)
        }
        return PendingIntent.getBroadcast(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    /**
     * Create a pending intent that opens the TTS V2 screen when notification is tapped
     */
    private fun createContentIntent(): PendingIntent {
        val state = controller.state.value
        val bookId = state.book?.id ?: 0L
        val chapterId = state.chapter?.id ?: 0L
        val sourceId = state.book?.sourceId ?: 0L
        val currentParagraph = state.currentParagraphIndex
        
        val intent = launchMainActivityIntent(this)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            .apply {
                action = SHORTCUTS.SHORTCUT_TTS_V2
                putExtra(Args.ARG_BOOK_ID, bookId)
                putExtra(Args.ARG_CHAPTER_ID, chapterId)
                putExtra(Args.ARG_SOURCE_ID, sourceId)
                putExtra(Args.ARG_READING_PARAGRAPH, currentParagraph.toLong())
            }
        
        return PendingIntent.getActivity(
            this,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, createNotification())
    }
    
    /**
     * Load book cover image for notification and MediaSession
     */
    private fun loadBookCover(book: Book?) {
        if (book == null) return
        val coverUrl = book.cover
        if (book.id == cachedCoverBookId && coverUrl == cachedCoverUrl && cachedCoverBitmap != null) return
        
        if (book.id != cachedCoverBookId) {
            cachedCoverBitmap = null
            cachedCoverBookId = book.id
            cachedCoverUrl = null
        }
        cachedCoverUrl = coverUrl
        
        serviceScope.launch(Dispatchers.IO) {
            try {
                val bookCover = BookCover.from(book)
                val request = ImageRequest.Builder(this@TTSV2Service)
                    .data(bookCover)
                    .size(coil3.size.Size(512, 512))
                    .allowHardware(false) // Required for notification and MediaSession
                    .build()
                
                val result = SingletonImageLoader.get(this@TTSV2Service).execute(request)
                val bitmap = result.image?.asDrawable(resources)?.toBitmap()
                
                if (bitmap != null) {
                    cachedCoverBitmap = bitmap
                    // Update notification and media session with new cover
                    launch(Dispatchers.Main) {
                        updateMediaSessionState(controller.state.value)
                        updateNotification()
                    }
                }
            } catch (e: Exception) {
                Log.error { "TTSV2Service: Failed to load book cover: ${e.message}" }
            }
        }
    }
    
    // ========== State Observation ==========
    
    private fun observeState() {
        stateObserverJob?.cancel()
        stateObserverJob = controller.state
            .onEach { state ->

                
                // Load book cover if changed
                state.book?.let { book ->
                    loadBookCover(book)
                }
                
                updateMediaSessionState(state)
                updateNotification()
                
                // Request audio focus when starting playback and ensure silent player is active
                if (state.isPlaying) {
                    requestAudioFocus()
                    if (silentPlayer == null && silentAudioTrack == null) {
                        startSilentPlayback()
                    }
                }
                
                // Stop service if playback stopped
                if (state.playbackState == PlaybackState.STOPPED && !state.hasContent) {
                    stopSelf()
                }
            }
            .launchIn(serviceScope)
    }
    
    // ========== Silent Audio Playback (for media button routing) ==========
    
    /**
     * Start playing a silent audio track on loop at zero volume.
     * This makes Android's MediaSessionManager recognize our app as the
     * "currently playing media" app, so headset button presses (KEYCODE_HEADSETHOOK)
     * are routed to our MediaSession instead of Google Assistant.
     */
    private fun startSilentPlayback() {
        try {
            silentPlayer?.release()
            silentPlayer = null
            
            // Try loading silence from resources (res/raw/silence.wav)
            val resId = try {
                resources.getIdentifier("silence", "raw", packageName).takeIf { it != 0 }
                    ?: resources.getIdentifier("silence", "raw", "ireader.i18n").takeIf { it != 0 }
                    ?: ireader.i18n.R.raw.silence
            } catch (e: Throwable) {
                0
            }
            
            if (resId != 0) {
                silentPlayer = MediaPlayer.create(this, resId)?.apply {
                    isLooping = true
                    setVolume(0f, 0f)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    start()
                }
                if (silentPlayer != null) {
                    Log.info { "TTSV2Service: Silent MediaPlayer started with resource $resId" }
                }
            }
        } catch (e: Exception) {
            Log.warn { "TTSV2Service: MediaPlayer silence creation failed: ${e.message}, falling back to AudioTrack" }
            silentPlayer = null
        }
        
        // Fallback: If MediaPlayer failed or resource is missing, use an in-memory AudioTrack
        // which has zero file dependencies and directly registers active media playback with Android's AudioService.
        if (silentPlayer == null && silentAudioTrack == null) {
            startSilentAudioTrack()
        }
    }
    
    private fun startSilentAudioTrack() {
        try {
            silentAudioTrack?.release()
            val sampleRate = 44100
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(1024)
            
            val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                    AudioTrack.MODE_STATIC
                )
            }
            
            val silence = ByteArray(bufferSize)
            track.write(silence, 0, silence.size)
            track.setLoopPoints(0, bufferSize / 2, -1)
            track.setVolume(0f)
            track.play()
            silentAudioTrack = track
            Log.info { "TTSV2Service: Silent AudioTrack started successfully" }
        } catch (e: Exception) {
            Log.error { "TTSV2Service: Failed to start silent AudioTrack: ${e.message}" }
            silentAudioTrack = null
        }
    }
    
    private fun stopSilentPlayback() {
        try {
            silentPlayer?.stop()
            silentPlayer?.release()
        } catch (e: Exception) {
            // Ignore
        }
        silentPlayer = null
        
        try {
            silentAudioTrack?.stop()
            silentAudioTrack?.release()
        } catch (e: Exception) {
            // Ignore
        }
        silentAudioTrack = null
    }
    
    // ========== Broadcast Receiver ==========
    
    private fun registerActionReceiver() {
        val filter = IntentFilter().apply {
            addAction(ACTION_PLAY_PAUSE)
            addAction(ACTION_STOP)
            addAction(ACTION_NEXT)
            addAction(ACTION_PREVIOUS)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(actionReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(actionReceiver, filter)
        }
    }
    
    private fun unregisterActionReceiver() {
        try {
            unregisterReceiver(actionReceiver)
        } catch (e: Exception) {
            // Ignore
        }
    }
    
    // ========== Headphone & Bluetooth Disconnection Handling ==========
    
    private fun registerAudioDeviceCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && audioDeviceCallback == null) {
            audioDeviceCallback = object : AudioDeviceCallback() {
                override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
                    val devices = removedDevices.orEmpty()
                    val isHeadphoneRemoved = devices.any { device ->
                        when (device.type) {
                            AudioDeviceInfo.TYPE_WIRED_HEADSET,
                            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                            AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                            AudioDeviceInfo.TYPE_USB_HEADSET,
                            AudioDeviceInfo.TYPE_USB_DEVICE -> true
                            else -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    device.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                                    device.type == AudioDeviceInfo.TYPE_BLE_SPEAKER ||
                                    device.type == AudioDeviceInfo.TYPE_BLE_BROADCAST
                                } else false
                            }
                        }
                    }
                    if (isHeadphoneRemoved) {
                        Log.info { "TTSV2Service: AudioDeviceCallback detected headphone/Bluetooth removal, pausing playback" }
                        controller.dispatch(TTSCommand.Pause)
                    }
                }
            }
            try {
                audioManager.registerAudioDeviceCallback(audioDeviceCallback, null)
            } catch (e: Exception) {
                Log.error { "TTSV2Service: Failed to register AudioDeviceCallback: ${e.message}" }
            }
        }
    }
    
    private fun unregisterAudioDeviceCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && audioDeviceCallback != null) {
            try {
                audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
            } catch (e: Exception) {
                // Ignore
            }
            audioDeviceCallback = null
        }
    }
    
    private fun registerNoisyReceiver() {
        if (!isNoisyReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
                addAction(Intent.ACTION_HEADSET_PLUG)
                addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    registerReceiver(noisyAudioReceiver, filter, Context.RECEIVER_EXPORTED)
                } else {
                    registerReceiver(noisyAudioReceiver, filter)
                }
                isNoisyReceiverRegistered = true
            } catch (e: Exception) {
                Log.error { "TTSV2Service: Failed to register noisy receiver: ${e.message}" }
            }
        }
    }
    
    private fun unregisterNoisyReceiver() {
        if (isNoisyReceiverRegistered) {
            try {
                unregisterReceiver(noisyAudioReceiver)
            } catch (e: Exception) {
                // Ignore
            }
            isNoisyReceiverRegistered = false
        }
    }
}
