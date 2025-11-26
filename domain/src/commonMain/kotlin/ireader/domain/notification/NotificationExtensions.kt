package ireader.domain.notification

/**
 * Extension functions to make working with notifications easier
 */

/**
 * Builder DSL for creating NotificationData
 */
fun buildNotification(
    id: Int,
    channelId: String,
    builder: NotificationDataBuilder.() -> Unit
): NotificationData {
    return NotificationDataBuilder(id, channelId).apply(builder).build()
}

class NotificationDataBuilder(
    private val id: Int,
    private val channelId: String
) {
    var title: String = ""
    var content: String = ""
    var priority: NotificationPriority = NotificationPriority.DEFAULT
    var autoCancel: Boolean = false
    var ongoing: Boolean = false
    var progress: NotificationProgress? = null
    var smallIconRes: Int? = null
    var contentIntent: Any? = null
    var style: NotificationStyle? = null
    private val actions = mutableListOf<NotificationAction>()
    
    fun setProgress(max: Int, current: Int, indeterminate: Boolean = false) {
        progress = NotificationProgress(max, current, indeterminate)
    }
    
    fun addAction(title: String, intent: Any? = null, iconRes: Int? = null) {
        actions.add(NotificationAction(title, intent, iconRes))
    }
    
    fun setBigTextStyle(text: String) {
        style = NotificationStyle.BigText(text)
    }
    
    fun build(): NotificationData {
        return NotificationData(
            id = id,
            title = title,
            content = content,
            channelId = channelId,
            priority = priority,
            autoCancel = autoCancel,
            ongoing = ongoing,
            progress = progress,
            actions = actions,
            style = style,
            smallIconRes = smallIconRes,
            contentIntent = contentIntent
        )
    }
}

/**
 * Quick notification creation for simple cases
 */
fun PlatformNotificationManager.showSimple(
    id: Int,
    channelId: String,
    title: String,
    content: String,
    priority: NotificationPriority = NotificationPriority.DEFAULT,
    autoCancel: Boolean = true
) {
    show(
        NotificationData(
            id = id,
            title = title,
            content = content,
            channelId = channelId,
            priority = priority,
            autoCancel = autoCancel,
            ongoing = false
        )
    )
}

/**
 * Show a progress notification
 */
fun PlatformNotificationManager.showProgress(
    id: Int,
    channelId: String,
    title: String,
    content: String,
    max: Int,
    current: Int,
    indeterminate: Boolean = false,
    ongoing: Boolean = true
) {
    show(
        NotificationData(
            id = id,
            title = title,
            content = content,
            channelId = channelId,
            priority = NotificationPriority.LOW,
            autoCancel = false,
            ongoing = ongoing,
            progress = NotificationProgress(max, current, indeterminate)
        )
    )
}
