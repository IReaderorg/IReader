//package ireader.domain.utils
//
//import ireader.domain.notification.DesktopNotificationManager
//import ireader.domain.notification.PlatformNotificationManager
//
///**
// * Legacy Desktop NotificationManager - DEPRECATED
// * Delegates to DesktopNotificationManager for backward compatibility
// */
//@Deprecated(
//    message = "Use PlatformNotificationManager for type-safe notifications",
//    replaceWith = ReplaceWith(
//        "DesktopNotificationManager",
//        "ireader.domain.notification.DesktopNotificationManager"
//    )
//)
//actual class NotificationManager {
//    actual val scope: Any? = null
//
//    private val platformManager: PlatformNotificationManager = DesktopNotificationManager()
//
//    actual fun show(id: Int, notification: Any) {
//        // No-op for desktop
//    }
//
//    actual fun cancel(id: Int) {
//        platformManager.cancel(id)
//    }
//}