package ireader.domain.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.app.Notification
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import ireader.domain.notification.AndroidNotificationManager
import ireader.domain.notification.PlatformNotificationManager

/**
 * Legacy Android NotificationManager - DEPRECATED
 * Delegates to AndroidNotificationManager for backward compatibility
 */
@Deprecated(
    message = "Use PlatformNotificationManager for type-safe notifications",
    replaceWith = ReplaceWith(
        "AndroidNotificationManager",
        "ireader.domain.notification.AndroidNotificationManager"
    )
)
actual class NotificationManager(
    private val context: Application
) {
    actual val scope : Any? = NotificationManagerCompat.from(context.applicationContext)
    
    private val platformManager: PlatformNotificationManager = AndroidNotificationManager(context)
    private val notificationManagerScope = scope as? NotificationManagerCompat
    
    @SuppressLint("MissingPermission")
    actual fun show(id: Int, notification: Any) {
        (notification as? Notification)?.let { notifications ->
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notificationManagerScope?.notify(id, notifications)
        }
    }
    
    actual fun cancel(id: Int) {
        notificationManagerScope?.cancel(id)
    }
}