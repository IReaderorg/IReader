package ireader.domain.models.notification

/**
 * Notification channel configuration following Mihon's pattern
 */
data class NotificationChannel(
    val id: String,
    val name: String,
    val description: String,
    val importance: NotificationImportance = NotificationImportance.DEFAULT,
    val groupId: String? = null,
    val enableVibration: Boolean = true,
    val enableSound: Boolean = true,
    val showBadge: Boolean = true
)

/**
 * Notification importance levels
 */
enum class NotificationImportance {
    MIN,
    LOW,
    DEFAULT,
    HIGH,
    MAX
}

/**
 * Notification group configuration
 */
data class NotificationGroup(
    val id: String,
    val name: String,
    val description: String
)

/**
 * Enhanced notification data
 */
data class IReaderNotification(
    val id: Int,
    val channelId: String,
    val title: String,
    val content: String,
    val smallIcon: String? = null,
    val largeIcon: String? = null,
    val progress: NotificationProgress? = null,
    val actions: List<NotificationAction> = emptyList(),
    val autoCancel: Boolean = true,
    val ongoing: Boolean = false,
    val priority: NotificationPriority = NotificationPriority.DEFAULT,
    val groupKey: String? = null,
    val isGroupSummary: Boolean = false,
    val extras: Map<String, Any> = emptyMap()
)

/**
 * Notification progress information
 */
data class NotificationProgress(
    val current: Int,
    val max: Int,
    val isIndeterminate: Boolean = false
)

/**
 * Notification action
 */
data class NotificationAction(
    val id: String,
    val title: String,
    val icon: String? = null,
    val intent: String? = null
)

/**
 * Notification priority
 */
enum class NotificationPriority {
    MIN,
    LOW,
    DEFAULT,
    HIGH,
    MAX
}

/**
 * Library update notification data
 */
data class LibraryUpdateNotification(
    val updatedBooks: List<UpdatedBookInfo>,
    val totalUpdated: Int,
    val totalChecked: Int,
    val duration: Long, // milliseconds
    val hasErrors: Boolean = false,
    val errorCount: Int = 0
)

/**
 * Updated book information for notifications
 */
data class UpdatedBookInfo(
    val bookId: Long,
    val bookTitle: String,
    val newChapters: Int,
    val coverUrl: String? = null
)

/**
 * Backup/Restore notification data
 */
data class BackupRestoreNotification(
    val type: BackupRestoreType,
    val progress: Float,
    val currentItem: String = "",
    val totalItems: Int = 0,
    val completedItems: Int = 0,
    val isCompleted: Boolean = false,
    val hasErrors: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Backup/Restore operation type
 */
enum class BackupRestoreType {
    BACKUP,
    RESTORE
}

/**
 * Migration notification data
 */
data class MigrationNotification(
    val bookTitle: String,
    val sourceFrom: String,
    val sourceTo: String,
    val progress: Float,
    val status: String,
    val isCompleted: Boolean = false,
    val hasErrors: Boolean = false,
    val errorMessage: String? = null
)