package ireader.data.repository

import ireader.domain.data.repository.NotificationRepository
import ireader.domain.models.notification.*
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.Toolkit

/**
 * Desktop implementation of NotificationRepository using system tray notifications.
 * Falls back to console logging if system tray is not supported.
 */
class NotificationRepositoryImpl : NotificationRepository {

    private val trayIcon: TrayIcon? = initializeTrayIcon()
    private val createdChannels = mutableSetOf<String>()
    private val createdGroups = mutableSetOf<String>()

    private fun initializeTrayIcon(): TrayIcon? {
        return try {
            if (SystemTray.isSupported()) {
                val tray = SystemTray.getSystemTray()
                val image = Toolkit.getDefaultToolkit().createImage(ByteArray(0))
                val icon = TrayIcon(image, "IReader")
                icon.isImageAutoSize = true
                tray.add(icon)
                icon
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

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
        displayNotification(notification.title, notification.content)
    }

    override suspend fun updateNotification(notification: IReaderNotification) {
        displayNotification(notification.title, notification.content)
    }

    override suspend fun cancelNotification(notificationId: Int) {
        // No-op for desktop
    }

    override suspend fun cancelNotificationGroup(groupKey: String) {
        // No-op for desktop
    }

    override suspend fun cancelAllNotifications() {
        // No-op for desktop
    }

    override suspend fun showDownloadProgressNotification(
        downloadCount: Int,
        progress: Float,
        speed: Float,
        eta: Long
    ) {
        val content = "$downloadCount chapters • ${(progress * 100).toInt()}% • ${formatSpeed(speed)} • ETA: ${formatEta(eta)}"
        displayNotification("Downloading chapters", content)
    }

    override suspend fun showDownloadCompletedNotification(
        bookTitle: String,
        chapterTitle: String,
        totalDownloads: Int
    ) {
        val content = "$bookTitle - $chapterTitle${if (totalDownloads > 1) " (+${totalDownloads - 1} more)" else ""}"
        displayNotification("Download completed", content)
    }

    override suspend fun showDownloadFailedNotification(
        bookTitle: String,
        chapterTitle: String,
        errorMessage: String
    ) {
        val content = "$bookTitle - $chapterTitle: $errorMessage"
        displayNotification("Download failed", content, TrayIcon.MessageType.ERROR)
    }

    override suspend fun showLibraryUpdateNotification(data: LibraryUpdateNotification) {
        val content = buildString {
            append("${data.totalUpdated} books updated")
            if (data.hasErrors) {
                append(" (${data.errorCount} errors)")
            }
            append(" • ${formatDuration(data.duration)}")
        }
        displayNotification("Library update completed", content)
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

        val messageType = when {
            data.hasErrors -> TrayIcon.MessageType.ERROR
            data.isCompleted -> TrayIcon.MessageType.INFO
            else -> TrayIcon.MessageType.NONE
        }

        displayNotification(title, content, messageType)
    }

    override suspend fun showMigrationNotification(data: MigrationNotification) {
        val title = if (data.isSuccess) "Migration completed" else "Migration failed"
        val content = "${data.bookTitle}: ${data.message}"
        val messageType = if (data.isSuccess) TrayIcon.MessageType.INFO else TrayIcon.MessageType.ERROR
        displayNotification(title, content, messageType)
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

        val messageType = when {
            notification.hasErrors -> TrayIcon.MessageType.ERROR
            notification.isCompleted -> TrayIcon.MessageType.INFO
            else -> TrayIcon.MessageType.NONE
        }

        displayNotification(notification.bookTitle, content, messageType)
    }

    // Helper functions

    private fun displayNotification(
        title: String,
        content: String,
        messageType: TrayIcon.MessageType = TrayIcon.MessageType.INFO
    ) {
        trayIcon?.let {
            try {
                it.displayMessage(title, content, messageType)
            } catch (_: Exception) {
                // Silently ignore notification display errors
            }
        }
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
