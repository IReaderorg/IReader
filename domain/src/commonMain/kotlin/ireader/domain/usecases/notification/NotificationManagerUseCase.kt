package ireader.domain.usecases.notification

import ireader.domain.data.repository.NotificationRepository
import ireader.domain.models.notification.*
import ireader.domain.notification.NotificationsIds

/**
 * Notification manager following Mihon's comprehensive notification system
 */
class NotificationManagerUseCase(
    private val notificationRepository: NotificationRepository
) {
    
    /**
     * Initialize notification channels and groups
     */
    suspend fun initializeNotificationSystem() {
        // Create notification groups
        val groups = listOf(
            NotificationGroup(
                id = NotificationsIds.GROUP_DOWNLOADER,
                name = "Downloads",
                description = "Download progress and completion notifications"
            ),
            NotificationGroup(
                id = NotificationsIds.GROUP_LIBRARY,
                name = "Library Updates",
                description = "Library update progress and new chapter notifications"
            ),
            NotificationGroup(
                id = NotificationsIds.GROUP_BACKUP_RESTORE,
                name = "Backup & Restore",
                description = "Backup and restore operation notifications"
            ),
            NotificationGroup(
                id = "group_migration",
                name = "Migration",
                description = "Book migration progress and completion notifications"
            ),
            NotificationGroup(
                id = NotificationsIds.GROUP_INSTALLER,
                name = "Extensions",
                description = "Extension installation and update notifications"
            ),
            NotificationGroup(
                id = NotificationsIds.GROUP_APK_UPDATES,
                name = "App Updates",
                description = "Application and extension update notifications"
            )
        )
        
        notificationRepository.createNotificationGroups(groups)
        
        // Create notification channels
        val channels = listOf(
            // Download channels
            NotificationChannel(
                id = NotificationsIds.CHANNEL_DOWNLOADER_PROGRESS,
                name = "Download Progress",
                description = "Shows download progress for chapters",
                importance = NotificationImportance.LOW,
                groupId = NotificationsIds.GROUP_DOWNLOADER,
                enableVibration = false,
                enableSound = false
            ),
            NotificationChannel(
                id = NotificationsIds.CHANNEL_DOWNLOADER_COMPLETE,
                name = "Download Complete",
                description = "Shows when downloads are completed",
                importance = NotificationImportance.DEFAULT,
                groupId = NotificationsIds.GROUP_DOWNLOADER
            ),
            NotificationChannel(
                id = NotificationsIds.CHANNEL_DOWNLOADER_ERROR,
                name = "Download Errors",
                description = "Shows download errors and failures",
                importance = NotificationImportance.HIGH,
                groupId = NotificationsIds.GROUP_DOWNLOADER
            ),
            
            // Library update channels
            NotificationChannel(
                id = NotificationsIds.CHANNEL_LIBRARY_PROGRESS,
                name = "Library Update Progress",
                description = "Shows library update progress",
                importance = NotificationImportance.LOW,
                groupId = NotificationsIds.GROUP_LIBRARY,
                enableVibration = false,
                enableSound = false
            ),
            NotificationChannel(
                id = NotificationsIds.CHANNEL_NEW_CHAPTERS,
                name = "New Chapters",
                description = "Shows when new chapters are found",
                importance = NotificationImportance.DEFAULT,
                groupId = NotificationsIds.GROUP_LIBRARY
            ),
            NotificationChannel(
                id = NotificationsIds.CHANNEL_LIBRARY_ERROR,
                name = "Library Update Errors",
                description = "Shows library update errors",
                importance = NotificationImportance.HIGH,
                groupId = NotificationsIds.GROUP_LIBRARY
            ),
            
            // Backup/Restore channels
            NotificationChannel(
                id = NotificationsIds.CHANNEL_BACKUP_RESTORE_PROGRESS,
                name = "Backup/Restore Progress",
                description = "Shows backup and restore progress",
                importance = NotificationImportance.LOW,
                groupId = NotificationsIds.GROUP_BACKUP_RESTORE,
                enableVibration = false,
                enableSound = false
            ),
            NotificationChannel(
                id = NotificationsIds.CHANNEL_BACKUP_RESTORE_COMPLETE,
                name = "Backup/Restore Complete",
                description = "Shows when backup or restore is completed",
                importance = NotificationImportance.DEFAULT,
                groupId = NotificationsIds.GROUP_BACKUP_RESTORE
            ),
            
            // Migration channels
            NotificationChannel(
                id = "migration_progress_channel",
                name = "Migration Progress",
                description = "Shows book migration progress",
                importance = NotificationImportance.LOW,
                groupId = "group_migration",
                enableVibration = false,
                enableSound = false
            ),
            NotificationChannel(
                id = "migration_complete_channel",
                name = "Migration Complete",
                description = "Shows when book migration is completed",
                importance = NotificationImportance.DEFAULT,
                groupId = "group_migration"
            ),
            
            // Extension channels
            NotificationChannel(
                id = NotificationsIds.CHANNEL_INSTALLER_PROGRESS,
                name = "Extension Installation",
                description = "Shows extension installation progress",
                importance = NotificationImportance.LOW,
                groupId = NotificationsIds.GROUP_INSTALLER,
                enableVibration = false,
                enableSound = false
            ),
            NotificationChannel(
                id = NotificationsIds.CHANNEL_INSTALLER_COMPLETE,
                name = "Extension Installed",
                description = "Shows when extensions are installed",
                importance = NotificationImportance.DEFAULT,
                groupId = NotificationsIds.GROUP_INSTALLER
            ),
            
            // App update channels
            NotificationChannel(
                id = NotificationsIds.CHANNEL_APP_UPDATE,
                name = "App Updates",
                description = "Shows available app updates",
                importance = NotificationImportance.DEFAULT,
                groupId = NotificationsIds.GROUP_APK_UPDATES
            ),
            NotificationChannel(
                id = NotificationsIds.CHANNEL_EXTENSIONS_UPDATE,
                name = "Extension Updates",
                description = "Shows available extension updates",
                importance = NotificationImportance.DEFAULT,
                groupId = NotificationsIds.GROUP_APK_UPDATES
            ),
            
            // Common channels
            NotificationChannel(
                id = NotificationsIds.CHANNEL_COMMON,
                name = "General",
                description = "General notifications",
                importance = NotificationImportance.DEFAULT
            ),
            NotificationChannel(
                id = NotificationsIds.CHANNEL_CRASH_LOGS,
                name = "Crash Reports",
                description = "Crash report sharing notifications",
                importance = NotificationImportance.LOW
            ),
            NotificationChannel(
                id = NotificationsIds.CHANNEL_INCOGNITO_MODE,
                name = "Incognito Mode",
                description = "Incognito mode status notifications",
                importance = NotificationImportance.LOW,
                enableVibration = false,
                enableSound = false
            )
        )
        
        notificationRepository.createNotificationChannels(channels)
    }
    
    /**
     * Show download progress notification with grouped updates
     */
    suspend fun showDownloadProgress(
        activeDownloads: Int,
        totalProgress: Float,
        averageSpeed: Float,
        eta: Long
    ) {
        val speedText = formatSpeed(averageSpeed)
        val etaText = formatEta(eta)
        
        val notification = IReaderNotification(
            id = NotificationsIds.ID_DOWNLOAD_CHAPTER_PROGRESS,
            channelId = NotificationsIds.CHANNEL_DOWNLOADER_PROGRESS,
            title = "Downloading $activeDownloads chapters",
            content = "Progress: ${(totalProgress * 100).toInt()}% • $speedText • ETA: $etaText",
            progress = NotificationProgress(
                current = (totalProgress * 100).toInt(),
                max = 100
            ),
            actions = listOf(
                NotificationAction(
                    id = "pause_all",
                    title = "Pause All",
                    icon = "pause"
                ),
                NotificationAction(
                    id = "cancel_all",
                    title = "Cancel All",
                    icon = "cancel"
                )
            ),
            ongoing = true,
            autoCancel = false,
            groupKey = NotificationsIds.GROUP_DOWNLOADER
        )
        
        notificationRepository.showNotification(notification)
    }
    
    /**
     * Show download completed notification
     */
    suspend fun showDownloadCompleted(completedDownloads: List<String>) {
        val title = if (completedDownloads.size == 1) {
            "Download completed"
        } else {
            "${completedDownloads.size} downloads completed"
        }
        
        val content = if (completedDownloads.size == 1) {
            completedDownloads.first()
        } else {
            completedDownloads.take(3).joinToString(", ") + 
                if (completedDownloads.size > 3) " and ${completedDownloads.size - 3} more" else ""
        }
        
        val notification = IReaderNotification(
            id = NotificationsIds.ID_DOWNLOAD_CHAPTER_COMPLETE,
            channelId = NotificationsIds.CHANNEL_DOWNLOADER_COMPLETE,
            title = title,
            content = content,
            actions = listOf(
                NotificationAction(
                    id = "view_downloads",
                    title = "View",
                    icon = "view"
                )
            ),
            groupKey = NotificationsIds.GROUP_DOWNLOADER
        )
        
        notificationRepository.showNotification(notification)
    }
    
    /**
     * Show library update notification
     */
    suspend fun showLibraryUpdate(data: LibraryUpdateNotification) {
        if (data.totalUpdated == 0) {
            // No updates found
            val notification = IReaderNotification(
                id = NotificationsIds.ID_LIBRARY_PROGRESS,
                channelId = NotificationsIds.CHANNEL_LIBRARY_PROGRESS,
                title = "Library update completed",
                content = "No new chapters found in ${data.totalChecked} books",
                autoCancel = true
            )
            notificationRepository.showNotification(notification)
            return
        }
        
        val title = if (data.totalUpdated == 1) {
            "1 book updated"
        } else {
            "${data.totalUpdated} books updated"
        }
        
        val content = if (data.updatedBooks.size == 1) {
            val book = data.updatedBooks.first()
            "${book.bookTitle} • ${book.newChapters} new chapters"
        } else {
            data.updatedBooks.take(3).joinToString(", ") { it.bookTitle } +
                if (data.updatedBooks.size > 3) " and ${data.updatedBooks.size - 3} more" else ""
        }
        
        val notification = IReaderNotification(
            id = NotificationsIds.ID_NEW_CHAPTERS,
            channelId = NotificationsIds.CHANNEL_NEW_CHAPTERS,
            title = title,
            content = content,
            actions = listOf(
                NotificationAction(
                    id = "view_updates",
                    title = "View Updates",
                    icon = "view"
                ),
                NotificationAction(
                    id = "download_all",
                    title = "Download All",
                    icon = "download"
                )
            ),
            groupKey = NotificationsIds.GROUP_LIBRARY
        )
        
        notificationRepository.showNotification(notification)
    }
    
    /**
     * Show migration progress notification
     */
    suspend fun showMigrationProgress(data: MigrationNotification) {
        val notification = IReaderNotification(
            id = -901, // Migration progress ID
            channelId = "migration_progress_channel",
            title = "Migrating ${data.bookTitle}",
            content = "${data.sourceFrom} → ${data.sourceTo} • ${data.status}",
            progress = if (!data.isCompleted) {
                NotificationProgress(
                    current = (data.progress * 100).toInt(),
                    max = 100
                )
            } else null,
            ongoing = !data.isCompleted,
            autoCancel = data.isCompleted,
            groupKey = "group_migration"
        )
        
        notificationRepository.showNotification(notification)
    }
    
    /**
     * Show backup/restore progress notification
     */
    suspend fun showBackupRestoreProgress(data: BackupRestoreNotification) {
        val title = when (data.type) {
            BackupRestoreType.BACKUP -> "Creating backup"
            BackupRestoreType.RESTORE -> "Restoring backup"
        }
        
        val content = if (data.isCompleted) {
            when (data.type) {
                BackupRestoreType.BACKUP -> "Backup completed successfully"
                BackupRestoreType.RESTORE -> "Restore completed successfully"
            }
        } else {
            "${data.completedItems}/${data.totalItems} • ${data.currentItem}"
        }
        
        val notification = IReaderNotification(
            id = when (data.type) {
                BackupRestoreType.BACKUP -> NotificationsIds.ID_BACKUP_PROGRESS
                BackupRestoreType.RESTORE -> NotificationsIds.ID_RESTORE_PROGRESS
            },
            channelId = NotificationsIds.CHANNEL_BACKUP_RESTORE_PROGRESS,
            title = title,
            content = content,
            progress = if (!data.isCompleted) {
                NotificationProgress(
                    current = (data.progress * 100).toInt(),
                    max = 100
                )
            } else null,
            ongoing = !data.isCompleted,
            autoCancel = data.isCompleted,
            groupKey = NotificationsIds.GROUP_BACKUP_RESTORE
        )
        
        notificationRepository.showNotification(notification)
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
}