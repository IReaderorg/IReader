package ireader.domain.usecases.tts

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import ireader.core.log.Log
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Android implementation of TTS notification manager
 * 
 * Uses MediaSession and MediaStyle notifications for rich media controls
 * with lock screen and notification shade integration.
 */
class AndroidTTSNotificationManager(
    private val context: Context,
    private val mediaSession: MediaSessionCompat
) : TTSNotificationManager {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private var callback: TTSNotificationCallback? = null
    private var currentState: TTSNotificationState? = null
    private var currentBook: Book? = null
    private var currentChapter: Chapter? = null
    private var isShowing = false
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "tts_playback_channel"
        private const val CHANNEL_NAME = "TTS Playback"
        
        // Action IDs
        const val ACTION_PLAY_PAUSE = "ireader.tts.PLAY_PAUSE"
        const val ACTION_STOP = "ireader.tts.STOP"
        const val ACTION_NEXT = "ireader.tts.NEXT"
        const val ACTION_PREVIOUS = "ireader.tts.PREVIOUS"
    }
    
    init {
        createNotificationChannel()
    }
    
    override fun showNotification(
        book: Book,
        chapter: Chapter,
        state: TTSNotificationState
    ) {
        this.currentBook = book
        this.currentChapter = chapter
        this.currentState = state
        this.isShowing = true
        
        val notification = buildNotification(book, chapter, state)
        notificationManager.notify(NOTIFICATION_ID, notification)
        
        // Update MediaSession metadata
        updateMediaSession(book, chapter, state)
        
        Log.info { "TTS notification shown: ${book.title} - ${chapter.name}" }
    }
    
    override fun updateNotification(state: TTSNotificationState) {
        if (!isShowing || currentBook == null || currentChapter == null) {
            Log.warn { "Cannot update notification - not showing or missing data" }
            return
        }
        
        this.currentState = state
        
        val notification = buildNotification(currentBook!!, currentChapter!!, state)
        notificationManager.notify(NOTIFICATION_ID, notification)
        
        // Update MediaSession state
        updateMediaSessionState(state)
    }
    
    override fun hideNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
        isShowing = false
        currentBook = null
        currentChapter = null
        currentState = null
        
        Log.info { "TTS notification hidden" }
    }
    
    override fun isNotificationShowing(): Boolean = isShowing
    
    override fun setNotificationCallback(callback: TTSNotificationCallback) {
        this.callback = callback
    }
    
    override fun cleanup() {
        hideNotification()
        callback = null
    }
    
    /**
     * Create notification channel (Android O+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls for text-to-speech playback"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Build the notification
     */
    private fun buildNotification(
        book: Book,
        chapter: Chapter,
        state: TTSNotificationState
    ): Notification {
        val playPauseAction = if (state.isPlaying) {
            NotificationCompat.Action(
                android.R.drawable.ic_media_pause,
                "Pause",
                createPendingIntent(ACTION_PLAY_PAUSE)
            )
        } else {
            NotificationCompat.Action(
                android.R.drawable.ic_media_play,
                "Play",
                createPendingIntent(ACTION_PLAY_PAUSE)
            )
        }
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(book.title)
            .setContentText(chapter.name)
            .setSubText("${state.ttsProvider} • Paragraph ${state.currentParagraph + 1}/${state.totalParagraphs}")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(state.isPlaying)
            .setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            
            // Actions
            .addAction(
                NotificationCompat.Action(
                    android.R.drawable.ic_media_previous,
                    "Previous",
                    createPendingIntent(ACTION_PREVIOUS)
                )
            )
            .addAction(playPauseAction)
            .addAction(
                NotificationCompat.Action(
                    android.R.drawable.ic_media_next,
                    "Next",
                    createPendingIntent(ACTION_NEXT)
                )
            )
            .addAction(
                NotificationCompat.Action(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "Stop",
                    createPendingIntent(ACTION_STOP)
                )
            )
            
            // Media style
            .setStyle(
                MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(createPendingIntent(ACTION_STOP))
            )
        
        // Add progress if available
        if (state.totalParagraphs > 0) {
            builder.setProgress(
                state.totalParagraphs,
                state.currentParagraph,
                false
            )
        }
        
        return builder.build()
    }
    
    /**
     * Create pending intent for notification actions
     */
    private fun createPendingIntent(action: String): PendingIntent {
        val intent = Intent(action).apply {
            setPackage(context.packageName)
        }
        
        return PendingIntent.getBroadcast(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    /**
     * Update MediaSession metadata
     */
    private fun updateMediaSession(
        book: Book,
        chapter: Chapter,
        state: TTSNotificationState
    ) {
        val metadata = android.support.v4.media.MediaMetadataCompat.Builder()
            .putString(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE, chapter.name)
            .putString(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ARTIST, book.title)
            .putString(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM, state.ttsProvider)
            .putLong(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DURATION, -1L)
            .build()
        
        mediaSession.setMetadata(metadata)
        updateMediaSessionState(state)
    }
    
    /**
     * Update MediaSession playback state
     */
    private fun updateMediaSessionState(state: TTSNotificationState) {
        val playbackState = if (state.isPlaying) {
            PlaybackStateCompat.STATE_PLAYING
        } else if (state.isPaused) {
            PlaybackStateCompat.STATE_PAUSED
        } else {
            PlaybackStateCompat.STATE_STOPPED
        }
        
        val stateBuilder = PlaybackStateCompat.Builder()
            .setState(playbackState, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, state.speed)
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_STOP or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            )
        
        mediaSession.setPlaybackState(stateBuilder.build())
    }
}
