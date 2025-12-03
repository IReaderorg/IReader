package ireader.data.repository

import ireader.domain.data.repository.NotificationRepository
import ireader.domain.models.notification.*
import platform.UserNotifications.*
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * iOS implementation of NotificationRepository
 * Uses UserNotifications framework for local notifications
 */
@OptIn(ExperimentalForeignApi::class)
class NotificationRepositoryImpl : NotificationRepository {

    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
    private val createdChannels = mutableSetOf<String>()
    private val createdGroups = mutableSetOf<String>()
    private var isAuthorized = false
    
    init {
        requestAuthorization()
    }
    
    private fun requestAuthorization() {
        val options = UNAuthorizationOptionAlert or 
                      UNAuthorizationOptionSound or 
                      UNAuthorizationOptionBadge
        
        notificationCenter.requestAuthorizationWithOptions(options) { granted, error ->
            isAuthorized = granted
            if (error != null) {
                println("[Notifications] Authorization error: ${error.localizedDescription}")
            }
        }
    }

    override suspend fun createNotificationChannels(channels: List<NotificationChannel>) {
        // iOS doesn't have notification channels like Android
        // Categories serve a similar purpose for grouping actions
        channels.forEach { channel ->
            createdChannels.add(channel.id)
            
            // Create category for this channel
            val category = UNNotificationCategory.categoryWithIdentifier(
                identifier = channel.id,
                actions = emptyList<UNNotificationAction>(),
                intentIdentifiers = emptyList<String>(),
                options = UNNotificationCategoryOptionNone
            )
            notificationCenter.setNotificationCategories(setOf(category))
        }
    }

    override suspend fun createNotificationGroups(groups: List<NotificationGroup>) {
        groups.forEach { group ->
            createdGroups.add(group.id)
        }
    }

    override suspend fun showNotification(notification: IReaderNotification) {
        if (!isAuthorized) {
            println("[iOS Notification] Not authorized: ${notification.title}")
            return
        }
        
        val content = UNMutableNotificationContent().apply {
            setTitle(notification.title)
            setBody(notification.content)
            setSound(UNNotificationSound.defaultSound)
            
            // Set thread identifier for grouping
            notification.groupKey?.let { setThreadIdentifier(it) }
            
            // Set category
            notification.channelId?.let { setCategoryIdentifier(it) }
            
            // Add user info
            val userInfo = mutableMapOf<Any?, Any?>()
            userInfo["notificationId"] = notification.id
            notification.data?.forEach { (key, value) ->
                userInfo[key] = value
            }
            setUserInfo(userInfo)
        }
        
        // Create trigger (immediate)
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            timeInterval = 0.1,
            repeats = false
        )
        
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = notification.id.toString(),
            content = content,
            trigger = trigger
        )
        
        notificationCenter.addNotificationRequest(request) { error ->
            if (error != null) {
                println("[Notifications] Error: ${error.localizedDescription}")
            }
        }
    }

    override suspend fun updateNotification(notification: IReaderNotification) {
        // iOS replaces notifications with the same identifier
        showNotification(notification)
    }

    override suspend fun cancelNotification(notificationId: Int) {
        notificationCenter.removePendingNotificationRequestsWithIdentifiers(
            listOf(notificationId.toString())
        )
        notificationCenter.removeDeliveredNotificationsWithIdentifiers(
            listOf(notificationId.toString())
        )
    }

    override suspend fun cancelNotificationGroup(groupKey: String) {
        // Get all delivered notifications and filter by thread identifier
        notificationCenter.getDeliveredNotificationsWithCompletionHandler { notifications ->
            val idsToRemove = notifications?.mapNotNull { notification ->
                (notification as? UNNotification)?.let {
                    if (it.request.content.threadIdentifier == groupKey) {
                        it.request.identifier
                    } else null
                }
            } ?: emptyList()
            
            if (idsToRemove.isNotEmpty()) {
                notificationCenter.removeDeliveredNotificationsWithIdentifiers(idsToRemove)
            }
        }
    }

    override suspend fun cancelAllNotifications() {
        notificationCenter.removeAllPendingNotificationRequests()
        notificationCenter.removeAllDeliveredNotifications()
    }

    override suspend fun showDownloadProgressNotification(
        downloadCount: Int,
        progress: Float,
        speed: Float,
        eta: Long
    ) {
        val content = UNMutableNotificationContent().apply {
            setTitle("Downloading chapters")
            setBody("$downloadCount chapters • ${(progress * 100).toInt()}% • ${formatSpeed(speed)}")
            setSound(null) // Silent for progress updates
            setThreadIdentifier("download_progress")
        }
        
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "download_progress",
            content = content,
            trigger = null
        )
        
        notificationCenter.addNotificationRequest(request, null)
    }

    override suspend fun showDownloadCompletedNotification(
        bookTitle: String,
        chapterTitle: String,
        totalDownloads: Int
    ) {
        val content = UNMutableNotificationContent().apply {
            setTitle("Download completed")
            val body = "$bookTitle - $chapterTitle${if (totalDownloads > 1) " (+${totalDownloads - 1} more)" else ""}"
            setBody(body)
            setSound(UNNotificationSound.defaultSound)
            setThreadIdentifier("download_complete")
        }
        
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "download_complete_${System.currentTimeMillis()}",
            content = content,
            trigger = null
        )
        
        notificationCenter.addNotificationRequest(request, null)
    }

    override suspend fun showDownloadFailedNotification(
        bookTitle: String,
        chapterTitle: String,
        errorMessage: String
    ) {
        val content = UNMutableNotificationContent().apply {
            setTitle("Download failed")
            setBody("$bookTitle - $chapterTitle: $errorMessage")
            setSound(UNNotificationSound.defaultSound)
            setThreadIdentifier("download_error")
        }
        
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "download_error_${System.currentTimeMillis()}",
            content = content,
            trigger = null
        )
        
        notificationCenter.addNotificationRequest(request, null)
    }

    override suspend fun showLibraryUpdateNotification(data: LibraryUpdateNotification) {
        val content = UNMutableNotificationContent().apply {
            setTitle("Library update completed")
            val body = buildString {
                append("${data.totalUpdated} books updated")
                if (data.hasErrors) {
                    append(" (${data.errorCount} errors)")
                }
                append(" • ${formatDuration(data.duration)}")
            }
            setBody(body)
            setSound(UNNotificationSound.defaultSound)
            setThreadIdentifier("library_update")
        }
        
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "library_update",
            content = content,
            trigger = null
        )
        
        notificationCenter.addNotificationRequest(request, null)
    }

    override suspend fun showBackupRestoreNotification(data: BackupRestoreNotification) {
        val title = when (data.type) {
            BackupRestoreType.BACKUP -> "Backup"
            BackupRestoreType.RESTORE -> "Restore"
        }

        val body = when {
            data.isCompleted && data.hasErrors -> "$title completed with errors: ${data.errorMessage}"
            data.isCompleted -> "$title completed successfully"
            data.hasErrors -> "$title failed: ${data.errorMessage}"
            else -> "${data.currentItem} (${data.completedItems}/${data.totalItems})"
        }

        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(if (data.isCompleted) UNNotificationSound.defaultSound else null)
            setThreadIdentifier("backup_restore")
        }
        
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "backup_restore",
            content = content,
            trigger = null
        )
        
        notificationCenter.addNotificationRequest(request, null)
    }

    override suspend fun showMigrationNotification(data: MigrationNotification) {
        val title = if (data.isSuccess) "Migration completed" else "Migration failed"
        
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody("${data.bookTitle}: ${data.message}")
            setSound(UNNotificationSound.defaultSound)
            setThreadIdentifier("migration")
        }
        
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "migration_${System.currentTimeMillis()}",
            content = content,
            trigger = null
        )
        
        notificationCenter.addNotificationRequest(request, null)
    }

    override suspend fun showMigrationNotification(notification: MigrationNotificationInfo) {
        val body = buildString {
            append("${notification.sourceFrom} → ${notification.sourceTo}")
            if (notification.hasErrors) {
                append(" • Error: ${notification.errorMessage}")
            } else if (!notification.isCompleted) {
                append(" • ${(notification.progress * 100).toInt()}%")
            }
        }

        val content = UNMutableNotificationContent().apply {
            setTitle(notification.bookTitle)
            setBody(body)
            setSound(if (notification.isCompleted) UNNotificationSound.defaultSound else null)
            setThreadIdentifier("migration")
        }
        
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "migration_${notification.bookTitle.hashCode()}",
            content = content,
            trigger = null
        )
        
        notificationCenter.addNotificationRequest(request, null)
    }

    private fun formatSpeed(bytesPerSecond: Float): String {
        return when {
            bytesPerSecond < 1024 -> "${bytesPerSecond.toInt()} B/s"
            bytesPerSecond < 1024 * 1024 -> "${(bytesPerSecond / 1024).toInt()} KB/s"
            else -> "${(bytesPerSecond / (1024 * 1024)).toInt()} MB/s"
        }
    }

    private fun formatDuration(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        return when {
            seconds < 60 -> "${seconds}s"
            seconds < 3600 -> "${seconds / 60}m"
            else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
        }
    }
}

private object System {
    fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
}
