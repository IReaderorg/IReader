package ireader.data.repository

import ireader.domain.data.repository.NotificationRepository
import ireader.domain.models.notification.*

/**
 * iOS implementation of NotificationRepository
 * Uses UserNotifications framework for local notifications
 * 
 * TODO: Implement using:
 * - platform.UserNotifications.UNUserNotificationCenter
 * - UNMutableNotificationContent
 * - UNNotificationRequest
 */
class NotificationRepositoryImpl : NotificationRepository {

    private val createdChannels = mutableSetOf<String>()
    private val createdGroups = mutableSetOf<String>()

    override suspend fun createNotificationChannels(channels: List<NotificationChannel>) {
        channels.forEach { channel ->
            createdChannels.add(channel.id)
        }
    }

    override suspend fun createNotificationGroups(groups: List<NotificationGroup>) {
        groups.forEach { group ->
            createdGroups.add(group.id)
        }
    }

    override suspend fun showNotification(notification: IReaderNotification) {
        // TODO: Implement using UNUserNotificationCenter
        println("[iOS Notification] ${notification.title}: ${notification.content}")
    }

    override suspend fun updateNotification(notification: IReaderNotification) {
        showNotification(notification)
    }

    override suspend fun cancelNotification(notificationId: Int) {
        // TODO: Implement using UNUserNotificationCenter.removePendingNotificationRequests
    }

    override suspend fun cancelNotificationGroup(groupKey: String) {
        // TODO: Implement
    }

    override suspend fun cancelAllNotifications() {
        // TODO: Implement using UNUserNotificationCenter.removeAllPendingNotificationRequests
    }

    override suspend fun showDownloadProgressNotification(
        downloadCount: Int,
        progress: Float,
        speed: Float,
        eta: Long
    ) {
        val content = "$downloadCount chapters • ${(progress * 100).toInt()}% • ${formatSpeed(speed)} • ETA: ${formatEta(eta)}"
        println("[iOS Notification] Downloading chapters: $content")
    }

    override suspend fun showDownloadCompletedNotification(
        bookTitle: String,
        chapterTitle: String,
        totalDownloads: Int
    ) {
        val content = "$bookTitle - $chapterTitle${if (totalDownloads > 1) " (+${totalDownloads - 1} more)" else ""}"
        println("[iOS Notification] Download completed: $content")
    }

    override suspend fun showDownloadFailedNotification(
        bookTitle: String,
        chapterTitle: String,
        errorMessage: String
    ) {
        val content = "$bookTitle - $chapterTitle: $errorMessage"
        println("[iOS Notification] Download failed: $content")
    }

    override suspend fun showLibraryUpdateNotification(data: LibraryUpdateNotification) {
        val content = buildString {
            append("${data.totalUpdated} books updated")
            if (data.hasErrors) {
                append(" (${data.errorCount} errors)")
            }
            append(" • ${formatDuration(data.duration)}")
        }
        println("[iOS Notification] Library update completed: $content")
    }

    override suspend fun showBackupRestoreNotification(data: BackupRestoreNotification) {
        val title = when (data.type) {
            BackupRestoreType.BACKUP -> "Backup"
            BackupRestoreType.RESTORE -> "Restore"
        }

        val content = when {
            data.isCompleted && data.hasErrors -> "$title completed with errors: ${data.errorMessage}"
            data.isCompleted -> "$title completed successfully"
            data.hasErrors -> "$title failed: ${data.errorMessage}"
            else -> "${data.currentItem} (${data.completedItems}/${data.totalItems})"
        }

        println("[iOS Notification] $title: $content")
    }

    override suspend fun showMigrationNotification(data: MigrationNotification) {
        val title = if (data.isSuccess) "Migration completed" else "Migration failed"
        val content = "${data.bookTitle}: ${data.message}"
        println("[iOS Notification] $title: $content")
    }

    override suspend fun showMigrationNotification(notification: MigrationNotificationInfo) {
        val content = buildString {
            append("${notification.sourceFrom} → ${notification.sourceTo}")
            if (notification.hasErrors) {
                append(" • Error: ${notification.errorMessage}")
            } else if (!notification.isCompleted) {
                append(" • ${(notification.progress * 100).toInt()}%")
            }
        }
        println("[iOS Notification] ${notification.bookTitle}: $content")
    }

    private fun formatSpeed(bytesPerSecond: Float): String {
        return when {
            bytesPerSecond < 1024 -> "${bytesPerSecond.toInt()} B/s"
            bytesPerSecond < 1024 * 1024 -> "${(bytesPerSecond / 1024).toInt()} KB/s"
            else -> "${(bytesPerSecond / (1024 * 1024)).toInt()} MB/s"
        }
    }

    private fun formatEta(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        return when {
            seconds < 60 -> "${seconds}s"
            seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
            else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
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
