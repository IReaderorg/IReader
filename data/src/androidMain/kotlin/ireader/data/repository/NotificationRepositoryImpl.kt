package ireader.data.repository

import android.app.NotificationChannel as AndroidNotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import ireader.domain.data.repository.NotificationRepository
import ireader.domain.models.notification.*

/**
 * Android implementation of NotificationRepository using Android's notification system.
 */
class NotificationRepositoryImpl(
    private val context: Context
) : NotificationRepository {

    private val notificationManager = NotificationManagerCompat.from(context)

    override suspend fun createNotificationChannels(channels: List<NotificationChannel>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val androidChannels = channels.map { channel ->
                AndroidNotificationChannel(
                    channel.id,
                    channel.name,
                    channel.importance.toAndroidImportance()
                ).apply {
                    description = channel.description
                    enableVibration(channel.enableVibration)
                    setShowBadge(channel.showBadge)
                    channel.groupId?.let { group = it }
                }
            }
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            androidChannels.forEach { systemNotificationManager.createNotificationChannel(it) }
        }
    }

    override suspend fun createNotificationGroups(groups: List<NotificationGroup>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            groups.forEach { group ->
                val androidGroup = android.app.NotificationChannelGroup(group.id, group.name)
                systemNotificationManager.createNotificationChannelGroup(androidGroup)
            }
        }
    }

    override suspend fun showNotification(notification: IReaderNotification) {
        val builder = buildNotification(notification)
        notificationManager.notify(notification.id, builder.build())
    }

    override suspend fun updateNotification(notification: IReaderNotification) {
        showNotification(notification)
    }

    override suspend fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    override suspend fun cancelNotificationGroup(groupKey: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            systemNotificationManager.activeNotifications
                .filter { it.notification.group == groupKey }
                .forEach { notificationManager.cancel(it.id) }
        }
    }

    override suspend fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }

    override suspend fun showDownloadProgressNotification(
        downloadCount: Int,
        progress: Float,
        speed: Float,
        eta: Long
    ) {
        val notification = IReaderNotification(
            id = DOWNLOAD_PROGRESS_NOTIFICATION_ID,
            channelId = "downloads",
            title = "Downloading chapters",
            content = "$downloadCount chapters • ${(progress * 100).toInt()}% • ${formatSpeed(speed)} • ETA: ${formatEta(eta)}",
            progress = NotificationProgress(
                current = (progress * 100).toInt(),
                max = 100,
                isIndeterminate = false
            ),
            ongoing = true,
            autoCancel = false
        )
        showNotification(notification)
    }

    override suspend fun showDownloadCompletedNotification(
        bookTitle: String,
        chapterTitle: String,
        totalDownloads: Int
    ) {
        val notification = IReaderNotification(
            id = DOWNLOAD_COMPLETE_NOTIFICATION_ID,
            channelId = "downloads",
            title = "Download completed",
            content = "$bookTitle - $chapterTitle${if (totalDownloads > 1) " (+${totalDownloads - 1} more)" else ""}",
            autoCancel = true
        )
        showNotification(notification)
    }

    override suspend fun showDownloadFailedNotification(
        bookTitle: String,
        chapterTitle: String,
        errorMessage: String
    ) {
        val notification = IReaderNotification(
            id = DOWNLOAD_FAILED_NOTIFICATION_ID,
            channelId = "downloads",
            title = "Download failed",
            content = "$bookTitle - $chapterTitle: $errorMessage",
            autoCancel = true
        )
        showNotification(notification)
    }

    override suspend fun showLibraryUpdateNotification(data: LibraryUpdateNotification) {
        val content = buildString {
            append("${data.totalUpdated} books updated")
            if (data.hasErrors) {
                append(" (${data.errorCount} errors)")
            }
            append(" • ${formatDuration(data.duration)}")
        }

        val notification = IReaderNotification(
            id = LIBRARY_UPDATE_NOTIFICATION_ID,
            channelId = "library_updates",
            title = "Library update completed",
            content = content,
            autoCancel = true
        )
        showNotification(notification)
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

        val notification = IReaderNotification(
            id = BACKUP_RESTORE_NOTIFICATION_ID,
            channelId = "backup_restore",
            title = title,
            content = content,
            progress = if (!data.isCompleted) {
                NotificationProgress(
                    current = (data.progress * 100).toInt(),
                    max = 100,
                    isIndeterminate = data.totalItems == 0
                )
            } else null,
            ongoing = !data.isCompleted,
            autoCancel = data.isCompleted
        )
        showNotification(notification)
    }

    override suspend fun showMigrationNotification(data: MigrationNotification) {
        val notification = IReaderNotification(
            id = MIGRATION_NOTIFICATION_ID,
            channelId = "migration",
            title = if (data.isSuccess) "Migration completed" else "Migration failed",
            content = "${data.bookTitle}: ${data.message}",
            autoCancel = true
        )
        showNotification(notification)
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

        val iReaderNotification = IReaderNotification(
            id = MIGRATION_NOTIFICATION_ID,
            channelId = "migration",
            title = notification.bookTitle,
            content = content,
            progress = if (!notification.isCompleted && !notification.hasErrors) {
                NotificationProgress(
                    current = (notification.progress * 100).toInt(),
                    max = 100,
                    isIndeterminate = false
                )
            } else null,
            ongoing = !notification.isCompleted && !notification.hasErrors,
            autoCancel = notification.isCompleted || notification.hasErrors
        )
        showNotification(iReaderNotification)
    }

    // Helper functions

    private fun buildNotification(notification: IReaderNotification): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(context, notification.channelId)
            .setContentTitle(notification.title)
            .setContentText(notification.content)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(notification.priority.toAndroidPriority())
            .setAutoCancel(notification.autoCancel)
            .setOngoing(notification.ongoing)

        notification.progress?.let { progress ->
            builder.setProgress(progress.max, progress.current, progress.isIndeterminate)
        }

        notification.groupKey?.let { groupKey ->
            builder.setGroup(groupKey)
            if (notification.isGroupSummary) {
                builder.setGroupSummary(true)
            }
        }

        notification.actions.forEach { action ->
            // Create pending intent for action if needed
            val actionBuilder = NotificationCompat.Action.Builder(
                0, // icon resource
                action.title,
                null // PendingIntent - would need to be created based on action.intent
            )
            builder.addAction(actionBuilder.build())
        }

        return builder
    }

    private fun NotificationImportance.toAndroidImportance(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            when (this) {
                NotificationImportance.MIN -> NotificationManager.IMPORTANCE_MIN
                NotificationImportance.LOW -> NotificationManager.IMPORTANCE_LOW
                NotificationImportance.DEFAULT -> NotificationManager.IMPORTANCE_DEFAULT
                NotificationImportance.HIGH -> NotificationManager.IMPORTANCE_HIGH
                NotificationImportance.MAX -> NotificationManager.IMPORTANCE_MAX
            }
        } else {
            NotificationManager.IMPORTANCE_DEFAULT
        }
    }

    private fun NotificationPriority.toAndroidPriority(): Int {
        return when (this) {
            NotificationPriority.MIN -> NotificationCompat.PRIORITY_MIN
            NotificationPriority.LOW -> NotificationCompat.PRIORITY_LOW
            NotificationPriority.DEFAULT -> NotificationCompat.PRIORITY_DEFAULT
            NotificationPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
            NotificationPriority.MAX -> NotificationCompat.PRIORITY_MAX
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

    companion object {
        private const val DOWNLOAD_PROGRESS_NOTIFICATION_ID = 1001
        private const val DOWNLOAD_COMPLETE_NOTIFICATION_ID = 1002
        private const val DOWNLOAD_FAILED_NOTIFICATION_ID = 1003
        private const val LIBRARY_UPDATE_NOTIFICATION_ID = 2001
        private const val BACKUP_RESTORE_NOTIFICATION_ID = 3001
        private const val MIGRATION_NOTIFICATION_ID = 4001
    }
}
