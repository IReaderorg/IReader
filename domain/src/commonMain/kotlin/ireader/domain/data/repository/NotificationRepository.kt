package ireader.domain.data.repository

import ireader.domain.models.notification.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing notifications following Mihon's notification system
 */
interface NotificationRepository {
    
    /**
     * Create notification channels
     */
    suspend fun createNotificationChannels(channels: List<NotificationChannel>)
    
    /**
     * Create notification groups
     */
    suspend fun createNotificationGroups(groups: List<NotificationGroup>)
    
    /**
     * Show notification
     */
    suspend fun showNotification(notification: IReaderNotification)
    
    /**
     * Update notification
     */
    suspend fun updateNotification(notification: IReaderNotification)
    
    /**
     * Cancel notification
     */
    suspend fun cancelNotification(notificationId: Int)
    
    /**
     * Cancel all notifications in group
     */
    suspend fun cancelNotificationGroup(groupKey: String)
    
    /**
     * Cancel all notifications
     */
    suspend fun cancelAllNotifications()
    
    /**
     * Show download progress notification
     */
    suspend fun showDownloadProgressNotification(
        downloadCount: Int,
        progress: Float,
        speed: Float,
        eta: Long
    )
    
    /**
     * Show download completed notification
     */
    suspend fun showDownloadCompletedNotification(
        bookTitle: String,
        chapterTitle: String,
        totalDownloads: Int
    )
    
    /**
     * Show download failed notification
     */
    suspend fun showDownloadFailedNotification(
        bookTitle: String,
        chapterTitle: String,
        errorMessage: String
    )
    
    /**
     * Show library update notification
     */
    suspend fun showLibraryUpdateNotification(data: LibraryUpdateNotification)
    
    /**
     * Show backup/restore notification
     */
    suspend fun showBackupRestoreNotification(data: BackupRestoreNotification)
    
    /**
     * Show migration notification
     */
    suspend fun showMigrationNotification(data: MigrationNotification)
    
    /**
     * Get notification preferences
     */
    suspend fun getNotificationPreferences(): NotificationPreferences
    
    /**
     * Save notification preferences
     */
    suspend fun saveNotificationPreferences(preferences: NotificationPreferences)
}

/**
 * Notification preferences
 */
data class NotificationPreferences(
    val enableDownloadNotifications: Boolean = true,
    val enableLibraryUpdateNotifications: Boolean = true,
    val enableBackupNotifications: Boolean = true,
    val enableMigrationNotifications: Boolean = true,
    val enableProgressNotifications: Boolean = true,
    val enableCompletionNotifications: Boolean = true,
    val enableErrorNotifications: Boolean = true,
    val groupNotifications: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = true
)