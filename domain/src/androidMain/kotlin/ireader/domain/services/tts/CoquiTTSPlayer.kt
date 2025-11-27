//package ireader.domain.services.tts
//
//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import androidx.core.app.NotificationCompat
//import ireader.core.log.Log
//import ireader.domain.models.tts.AudioData
//import ireader.domain.preferences.prefs.AppPreferences
//import kotlinx.coroutines.*
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import java.util.concurrent.ConcurrentLinkedQueue
//
///**
// * Enhanced Coqui TTS Player with:
// * - Auto next chapter
// * - Preload next 3 paragraphs
// * - Android notifications
// * - Playback state management
// */
//class CoquiTTSPlayer(
//    private val context: Context,
//    private val aiTTSManager: AITTSManager,
//    private val appPreferences: AppPreferences
//) {
//    private object Constants {
//        const val NOTIFICATION_ID = 1001
//        const val CHANNEL_ID = "coqui_tts_playback"
//        const val PRELOAD_COUNT = 3
//    }
//
//    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
//    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//    // Playback state
//    private val _isPlaying = MutableStateFlow(false)
//    val isPlaying: StateFlow<Boolean> = _isPlaying
//
//    private val _currentParagraph = MutableStateFlow(0)
//    val currentParagraph: StateFlow<Int> = _currentParagraph
//
//    private val _currentChapter = MutableStateFlow<String?>(null)
//    val currentChapter: StateFlow<String?> = _currentChapter
//
//    // Content
//    private var paragraphs: List<String> = emptyList()
//    private var chapterTitle: String = ""
//    private var bookTitle: String = ""
//
//    // Preload cache
//    private val audioCache = ConcurrentLinkedQueue<Pair<Int, AudioData>>()
//    private var preloadJob: Job? = null
//
//    // Auto-next chapter callback
//    private var onChapterEnd: (suspend () -> Boolean)? = null
//
//    init {
//        createNotificationChannel()
//    }
//
//    /**
//     * Start reading from current paragraph
//     */
//    fun startReading(
//        paragraphs: List<String>,
//        startParagraph: Int = 0,
//        chapterTitle: String = "",
//        bookTitle: String = "",
//        onChapterEnd: (suspend () -> Boolean)? = null
//    ) {
//        this.paragraphs = paragraphs
//        this.chapterTitle = chapterTitle
//        this.bookTitle = bookTitle
//        this.onChapterEnd = onChapterEnd
//
//        _currentParagraph.value = startParagraph
//        _isPlaying.value = true
//
//        // Start preloading
//        startPreloading()
//
//        // Start reading
//        readParagraph(startParagraph)
//
//        // Show notification
//        showNotification()
//    }
//
//    /**
//     * Pause reading
//     */
//    fun pause() {
//        _isPlaying.value = false
//        aiTTSManager.stopPlayback()
//        preloadJob?.cancel()
//        updateNotification()
//    }
//
//    /**
//     * Resume reading
//     */
//    fun resume() {
//        _isPlaying.value = true
//        startPreloading()
//        readParagraph(_currentParagraph.value)
//        updateNotification()
//    }
//
//    /**
//     * Stop reading completely
//     */
//    fun stop() {
//        _isPlaying.value = false
//        aiTTSManager.stopPlayback()
//        preloadJob?.cancel()
//        audioCache.clear()
//        hideNotification()
//    }
//
//    /**
//     * Skip to next paragraph
//     */
//    fun nextParagraph() {
//        val next = _currentParagraph.value + 1
//        if (next < paragraphs.size) {
//            _currentParagraph.value = next
//            if (_isPlaying.value) {
//                readParagraph(next)
//            }
//        }
//    }
//
//    /**
//     * Skip to previous paragraph
//     */
//    fun previousParagraph() {
//        val prev = _currentParagraph.value - 1
//        if (prev >= 0) {
//            _currentParagraph.value = prev
//            if (_isPlaying.value) {
//                readParagraph(prev)
//            }
//        }
//    }
//
//    /**
//     * Read specific paragraph
//     */
//    private fun readParagraph(index: Int) {
//        scope.launch {
//            try {
//                if (index >= paragraphs.size) {
//                    handleChapterEnd()
//                    return@launch
//                }
//
//                val text = paragraphs.getOrNull(index) ?: return@launch
//                _currentParagraph.value = index
//
//                Log.info { "Reading paragraph $index/${paragraphs.size}" }
//
//                // Check cache first
//                val cached = audioCache.find { it.first == index }
//                if (cached != null) {
//                    Log.info { "Using cached audio for paragraph $index" }
//                    playCachedAudio(cached.second)
//                    audioCache.remove(cached)
//                } else {
//                    // Synthesize and play
//                    synthesizeAndPlay(text, index)
//                }
//
//                updateNotification()
//            } catch (e: Exception) {
//                Log.error { "Failed to read paragraph: ${e.message}" }
//                _isPlaying.value = false
//            }
//        }
//    }
//
//    /**
//     * Synthesize and play text
//     */
//    private suspend fun synthesizeAndPlay(text: String, paragraphIndex: Int) {
//        val speed = appPreferences.coquiSpeed().get()
//
//        aiTTSManager.synthesize(
//            text = text,
//            provider = AITTSProvider.COQUI_TTS,
//            voiceId = "default",
//            speed = speed
//        ).onSuccess { audioData ->
//            if (_isPlaying.value) {
//                playAudioData(audioData)
//                // After playing, advance to next
//                advanceToNext()
//            }
//        }.onFailure { error ->
//            Log.error { "Synthesis failed: ${error.message}" }
//            _isPlaying.value = false
//        }
//    }
//
//    /**
//     * Play cached audio
//     */
//    private suspend fun playCachedAudio(audioData: AudioData) {
//        withContext(Dispatchers.IO) {
//            // Play audio (non-blocking)
//            val service = aiTTSManager.getCoquiService()
//            service?.playAudio(audioData)
//
//            // Wait for playback to finish (estimate based on audio duration)
//            delay(audioData.duration.inWholeMilliseconds)
//
//            if (_isPlaying.value) {
//                advanceToNext()
//            }
//        }
//    }
//
//    /**
//     * Play audio data
//     */
//    private suspend fun playAudioData(audioData: AudioData) {
//        withContext(Dispatchers.IO) {
//            val service = aiTTSManager.getCoquiService()
//            service?.playAudio(audioData)
//
//            // Wait for playback
//            delay(audioData.duration.inWholeMilliseconds)
//        }
//    }
//
//    /**
//     * Advance to next paragraph
//     */
//    private fun advanceToNext() {
//        scope.launch {
//            val next = _currentParagraph.value + 1
//            if (next < paragraphs.size && _isPlaying.value) {
//                readParagraph(next)
//            } else if (_isPlaying.value) {
//                handleChapterEnd()
//            }
//        }
//    }
//
//    /**
//     * Handle end of chapter
//     */
//    private suspend fun handleChapterEnd() {
//        Log.info { "Reached end of chapter" }
//
//        // Note: This should use ReaderPreferences.readerAutoNext() but we only have AppPreferences here
//        // For now, we'll rely on the callback to handle auto-next logic
//
//        if (onChapterEnd != null) {
//            Log.info { "Auto-next enabled, loading next chapter..." }
//
//            // Call callback to load next chapter
//            val hasNext = onChapterEnd?.invoke() ?: false
//
//            if (hasNext) {
//                // Wait for chapter to load
//                delay(1000)
//                // Chapter content should be updated externally
//                // Start reading from beginning
//                if (paragraphs.isNotEmpty()) {
//                    readParagraph(0)
//                }
//            } else {
//                Log.info { "No more chapters" }
//                _isPlaying.value = false
//                hideNotification()
//            }
//        } else {
//            Log.info { "Auto-next disabled or no callback" }
//            _isPlaying.value = false
//            updateNotification()
//        }
//    }
//
//    /**
//     * Start preloading next paragraphs
//     */
//    private fun startPreloading() {
//        preloadJob?.cancel()
//        preloadJob = scope.launch(Dispatchers.IO) {
//            while (_isPlaying.value) {
//                try {
//                    val current = _currentParagraph.value
//
//                    // Preload next 3 paragraphs
//                    for (i in 1..Constants.PRELOAD_COUNT) {
//                        val nextIndex = current + i
//                        if (nextIndex >= paragraphs.size) break
//
//                        // Check if already cached
//                        if (audioCache.any { it.first == nextIndex }) continue
//
//                        // Check cache size limit
//                        if (audioCache.size >= Constants.PRELOAD_COUNT) break
//
//                        val text = paragraphs.getOrNull(nextIndex) ?: continue
//                        val speed = appPreferences.coquiSpeed().get()
//
//                        Log.debug { "Preloading paragraph $nextIndex" }
//
//                        aiTTSManager.synthesize(
//                            text = text,
//                            provider = AITTSProvider.COQUI_TTS,
//                            voiceId = "default",
//                            speed = speed
//                        ).onSuccess { audioData ->
//                            audioCache.offer(Pair(nextIndex, audioData))
//                            Log.debug { "Cached paragraph $nextIndex" }
//                        }
//                    }
//
//                    // Wait before next preload check
//                    delay(2000)
//                } catch (e: Exception) {
//                    Log.error { "Preload error: ${e.message}" }
//                }
//            }
//        }
//    }
//
//    /**
//     * Update content (when chapter changes)
//     */
//    fun updateContent(
//        newParagraphs: List<String>,
//        newChapterTitle: String = "",
//        newBookTitle: String = ""
//    ) {
//        this.paragraphs = newParagraphs
//        this.chapterTitle = newChapterTitle
//        this.bookTitle = newBookTitle
//
//        // Clear cache for old chapter
//        audioCache.clear()
//
//        // Restart preloading
//        if (_isPlaying.value) {
//            startPreloading()
//        }
//
//        updateNotification()
//    }
//
//    // ============================================================================
//    // Notification Management
//    // ============================================================================
//
//    private fun createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                Constants.CHANNEL_ID,
//                "Coqui TTS Playback",
//                NotificationManager.IMPORTANCE_LOW
//            ).apply {
//                description = "Controls for Coqui TTS playback"
//                setShowBadge(false)
//            }
//            notificationManager.createNotificationChannel(channel)
//        }
//    }
//
//    private fun showNotification() {
//        val notification = buildNotification()
//        notificationManager.notify(Constants.NOTIFICATION_ID, notification)
//    }
//
//    private fun updateNotification() {
//        if (_isPlaying.value || _currentParagraph.value > 0) {
//            val notification = buildNotification()
//            notificationManager.notify(Constants.NOTIFICATION_ID, notification)
//        }
//    }
//
//    private fun hideNotification() {
//        notificationManager.cancel(Constants.NOTIFICATION_ID)
//    }
//
//    private fun buildNotification(): Notification {
//        val playPauseIntent = createPlayPauseIntent()
//        val stopIntent = createStopIntent()
//        val nextIntent = createNextIntent()
//        val prevIntent = createPrevIntent()
//
//        val playPauseAction = if (_isPlaying.value) {
//            NotificationCompat.Action(
//                android.R.drawable.ic_media_pause,
//                "Pause",
//                playPauseIntent
//            )
//        } else {
//            NotificationCompat.Action(
//                android.R.drawable.ic_media_play,
//                "Play",
//                playPauseIntent
//            )
//        }
//
//        return NotificationCompat.Builder(context, Constants.CHANNEL_ID)
//            .setSmallIcon(android.R.drawable.ic_media_play)
//            .setContentTitle(bookTitle.ifEmpty { "IReader TTS" })
//            .setContentText(chapterTitle.ifEmpty { "Reading..." })
//            .setSubText("Paragraph ${_currentParagraph.value + 1}/${paragraphs.size}")
//            .setPriority(NotificationCompat.PRIORITY_LOW)
//            .setOngoing(_isPlaying.value)
//            .addAction(
//                NotificationCompat.Action(
//                    android.R.drawable.ic_media_previous,
//                    "Previous",
//                    prevIntent
//                )
//            )
//            .addAction(playPauseAction)
//            .addAction(
//                NotificationCompat.Action(
//                    android.R.drawable.ic_media_next,
//                    "Next",
//                    nextIntent
//                )
//            )
//            .addAction(
//                NotificationCompat.Action(
//                    android.R.drawable.ic_menu_close_clear_cancel,
//                    "Stop",
//                    stopIntent
//                )
//            )
//            .setStyle(
//                androidx.media.app.NotificationCompat.MediaStyle()
//                    .setShowActionsInCompactView(0, 1, 2)
//            )
//            .build()
//    }
//
//    private fun createPlayPauseIntent(): PendingIntent {
//        val intent = Intent(context, CoquiTTSReceiver::class.java).apply {
//            action = CoquiTTSReceiver.ACTION_PLAY_PAUSE
//        }
//        return PendingIntent.getBroadcast(
//            context,
//            0,
//            intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//    }
//
//    private fun createStopIntent(): PendingIntent {
//        val intent = Intent(context, CoquiTTSReceiver::class.java).apply {
//            action = CoquiTTSReceiver.ACTION_STOP
//        }
//        return PendingIntent.getBroadcast(
//            context,
//            1,
//            intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//    }
//
//    private fun createNextIntent(): PendingIntent {
//        val intent = Intent(context, CoquiTTSReceiver::class.java).apply {
//            action = CoquiTTSReceiver.ACTION_NEXT
//        }
//        return PendingIntent.getBroadcast(
//            context,
//            2,
//            intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//    }
//
//    private fun createPrevIntent(): PendingIntent {
//        val intent = Intent(context, CoquiTTSReceiver::class.java).apply {
//            action = CoquiTTSReceiver.ACTION_PREV
//        }
//        return PendingIntent.getBroadcast(
//            context,
//            3,
//            intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//    }
//
//    /**
//     * Cleanup
//     */
//    fun cleanup() {
//        stop()
//        scope.cancel()
//        audioCache.clear()
//    }
//}
//
///**
// * Broadcast receiver for notification actions
// */
//class CoquiTTSReceiver : android.content.BroadcastReceiver() {
//    companion object {
//        const val ACTION_PLAY_PAUSE = "ireader.tts.PLAY_PAUSE"
//        const val ACTION_STOP = "ireader.tts.STOP"
//        const val ACTION_NEXT = "ireader.tts.NEXT"
//        const val ACTION_PREV = "ireader.tts.PREV"
//    }
//
//    override fun onReceive(context: Context, intent: Intent) {
//        // Handle notification actions
//        // This will be connected to the player instance
//        Log.info { "Received TTS action: ${intent.action}" }
//    }
//}
