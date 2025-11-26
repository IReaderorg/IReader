package ireader.domain.notification

/**
 * Platform-agnostic notification data model.
 * This provides type safety across all platforms while allowing platform-specific implementations.
 */
data class NotificationData(
    val id: Int,
    val title: String,
    val content: String,
    val channelId: String,
    val priority: NotificationPriority = NotificationPriority.DEFAULT,
    val autoCancel: Boolean = false,
    val ongoing: Boolean = false,
    val progress: NotificationProgress? = null,
    val actions: List<NotificationAction> = emptyList(),
    val style: NotificationStyle? = null,
    val smallIconRes: Int? = null,
    val contentIntent: Any? = null // Platform-specific PendingIntent
)

/**
 * Notification priority levels
 */
enum class NotificationPriority {
    MIN,
    LOW,
    DEFAULT,
    HIGH,
    MAX
}

/**
 * Progress indicator for notifications
 */
data class NotificationProgress(
    val max: Int,
    val current: Int,
    val indeterminate: Boolean = false
)

/**
 * Notification action (buttons)
 */
data class NotificationAction(
    val title: String,
    val intent: Any? = null, // Platform-specific PendingIntent
    val iconRes: Int? = null
)

/**
 * Notification styles
 */
sealed class NotificationStyle {
    data class BigText(val text: String) : NotificationStyle()
    data class BigPicture(val pictureData: Any) : NotificationStyle()
}
