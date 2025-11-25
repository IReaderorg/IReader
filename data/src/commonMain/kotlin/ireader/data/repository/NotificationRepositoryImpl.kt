//package ireader.data.repository
//
//import ireader.domain.data.repository.NotificationRepository
//import ireader.domain.models.notification.*
//
///**
// * Platform-agnostic implementation of NotificationRepository.
// * This is a no-op implementation that logs notification requests but doesn't display actual notifications.
// * Platform-specific implementations should be created in androidMain/desktopMain for actual notification support.
// */
//class NotificationRepositoryImpl : NotificationRepository {
//
//    private val createdChannels = mutableSetOf<String>()
//    private val createdGroups = mutableSetOf<String>()
//    private val activeNotifications = mutableMapOf<Int, IReaderNotification>()
//
//    override suspend fun createNotificationChannels(channels: List<NotificationChannel>) {
//        channels.forEach { channel ->
//            createdChannels.add(channel.id)
//            logNotification("Channel created: ${channel.name} (${channel.id})")
//        }
//    }
//
//    override suspend fun createNotificationGroups(groups: List<NotificationGroup>) {
//        groups.forEach { group ->
//            createdGroups.add(group.id)
//            logNotification("Group created: ${group.name} (${group.id})")
//        }
//    }
//
//    override suspend fun showNotification(notification: IReaderNotification) {
//        activeNotifications[notification.id] = notification
//        logNotification("Show notification [${notification.id}]: ${notification.title} - ${notification.content}")
//    }
//
//    override suspend fun updateNotification(notification: IReaderNotification) {
//        activeNotifications[notification.id] = notification
//        logNotification("Update notification [${notification.id}]: ${notification.title} - ${notification.content}")
//    }
//
//    override suspend fun cancelNotification(notificationId: Int) {
//        activeNotifications.remove(notificationId)
//        logNotification("Cancel notification [$notificationId]")
//    }
//
//    override suspend fun cancelNotificationGroup(groupKey: String) {
//        val toRemove = activeNotifications.filter { it.value.groupKey == groupKey }
//        toRemove.keys.forEach { activeNotifications.remove(it) }
//        logNotification("Cancel notification group: $groupKey (${toRemove.size} notifications)")
//    }
//
//    override suspend fun cancelAllNotifications() {
//        val count = activeNotifications.size
//        activeNotifications.clear()
//        logNotification("Cancel all notifications ($count notifications)")
//    }
//
//    override suspend fun showDownloadProgressNotification(
//        downloadCount: Int,
//        progress: Float,
//        speed: Float,
//        eta: Long
//    ) {
//        val notification = IReaderNotification(
//            id = DOWNLOAD_PROGRESS_NOTIFICATION_ID,
//            channelId = "downloads",
//            title = "Downloading chapters",
//            content = "$downloadCount chapters • ${(progress * 100).toInt()}% • ${formatSpeed(speed)} • ETA: ${formatEta(eta)}",
//            progress = NotificationProgress(
//                current = (progress * 100).toInt(),
//                max = 100,
//                isIndeterminate = false
//            ),
//            ongoing = true,
//            autoCancel = false
//        )
//        showNotification(notification)
//    }
//
//    override suspend fun showDownloadCompletedNotification(
//        bookTitle: String,
//        chapterTitle: String,
//        totalDownloads: Int
//    ) {
//        val notification = IReaderNotification(
//            id = DOWNLOAD_COMPLETE_NOTIFICATION_ID,
//            channelId = "downloads",
//            title = "Download completed",
//            content = "$bookTitle - $chapterTitle${if (totalDownloads > 1) " (+${totalDownloads - 1} more)" else ""}",
//            autoCancel = true
//        )
//        showNotification(notification)
//    }
//
//    override suspend fun showDownloadFailedNotification(
//        bookTitle: String,
//        chapterTitle: String,
//        errorMessage: String
//    ) {
//        val notification = IReaderNotification(
//            id = DOWNLOAD_FAILED_NOTIFICATION_ID,
//            channelId = "downloads",
//            title = "Download failed",
//            content = "$bookTitle - $chapterTitle: $errorMessage",
//            autoCancel = true
//        )
//        showNotification(notification)
//    }
//
//    override suspend fun showLibraryUpdateNotification(data: LibraryUpdateNotification) {
//        val content = buildString {
//            append("${data.totalUpdated} books updated")
//            if (data.hasErrors) {
//                append(" (${data.errorCount} errors)")
//            }
//            append(" • ${formatDuration(data.duration)}")
//        }
//
//        val notification = IReaderNotification(
//            id = LIBRARY_UPDATE_NOTIFICATION_ID,
//            channelId = "library_updates",
//            title = "Library update completed",
//            content = content,
//            autoCancel = true
//        )
//        showNotification(notification)
//    }
//
//    override suspend fun showBackupRestoreNotification(data: BackupRestoreNotification) {
//        val title = when (data.type) {
//            BackupRestoreType.BACKUP -> "Backup"
//            BackupRestoreType.RESTORE -> "Restore"
//        }
//
//        val content = when {
//            data.isCompleted && data.hasErrors -> "$title completed with errors: ${data.errorMessage}"
//            data.isCompleted -> "$title completed successfully"
//            data.hasErrors -> "$title failed: ${data.errorMessage}"
//            else -> "${data.currentItem} (${data.completedItems}/${data.totalItems})"
//        }
//
//        val notification = IReaderNotification(
//            id = BACKUP_RESTORE_NOTIFICATION_ID,
//            channelId = "backup_restore",
//            title = title,
//            content = content,
//            progress = if (!data.isCompleted) {
//                NotificationProgress(
//                    current = (data.progress * 100).toInt(),
//                    max = 100,
//                    isIndeterminate = data.totalItems == 0
//                )
//            } else null,
//            ongoing = !data.isCompleted,
//            autoCancel = data.isCompleted
//        )
//        showNotification(notification)
//    }
//
//    override suspend fun showMigrationNotification(data: MigrationNotification) {
//        val notification = IReaderNotification(
//            id = MIGRATION_NOTIFICATION_ID,
//            channelId = "migration",
//            title = if (data.isSuccess) "Migration completed" else "Migration failed",
//            content = "${data.bookTitle}: ${data.message}",
//            autoCancel = true
//        )
//        showNotification(notification)
//    }
//
//    override suspend fun showMigrationNotification(notification: MigrationNotificationInfo) {
//        val content = buildString {
//            append("${notification.sourceFrom} → ${notification.sourceTo}")
//            if (notification.hasErrors) {
//                append(" • Error: ${notification.errorMessage}")
//            } else if (!notification.isCompleted) {
//                append(" • ${(notification.progress * 100).toInt()}%")
//            }
//        }
//
//        val iReaderNotification = IReaderNotification(
//            id = MIGRATION_NOTIFICATION_ID,
//            channelId = "migration",
//            title = notification.bookTitle,
//            content = content,
//            progress = if (!notification.isCompleted && !notification.hasErrors) {
//                NotificationProgress(
//                    current = (notification.progress * 100).toInt(),
//                    max = 100,
//                    isIndeterminate = false
//                )
//            } else null,
//            ongoing = !notification.isCompleted && !notification.hasErrors,
//            autoCancel = notification.isCompleted || notification.hasErrors
//        )
//        showNotification(iReaderNotification)
//    }
//
//    // Helper functions
//
//    private fun logNotification(message: String) {
//        println("[NotificationRepository] $message")
//    }
//
//    private fun formatSpeed(bytesPerSecond: Float): String {
//        return when {
//            bytesPerSecond < 1024 -> "${bytesPerSecond.toInt()} B/s"
//            bytesPerSecond < 1024 * 1024 -> "${(bytesPerSecond / 1024).toInt()} KB/s"
//            else -> "${(bytesPerSecond / (1024 * 1024)).toInt()} MB/s"
//        }
//    }
//
//    private fun formatEta(milliseconds: Long): String {
//        val seconds = milliseconds / 1000
//        return when {
//            seconds < 60 -> "${seconds}s"
//            seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
//            else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
//        }
//    }
//
//    private fun formatDuration(milliseconds: Long): String {
//        val seconds = milliseconds / 1000
//        return when {
//            seconds < 60 -> "${seconds}s"
//            seconds < 3600 -> "${seconds / 60}m"
//            else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
//        }
//    }
//
//    companion object {
//        private const val DOWNLOAD_PROGRESS_NOTIFICATION_ID = 1001
//        private const val DOWNLOAD_COMPLETE_NOTIFICATION_ID = 1002
//        private const val DOWNLOAD_FAILED_NOTIFICATION_ID = 1003
//        private const val LIBRARY_UPDATE_NOTIFICATION_ID = 2001
//        private const val BACKUP_RESTORE_NOTIFICATION_ID = 3001
//        private const val MIGRATION_NOTIFICATION_ID = 4001
//    }
//}
