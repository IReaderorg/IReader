package ireader.domain.notification

import android.Manifest
import android.app.Application
import android.app.Notification
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ireader.core.log.Log

/**
 * Android implementation of PlatformNotificationManager
 */
class AndroidNotificationManager(
    private val context: Application
) : PlatformNotificationManager {
    
    private val notificationManager = NotificationManagerCompat.from(context.applicationContext)
    
    override fun show(notification: NotificationData) {
        if (!notificationManager.areNotificationsEnabled()) {
            Log.warn { "AndroidNotificationManager: Cannot show notification (id=${notification.id}) - notifications are disabled in system settings" }
            return
        }
        
        if (!checkNotificationPermission()) {
            Log.warn { "AndroidNotificationManager: Cannot show notification (id=${notification.id}) - POST_NOTIFICATIONS permission not granted" }
            return
        }
        
        val platformNotification = buildNotification(notification)
        notificationManager.notify(notification.id, platformNotification)
    }
    
    override fun showPlatformNotification(id: Int, platformNotification: Any) {
        if (!notificationManager.areNotificationsEnabled()) {
            Log.warn { "AndroidNotificationManager: Cannot show platform notification (id=$id) - notifications are disabled in system settings" }
            return
        }
        
        if (!checkNotificationPermission()) {
            Log.warn { "AndroidNotificationManager: Cannot show platform notification (id=$id) - POST_NOTIFICATIONS permission not granted" }
            return
        }
        
        (platformNotification as? Notification)?.let { notification ->
            notificationManager.notify(id, notification)
            Log.debug { "AndroidNotificationManager: Platform notification shown (id=$id)" }
        } ?: run {
            Log.warn { "AndroidNotificationManager: Platform notification (id=$id) is not an Android Notification type" }
        }
    }
    
    override fun cancel(id: Int) {
        notificationManager.cancel(id)
    }
    
    override fun cancelAll() {
        notificationManager.cancelAll()
    }
    
    override fun areNotificationsEnabled(): Boolean {
        // Check both system-level notification setting and runtime permission
        return notificationManager.areNotificationsEnabled() && checkNotificationPermission()
    }
    
    private fun checkNotificationPermission(): Boolean {
        // POST_NOTIFICATIONS permission is only required on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }
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
