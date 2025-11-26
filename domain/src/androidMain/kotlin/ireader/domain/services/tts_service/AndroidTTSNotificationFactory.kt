package ireader.domain.services.tts_service

import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationManagerCompat
import ireader.domain.notification.NotificationsIds
import ireader.domain.services.tts_service.media_player.TTSNotificationBuilder
import ireader.i18n.LocalizeHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android implementation of TTS Notification Factory
 */
actual object TTSNotificationFactory : KoinComponent {
    private val context: Context by inject()
    private val localizeHelper: LocalizeHelper by inject()
    
    actual fun create(callback: TTSNotificationCallback): TTSNotification {
        return AndroidTTSNotificationImpl(context, localizeHelper, callback)
    }
}

/**
 * Android implementation using existing TTSNotificationBuilder
 * This adapter bridges the cross-platform abstraction with Android's MediaSession-based notifications
 */
class AndroidTTSNotificationImpl(
    private val context: Context,
    private val localizeHelper: LocalizeHelper,
    private val callback: TTSNotificationCallback
) : TTSNotification {
    
    private val notificationBuilder = TTSNotificationBuilder(context, localizeHelper)
    private val notificationManager = NotificationManagerCompat.from(context)
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Store current data for updates
    private var currentData: TTSNotificationData? = null
    private var mediaSession: MediaSessionCompat? = null
    
    /**
     * Set the MediaSession for notification integration
     * This is required for Android's MediaStyle notifications
     */
    fun setMediaSession(session: MediaSessionCompat) {
        this.mediaSession = session
    }
    
    override fun show(data: TTSNotificationData) {
        currentData = data
        updateNotification()
    }
    
    override fun hide() {
        notificationManager.cancel(NotificationsIds.ID_TTS)
        currentData = null
    }
    
    override fun updatePlaybackState(isPlaying: Boolean) {
        currentData?.let { data ->
            currentData = data.copy(isPlaying = isPlaying)
            updateNotification()
        }
    }
    
    override fun updateProgress(current: Int, total: Int) {
        currentData?.let { data ->
            currentData = data.copy(
                currentParagraph = current,
                totalParagraphs = total
            )
            updateNotification()
        }
    }
    
    private fun updateNotification() {
        val session = mediaSession ?: return
        val data = currentData ?: return
        
        scope.launch {
            val notification = notificationBuilder.buildTTSNotification(session).build()
            notificationManager.notify(NotificationsIds.ID_TTS, notification)
        }
    }
}
