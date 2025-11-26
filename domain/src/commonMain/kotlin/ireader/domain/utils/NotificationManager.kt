//package ireader.domain.utils
//
//import ireader.domain.notification.PlatformNotificationManager
//
///**
// * Legacy NotificationManager - DEPRECATED
// *
// * This class is maintained for backward compatibility.
// * New code should use PlatformNotificationManager directly for type safety.
// *
// * @see ireader.domain.notification.PlatformNotificationManager
// */
//@Deprecated(
//    message = "Use PlatformNotificationManager for type-safe notifications",
//    replaceWith = ReplaceWith(
//        "PlatformNotificationManager",
//        "ireader.domain.notification.PlatformNotificationManager"
//    )
//)
//expect class NotificationManager {
//    val scope : Any?
//
//    fun show(id: Int, notification: Any)
//    fun cancel(id: Int)
//}