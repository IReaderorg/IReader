package ireader.domain.services.common

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import ireader.domain.notification.NotificationsIds

/**
 * Android implementation of NotificationService
 */
class AndroidNotificationService(
    private val context: Context
) : NotificationService {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val activeNotifications = mutableMapOf<Int, NotificationCompat.Builder>()
    
    override suspend fun initialize() {}
    override suspend fun start() {}
    override suspend fun stop() {}
    override fun isRunning(): Boolean = true
    override suspend fun cleanup() {
        cancelAllNotifications()
    }
    
    override fun showNotification(
        id: Int,
        title: String,
        message: String,
        priority: NotificationPriority
    ) {
        val builder = NotificationCompat.Builder(context, getChannelId(priority))
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(ireader.i18n.R.drawable.ic_downloading)
            .apply {
                this.priority = mapPriority(priority)
            }
            .setAutoCancel(true)
        
        activeNotifications[id] = builder
        notificationManager.notify(id, builder.build())
    }
    
    override fun showProgressNotification(
        id: Int,
        title: String,
        message: String,
        progress: Int,
        maxProgress: Int,
        indeterminate: Boolean
    ) {
        val builder = NotificationCompat.Builder(context, NotificationsIds.CHANNEL_DOWNLOADER_PROGRESS)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(ireader.i18n.R.drawable.ic_downloading)
            .setProgress(maxProgress, progress, indeterminate)
            .setOngoing(true)
        
        activeNotifications[id] = builder
        notificationManager.notify(id, builder.build())
    }
    
    override fun updateNotification(
        id: Int,
        title: String?,
        message: String?,
        progress: Int?,
        maxProgress: Int?
    ) {
        val builder = activeNotifications[id] ?: return
        
        title?.let { builder.setContentTitle(it) }
        message?.let { builder.setContentText(it) }
        
        if (progress != null && maxProgress != null) {
            builder.setProgress(maxProgress, progress, false)
        }
        
        notificationManager.notify(id, builder.build())
    }
    
    override fun cancelNotification(id: Int) {
        notificationManager.cancel(id)
        activeNotifications.remove(id)
    }
    
    override fun cancelAllNotifications() {
        notificationManager.cancelAll()
        activeNotifications.clear()
    }
    
    override fun areNotificationsEnabled(): Boolean {
        return notificationManager.areNotificationsEnabled()
    }
    
    private fun mapPriority(priority: NotificationPriority): Int {
        return when (priority) {
            NotificationPriority.MIN -> NotificationCompat.PRIORITY_MIN
            NotificationPriority.LOW -> NotificationCompat.PRIORITY_LOW
            NotificationPriority.DEFAULT -> NotificationCompat.PRIORITY_DEFAULT
            NotificationPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
            NotificationPriority.MAX -> NotificationCompat.PRIORITY_MAX
        }
    }
    
    private fun getChannelId(priority: NotificationPriority): String {
        return when (priority) {
            NotificationPriority.HIGH, NotificationPriority.MAX -> 
                NotificationsIds.CHANNEL_DOWNLOADER_ERROR
            else -> 
                NotificationsIds.CHANNEL_DOWNLOADER_COMPLETE
        }
    }
}
