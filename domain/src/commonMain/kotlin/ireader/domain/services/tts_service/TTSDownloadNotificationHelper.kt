package ireader.domain.services.tts_service

import ireader.domain.notification.NotificationPriority
import ireader.domain.notification.PlatformNotificationManager
import ireader.domain.notification.buildNotification
import ireader.domain.notification.showProgress
import ireader.domain.notification.showSimple

/**
 * Helper class for managing TTS chapter download notifications.
 * Provides progress updates, pause/cancel actions, and completion/failure states.
 */
class TTSDownloadNotificationHelper(
    private val notificationManager: PlatformNotificationManager
) {
    companion object {
        const val NOTIFICATION_ID = ireader.domain.notification.NotificationsIds.ID_TTS_DOWNLOAD
        const val CHANNEL_ID = ireader.domain.notification.NotificationsIds.CHANNEL_TTS_DOWNLOAD
    }
    
    /**
     * Show starting notification with indeterminate progress
     */
    fun showStarting(chapterName: String, bookTitle: String) {
        notificationManager.showProgress(
            id = NOTIFICATION_ID,
            channelId = CHANNEL_ID,
            title = "Downloading TTS Audio",
            content = "$chapterName - $bookTitle",
            max = 0,
            current = 0,
            indeterminate = true,
            ongoing = true
        )
    }
    
    /**
     * Update progress notification
     * @param pauseIntent Platform-specific PendingIntent for pause action
     * @param cancelIntent Platform-specific PendingIntent for cancel action
     */
    fun updateProgress(
        chapterName: String,
        bookTitle: String,
        currentParagraph: Int,
        totalParagraphs: Int,
        isPaused: Boolean = false,
        pauseIntent: Any? = null,
        cancelIntent: Any? = null
    ) {
        val notification = buildNotification(NOTIFICATION_ID, CHANNEL_ID) {
            title = if (isPaused) "TTS Download Paused" else "Downloading TTS Audio"
            content = "$chapterName ($currentParagraph/$totalParagraphs)"
            setProgress(totalParagraphs, currentParagraph, false)
            ongoing = true
            priority = NotificationPriority.LOW
            
            // Add pause/resume action
            if (pauseIntent != null) {
                val pauseTitle = if (isPaused) "Resume" else "Pause"
                addAction(pauseTitle, pauseIntent)
            }
            
            // Add cancel action
            if (cancelIntent != null) {
                addAction("Cancel", cancelIntent)
            }
        }
        notificationManager.show(notification)
    }
    
    /**
     * Show completion notification
     */
    fun showComplete(chapterName: String, bookTitle: String) {
        notificationManager.showSimple(
            id = NOTIFICATION_ID,
            channelId = CHANNEL_ID,
            title = "TTS Download Complete",
            content = "$chapterName - $bookTitle",
            autoCancel = true
        )
    }
    
    /**
     * Show failure notification
     */
    fun showFailed(chapterName: String, bookTitle: String, error: String) {
        val notification = buildNotification(NOTIFICATION_ID, CHANNEL_ID) {
            title = "TTS Download Failed"
            content = chapterName
            priority = NotificationPriority.HIGH
            autoCancel = true
            setBigTextStyle("Failed to download TTS audio for $chapterName\n\nError: $error")
        }
        notificationManager.show(notification)
    }
    
    /**
     * Show cancelled notification
     */
    fun showCancelled(chapterName: String) {
        notificationManager.showSimple(
            id = NOTIFICATION_ID,
            channelId = CHANNEL_ID,
            title = "TTS Download Cancelled",
            content = chapterName,
            autoCancel = true
        )
    }
    
    /**
     * Cancel the notification
     */
    fun cancel() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
