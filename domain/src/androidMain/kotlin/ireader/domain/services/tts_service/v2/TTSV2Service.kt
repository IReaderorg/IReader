package ireader.domain.services.tts_service.v2

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.media.app.NotificationCompat.MediaStyle
import coil3.ImageLoader
import coil3.asDrawable
import coil3.request.ImageRequest
import coil3.request.allowHardware
import ireader.core.log.Log
import ireader.domain.notification.NotificationsIds
import ireader.i18n.R
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
        private const val TAG = "TTSV2Service"
        private const val NOTIFICATION_ID = 2001
        private const val CHANNEL_ID = "tts_v2_playback"
        private const val CHANNEL_NAME = "TTS V2 Playback"
        
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
                    Log.warn { "$TAG: ACTION_PLAY_PAUSE" }
                    val state = controller.state.value
                    if (state.isPlaying) {
                        controller.dispatch(TTSCommand.Pause)
                    } else {
                        controller.dispatch(TTSCommand.Play)
                    }
                }
                ACTION_STOP -> {
                    Log.warn { "$TAG: ACTION_STOP" }
                    controller.dispatch(TTSCommand.Stop)
                    stopSelf()
                }
                ACTION_NEXT -> {
                    Log.warn { "$TAG: ACTION_NEXT" }
                    val state = controller.state.value
                    if (state.chunkModeEnabled) {
                        controller.dispatch(TTSCommand.NextChunk)
                    } else {
                        controller.dispatch(TTSCommand.NextParagraph)
                    }
                }
                ACTION_PREVIOUS -> {
                    Log.warn { "$TAG: ACTION_PREVIOUS" }
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
    
    override fun onCreate() {
        super.onCreate()
        Log.warn { "$TAG: onCreate()" }
        
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        createNotificationChannel()
        setupMediaSession()
        registerActionReceiver()
        
        // Initialize controller
        controller.dispatch(TTSCommand.Initialize)
        
        // Observe state changes
        observeState()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.warn { "$TAG: onStartCommand()" }
        
        // Start as foreground service immediately
        startForeground(NOTIFICATION_ID, createNotification())
        
        // Load chapter if provided
        intent?.let {
            val bookId = it.getLongExtra(EXTRA_BOOK_ID, -1)
            val chapterId = it.getLongExtra(EXTRA_CHAPTER_ID, -1)
            val startParagraph = it.getIntExtra(EXTRA_START_PARAGRAPH, 0)
            
            if (bookId > 0 && chapterId > 0) {
                Log.warn { "$TAG: Loading chapter bookId=$bookId, chapterId=$chapterId" }
                controller.dispatch(TTSCommand.LoadChapter(bookId, chapterId, startParagraph))
            }
        }
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder {
        Log.warn { "$TAG: onBind()" }
        return binder
    }
    
    override fun onDestroy() {
        Log.warn { "$TAG: onDestroy()" }
        
        stateObserverJob?.cancel()
        serviceScope.cancel()
        
        unregisterActionReceiver()
        abandonAudioFocus()
        
        mediaSession.release()
        controller.dispatch(TTSCommand.Cleanup)
        
        super.onDestroy()
    }
    
    // ========== Audio Focus ==========
    
    override fun onAudioFocusChange(focusChange: Int) {
        Log.warn { "$TAG: onAudioFocusChange($focusChange)" }
        
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
        
        mediaSession = MediaSessionCompat(this, "TTSV2Service").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    Log.warn { "$TAG: MediaSession onPlay()" }
                    if (requestAudioFocus()) {
                        ttsController.dispatch(TTSCommand.Play)
                    }
                }
                
                override fun onPause() {
                    Log.warn { "$TAG: MediaSession onPause()" }
                    ttsController.dispatch(TTSCommand.Pause)
                }
                
                override fun onStop() {
                    Log.warn { "$TAG: MediaSession onStop()" }
                    ttsController.dispatch(TTSCommand.Stop)
                    stopSelf()
                }
                
                override fun onSkipToNext() {
                    Log.warn { "$TAG: MediaSession onSkipToNext()" }
                    val currentState = ttsController.state.value
                    if (currentState.chunkModeEnabled) {
                        ttsController.dispatch(TTSCommand.NextChunk)
                    } else {
                        ttsController.dispatch(TTSCommand.NextParagraph)
                    }
                }
                
                override fun onSkipToPrevious() {
                    Log.warn { "$TAG: MediaSession onSkipToPrevious()" }
                    val currentState = ttsController.state.value
                    if (currentState.chunkModeEnabled) {
                        ttsController.dispatch(TTSCommand.PreviousChunk)
                    } else {
                        ttsController.dispatch(TTSCommand.PreviousParagraph)
                    }
                }
                
                override fun onSetPlaybackSpeed(speed: Float) {
                    Log.warn { "$TAG: MediaSession onSetPlaybackSpeed($speed)" }
                    ttsController.dispatch(TTSCommand.SetSpeed(speed))
                }
            })
            
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
                PlaybackStateCompat.ACTION_STOP or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_SET_PLAYBACK_SPEED
            )
        
        mediaSession.setPlaybackState(stateBuilder.build())
        
        // Update metadata
        val metadata = android.support.v4.media.MediaMetadataCompat.Builder()
            .putString(
                android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE,
                state.chapter?.name ?: "TTS Playback"
            )
            .putString(
                android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ARTIST,
                state.book?.title ?: ""
            )
            .putString(
                android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM,
                when (state.engineType) {
                    EngineType.NATIVE -> "Native TTS"
                    EngineType.GRADIO -> "Gradio TTS"
                }
            )
            .build()
        
        mediaSession.setMetadata(metadata)
    }
    
    // ========== Notification ==========
    
    // Cached book cover bitmap
    private var cachedCoverBitmap: Bitmap? = null
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
            R.drawable.ic_baseline_skip_previous,
            "Previous",
            createActionPendingIntent(ACTION_PREVIOUS)
        )
        
        val playPauseAction = if (state.isPlaying) {
            NotificationCompat.Action(
                R.drawable.ic_baseline_pause,
                "Pause",
                createActionPendingIntent(ACTION_PLAY_PAUSE)
            )
        } else {
            NotificationCompat.Action(
                R.drawable.ic_baseline_play_arrow,
                "Play",
                createActionPendingIntent(ACTION_PLAY_PAUSE)
            )
        }
        
        val nextAction = NotificationCompat.Action(
            R.drawable.ic_baseline_skip_next,
            "Next",
            createActionPendingIntent(ACTION_NEXT)
        )
        
        val closeAction = NotificationCompat.Action(
            R.drawable.baseline_close_24,
            "Close",
            createActionPendingIntent(ACTION_STOP)
        )
        
        // Content text
        val chapterTitle = state.chapter?.name ?: "Ready"
        val progressText = if (state.isLoading) {
            "Loading..."
        } else if (state.totalParagraphs > 0) {
            "Paragraph ${state.currentParagraphIndex + 1} of ${state.totalParagraphs}"
        } else {
            "Ready"
        }
        
        val builder = NotificationCompat.Builder(this, NotificationsIds.CHANNEL_TTS)
            .setSmallIcon(R.drawable.ic_infinity)
            .setContentTitle(progressText)
            .setContentText(chapterTitle)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(state.isPlaying)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColorized(true)
            .setAutoCancel(false)
            // Add actions in order
            .addAction(prevAction)      // index 0
            .addAction(playPauseAction) // index 1
            .addAction(nextAction)      // index 2
            .addAction(closeAction)     // index 3
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
    
    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, createNotification())
    }
    
    /**
     * Load book cover image for notification
     */
    private fun loadBookCover(coverUrl: String?) {
        if (coverUrl == null || coverUrl == cachedCoverUrl) return
        
        cachedCoverUrl = coverUrl
        
        serviceScope.launch(Dispatchers.IO) {
            try {
                val request = ImageRequest.Builder(this@TTSV2Service)
                    .data(coverUrl)
                    .size(coil3.size.Size(512, 512))
                    .allowHardware(false) // Required for notification
                    .build()
                
                val result = ImageLoader(this@TTSV2Service).execute(request)
                val bitmap = result.image?.asDrawable(resources)?.toBitmap()
                
                if (bitmap != null) {
                    cachedCoverBitmap = bitmap
                    // Update notification with new cover
                    launch(Dispatchers.Main) {
                        updateNotification()
                    }
                    Log.warn { "$TAG: Loaded book cover for notification" }
                }
            } catch (e: Exception) {
                Log.error { "$TAG: Failed to load book cover: ${e.message}" }
            }
        }
    }
    
    // ========== State Observation ==========
    
    private fun observeState() {
        stateObserverJob?.cancel()
        stateObserverJob = controller.state
            .onEach { state ->
                Log.warn { "$TAG: State changed - playback=${state.playbackState}, paragraph=${state.currentParagraphIndex}" }
                
                // Load book cover if changed
                state.book?.cover?.let { coverUrl ->
                    loadBookCover(coverUrl)
                }
                
                updateMediaSessionState(state)
                updateNotification()
                
                // Request audio focus when starting playback
                if (state.isPlaying) {
                    requestAudioFocus()
                }
                
                // Stop service if playback stopped
                if (state.playbackState == PlaybackState.STOPPED && !state.hasContent) {
                    Log.warn { "$TAG: Playback stopped, stopping service" }
                    stopSelf()
                }
            }
            .launchIn(serviceScope)
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
            Log.warn { "$TAG: Failed to unregister receiver: ${e.message}" }
        }
    }
}
