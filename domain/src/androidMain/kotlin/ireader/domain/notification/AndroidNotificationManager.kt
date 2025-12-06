package ireader.domain.notification

import android.Manifest
import android.app.Application
import android.app.Notification
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Android implementation of PlatformNotificationManager
 */
class AndroidNotificationManager(
    private val context: Application
) : PlatformNotificationManager {
    
    private val notificationManager = NotificationManagerCompat.from(context.applicationContext)
    
    override fun show(notification: NotificationData) {
        if (!checkNotificationPermission()) return
        
        val platformNotification = buildNotification(notification)
        notificationManager.notify(notification.id, platformNotification)
    }
    
    override fun showPlatformNotification(id: Int, platformNotification: Any) {
        if (!checkNotificationPermission()) return
        
        (platformNotification as? Notification)?.let { notification ->
            notificationManager.notify(id, notification)
        }
    }
    
    override fun cancel(id: Int) {
        notificationManager.cancel(id)
    }
    
    override fun cancelAll() {
        notificationManager.cancelAll()
    }
    
    override fun areNotificationsEnabled(): Boolean {
        return checkNotificationPermission()
    }
    
    private fun checkNotificationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun buildNotification(data: NotificationData): Notification {
        val builder = NotificationCompat.Builder(context, data.channelId)
            .setContentTitle(data.title)
            .setContentText(data.content)
            .setPriority(data.priority.toAndroidPriority())
            .setAutoCancel(data.autoCancel)
            .setOngoing(data.ongoing)
        
        // Set small icon - use provided or default to ic_downloading
        val iconRes = data.smallIconRes ?: ireader.i18n.R.drawable.ic_downloading
        builder.setSmallIcon(iconRes)
        
        // Set progress if provided
        data.progress?.let { progress ->
            builder.setProgress(progress.max, progress.current, progress.indeterminate)
        }
        
        // Set content intent if provided
        (data.contentIntent as? android.app.PendingIntent)?.let {
            builder.setContentIntent(it)
        }
        
        // Set style if provided
        when (val style = data.style) {
            is NotificationStyle.BigText -> {
                builder.setStyle(NotificationCompat.BigTextStyle().bigText(style.text))
            }
            is NotificationStyle.BigPicture -> {
                (style.pictureData as? android.graphics.Bitmap)?.let { bitmap ->
                    builder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
                }
            }
            null -> {} // No style
        }
        
        // Add actions
        data.actions.forEach { action ->
            (action.intent as? android.app.PendingIntent)?.let { intent ->
                builder.addAction(
                    action.iconRes ?: 0,
                    action.title,
                    intent
                )
            }
        }
        
        return builder.build()
    }
    
    private fun NotificationPriority.toAndroidPriority(): Int {
        return when (this) {
            NotificationPriority.MIN -> NotificationCompat.PRIORITY_MIN
            NotificationPriority.LOW -> NotificationCompat.PRIORITY_LOW
            NotificationPriority.DEFAULT -> NotificationCompat.PRIORITY_DEFAULT
            NotificationPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
            NotificationPriority.MAX -> NotificationCompat.PRIORITY_MAX
        }
    }
}

actual fun createPlatformNotificationManager(): PlatformNotificationManager {
    throw IllegalStateException(
        "createPlatformNotificationManager() requires Application context. " +
        "Use AndroidNotificationManager(application) directly or inject via DI."
    )
}
