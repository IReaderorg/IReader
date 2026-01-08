package ireader.domain.services.downloaderService

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import ireader.i18n.R
import ireader.domain.models.download.Download
import ireader.domain.models.download.DownloadStatus
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.notification.NotificationsIds
import ireader.domain.notification.legacyFlags
import ireader.domain.utils.extensions.launchMainActivityIntent
import ireader.i18n.Args
import ireader.i18n.LocalizeHelper
import ireader.i18n.SHORTCUTS
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import java.util.*

/**
 * Download action constants for broadcast receiver.
 */
object DownloadActions {
    const val ACTION_PAUSE = "ireader.action.DOWNLOAD_PAUSE"
    const val ACTION_RESUME = "ireader.action.DOWNLOAD_RESUME"
    const val ACTION_CANCEL = "ireader.action.DOWNLOAD_CANCEL"
    const val ACTION_ALLOW_MOBILE_DATA = "ireader.action.DOWNLOAD_ALLOW_MOBILE"
    const val ACTION_RETRY_ALL = "ireader.action.DOWNLOAD_RETRY_ALL"
    const val ACTION_CLEAR_FAILED = "ireader.action.DOWNLOAD_CLEAR_FAILED"
    const val KEY_DOWNLOAD_ACTION = "download_action"
}

class DefaultNotificationHelper(
     private val context: Context,
     private val localizeHelper: LocalizeHelper
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    private val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }
    
    companion object {
        private const val REQUEST_CODE_PAUSE = 1001
        private const val REQUEST_CODE_RESUME = 1002
        private const val REQUEST_CODE_ALLOW_MOBILE = 1003
        private const val REQUEST_CODE_OPEN_SETTINGS = 1004
        private const val REQUEST_CODE_OPEN_STORAGE = 1005
        private const val REQUEST_CODE_RETRY_ALL = 1006
        private const val REQUEST_CODE_CLEAR_FAILED = 1007
    }

    fun openBookDetailIntent(
        bookId: Long,
        sourceId: Long,
    ): Intent {
        return launchMainActivityIntent(context)
            .apply {
                action = SHORTCUTS.SHORTCUT_DETAIL
                putExtra(Args.ARG_BOOK_ID,bookId)
                putExtra(Args.ARG_SOURCE_ID,sourceId)
            }
    }

    fun openBookDetailPendingIntent(
        bookId: Long,
        sourceId: Long,
    ): PendingIntent {
        return PendingIntent.getActivity(
            context, 0, openBookDetailIntent(bookId, sourceId), legacyFlags
        )
    }

    val openDownloadIntent = launchMainActivityIntent(
        context
    )
        .apply {
            action = SHORTCUTS.SHORTCUT_DOWNLOAD
        }

    val openDownloadsPendingIntent: PendingIntent = PendingIntent.getActivity(
        context, 0, openDownloadIntent, legacyFlags
    )

    fun baseInstallerNotification(
        workManagerId: UUID,
        addCancel: Boolean = true
    ): NotificationCompat.Builder {
        val cancelDownloadIntent = WorkManager.getInstance(context)
            .createCancelPendingIntent(workManagerId)
        return NotificationCompat.Builder(
            context,
            NotificationsIds.CHANNEL_DOWNLOADER_PROGRESS
        ).apply {

            setContentTitle("Installing")
            setSmallIcon(R.drawable.ic_downloading)
            setOnlyAlertOnce(true)
            priority = NotificationCompat.PRIORITY_LOW
            setAutoCancel(true)
            setOngoing(true)
            if (addCancel) {
                addAction(
                    R.drawable.baseline_close_24,
                    localizeHelper.localize(Res.string.cancel),
                    cancelDownloadIntent
                )
            }

        }
    }

    fun baseNotificationDownloader(
        chapter: Chapter? = null,
        workManagerId: UUID,
    ): NotificationCompat.Builder {
        val cancelDownloadIntent = WorkManager.getInstance(context)
            .createCancelPendingIntent(workManagerId)
        return NotificationCompat.Builder(
            context,
            NotificationsIds.CHANNEL_DOWNLOADER_PROGRESS
        ).apply {
            chapter?.let {
                setContentTitle("Downloading ${chapter.name}")
            }

            setSmallIcon(R.drawable.ic_downloading)
            setOnlyAlertOnce(true)
            priority = NotificationCompat.PRIORITY_LOW
            setAutoCancel(false) // Don't auto-cancel on click
            setOngoing(true) // Keep notification ongoing during download
            addAction(
                R.drawable.baseline_close_24,
                localizeHelper.localize(Res.string.cancel),
                cancelDownloadIntent
            )
            setContentIntent(openDownloadsPendingIntent)
        }
    }

    fun baseCancelledNotificationDownloader(
        bookName: String? = null,
        e: Throwable,
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(
            context.applicationContext,
            NotificationsIds.CHANNEL_DOWNLOADER_ERROR
        ).apply {
            if (e.localizedMessage == localizeHelper.localize(Res.string.the_downloads_was_interrupted)) {
                setSubText(localizeHelper.localize(Res.string.the_downloads_was_cancelled))
                setContentTitle(
                    localizeHelper.localize(Res.string.download_of) + " $bookName" + localizeHelper.localize(Res.string.was_cancelled
                    )
                )
            } else {
                setContentTitle(localizeHelper.localize(Res.string.failed_to_download) + " $bookName")
                setSubText(e.localizedMessage)
            }
            setSmallIcon(R.drawable.ic_downloading)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setAutoCancel(true)
            setContentIntent(openDownloadsPendingIntent)
        }
    }

    fun cancelledNotificationDownloader(
        book: Book,
        e: Exception,
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(
            context,
            NotificationsIds.CHANNEL_DOWNLOADER_ERROR
        ).apply {
            if (e.localizedMessage == localizeHelper.localize(Res.string.the_downloads_was_interrupted)) {
                setSubText(localizeHelper.localize(Res.string.the_downloads_was_cancelled))
                setContentTitle(
                    localizeHelper.localize(Res.string.download_of) + " ${book.title}" + localizeHelper.localize(
                        Res.string.was_cancelled
                    )
                )
            } else {
                setContentTitle(localizeHelper.localize(Res.string.failed_to_download) + " $${book.title}")
                setSubText(e.localizedMessage)
            }
            setSmallIcon(R.drawable.ic_downloading)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setAutoCancel(true)
            setContentIntent(
                openBookDetailPendingIntent(
                    book.id,
                    book.sourceId
                )
            )
        }
    }

    /**
     * Creates a download notification using standard Android notification style.
     * Includes pause/resume and cancel actions.
     */
    fun createDownloadNotification(
        workManagerId: UUID,
        bookName: String,
        chapterName: String? = null,
        progress: Int = 0,
        currentChapter: Int = 0,
        totalChapters: Int = 0,
        isPaused: Boolean = false
    ): NotificationCompat.Builder {
        val cancelDownloadIntent = WorkManager.getInstance(context)
            .createCancelPendingIntent(workManagerId)
        
        val pauseIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_PAUSE,
            Intent(DownloadActions.ACTION_PAUSE).setPackage(context.packageName),
            pendingIntentFlags
        )
        
        val resumeIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_RESUME,
            Intent(DownloadActions.ACTION_RESUME).setPackage(context.packageName),
            pendingIntentFlags
        )
        
        val chaptersInfo = if (totalChapters > 0) "$currentChapter/$totalChapters" else ""
        val contentText = buildString {
            if (chapterName != null) append(chapterName)
            if (chaptersInfo.isNotEmpty()) {
                if (isNotEmpty()) append(" • ")
                append(chaptersInfo)
            }
        }
        
        return NotificationCompat.Builder(context, NotificationsIds.CHANNEL_DOWNLOADER_PROGRESS).apply {
            setContentTitle(bookName)
            setContentText(contentText.ifEmpty { if (isPaused) "Paused" else "Downloading..." })
            setSmallIcon(if (isPaused) R.drawable.baseline_pause_24 else R.drawable.ic_downloading)
            setProgress(100, progress, false)
            setOnlyAlertOnce(true)
            priority = NotificationCompat.PRIORITY_LOW
            setAutoCancel(false)
            setOngoing(!isPaused)
            setContentIntent(openDownloadsPendingIntent)
            
            // Pause/Resume action
            if (isPaused) {
                addAction(
                    R.drawable.ic_baseline_play_arrow,
                    "Resume",
                    resumeIntent
                )
            } else {
                addAction(
                    R.drawable.baseline_pause_24,
                    "Pause",
                    pauseIntent
                )
            }
            
            // Cancel action
            addAction(
                R.drawable.baseline_close_24,
                localizeHelper.localize(Res.string.cancel),
                cancelDownloadIntent
            )
        }
    }
    
    /**
     * Creates a modern download notification with custom layout that supports both themes.
     */
    fun createModernDownloadNotification(
        workManagerId: UUID,
        bookName: String,
        chapterName: String? = null,
        progress: Int = 0,
        currentChapter: Int = 0,
        totalChapters: Int = 0,
        status: DownloadNotificationStatus = DownloadNotificationStatus.DOWNLOADING,
        errorMessage: String? = null
    ): NotificationCompat.Builder {
        val cancelDownloadIntent = WorkManager.getInstance(context)
            .createCancelPendingIntent(workManagerId)
        
        val pauseIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_PAUSE,
            Intent(DownloadActions.ACTION_PAUSE).setPackage(context.packageName),
            pendingIntentFlags
        )
        
        val resumeIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_RESUME,
            Intent(DownloadActions.ACTION_RESUME).setPackage(context.packageName),
            pendingIntentFlags
        )
        
        val statusText = getStatusText(status)
        val chaptersInfo = if (totalChapters > 0) "$currentChapter/$totalChapters" else ""
        val isIndeterminate = status == DownloadNotificationStatus.QUEUED
        
        // Use standard notification style
        return NotificationCompat.Builder(context, NotificationsIds.CHANNEL_DOWNLOADER_PROGRESS).apply {
            setContentTitle(bookName)
            setContentText(chapterName ?: statusText)
            setSubText(if (chaptersInfo.isNotEmpty()) "$chaptersInfo chapters" else null)
            setSmallIcon(getStatusIcon(status))
            
            // Progress bar
            if (status == DownloadNotificationStatus.DOWNLOADING || 
                status == DownloadNotificationStatus.QUEUED) {
                setProgress(100, progress, isIndeterminate)
            }
            
            setOnlyAlertOnce(true)
            priority = NotificationCompat.PRIORITY_LOW
            setAutoCancel(status == DownloadNotificationStatus.COMPLETED || 
                         status == DownloadNotificationStatus.FAILED ||
                         status == DownloadNotificationStatus.CANCELLED)
            setOngoing(status == DownloadNotificationStatus.DOWNLOADING || 
                      status == DownloadNotificationStatus.QUEUED)
            setContentIntent(openDownloadsPendingIntent)
            
            // Accent color based on status
            color = when (status) {
                DownloadNotificationStatus.COMPLETED -> 0xFF4CAF50.toInt()
                DownloadNotificationStatus.FAILED -> 0xFFF44336.toInt()
                DownloadNotificationStatus.PAUSED -> 0xFFFF9800.toInt()
                else -> 0xFF2196F3.toInt()
            }
            
            // Actions based on status
            when (status) {
                DownloadNotificationStatus.DOWNLOADING -> {
                    addAction(
                        R.drawable.baseline_pause_24,
                        "Pause",
                        pauseIntent
                    )
                    addAction(
                        R.drawable.baseline_close_24,
                        localizeHelper.localize(Res.string.cancel),
                        cancelDownloadIntent
                    )
                }
                DownloadNotificationStatus.PAUSED -> {
                    addAction(
                        R.drawable.ic_baseline_play_arrow,
                        "Resume",
                        resumeIntent
                    )
                    addAction(
                        R.drawable.baseline_close_24,
                        localizeHelper.localize(Res.string.cancel),
                        cancelDownloadIntent
                    )
                }
                DownloadNotificationStatus.QUEUED -> {
                    addAction(
                        R.drawable.baseline_close_24,
                        localizeHelper.localize(Res.string.cancel),
                        cancelDownloadIntent
                    )
                }
                else -> { /* No actions for completed/failed/cancelled */ }
            }
        }
    }
    
    private fun getStatusText(status: DownloadNotificationStatus): String {
        return when (status) {
            DownloadNotificationStatus.QUEUED -> "Waiting in queue..."
            DownloadNotificationStatus.DOWNLOADING -> "Downloading..."
            DownloadNotificationStatus.PAUSED -> "Paused"
            DownloadNotificationStatus.COMPLETED -> "Download complete"
            DownloadNotificationStatus.FAILED -> "Download failed"
            DownloadNotificationStatus.CANCELLED -> "Download cancelled"
        }
    }
    
    private fun getStatusIcon(status: DownloadNotificationStatus): Int {
        return when (status) {
            DownloadNotificationStatus.COMPLETED -> R.drawable.baseline_check_circle_24
            DownloadNotificationStatus.FAILED -> R.drawable.baseline_error_24
            DownloadNotificationStatus.PAUSED -> R.drawable.baseline_pause_24
            else -> R.drawable.ic_downloading
        }
    }
    
    /**
     * Creates a completion notification using standard Android style.
     */
    fun createCompletionNotification(
        bookName: String,
        completedCount: Int,
        failedCount: Int = 0,
        totalSize: String? = null
    ): NotificationCompat.Builder {
        val summaryText = buildString {
            append("$completedCount chapters downloaded")
            if (failedCount > 0) append(", $failedCount failed")
            if (totalSize != null) append(" • $totalSize")
        }
        
        val isSuccess = failedCount == 0
        val titleText = if (isSuccess) "Download Complete" else "Download Finished"
        val icon = if (isSuccess) R.drawable.baseline_check_circle_24 else R.drawable.baseline_warning_24
        
        return NotificationCompat.Builder(context, NotificationsIds.CHANNEL_DOWNLOADER_COMPLETE).apply {
            setContentTitle(titleText)
            setContentText(bookName)
            setSubText(summaryText)
            setSmallIcon(icon)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setAutoCancel(true)
            setContentIntent(openDownloadsPendingIntent)
            color = if (isSuccess) 0xFF4CAF50.toInt() else 0xFFFF9800.toInt()
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════════
    // ENHANCED NOTIFICATIONS FOR MIHON-STYLE DOWNLOAD SERVICE
    // ═══════════════════════════════════════════════════════════════════════════════
    
    /**
     * Creates a parallel download progress notification showing multiple active downloads.
     * Shows per-source progress when multiple sources are downloading.
     * 
     * @param workManagerId WorkManager UUID for cancel action
     * @param activeDownloads List of currently downloading items
     * @param queuedCount Number of items waiting in queue
     * @param completedCount Number of completed downloads
     * @param failedCount Number of failed downloads
     * @param isPaused Whether downloads are paused
     */
    fun createParallelDownloadNotification(
        workManagerId: UUID,
        activeDownloads: List<Download>,
        queuedCount: Int = 0,
        completedCount: Int = 0,
        failedCount: Int = 0,
        isPaused: Boolean = false
    ): NotificationCompat.Builder {
        val cancelDownloadIntent = WorkManager.getInstance(context)
            .createCancelPendingIntent(workManagerId)
        
        val pauseIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_PAUSE,
            Intent(DownloadActions.ACTION_PAUSE).setPackage(context.packageName),
            pendingIntentFlags
        )
        
        val resumeIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_RESUME,
            Intent(DownloadActions.ACTION_RESUME).setPackage(context.packageName),
            pendingIntentFlags
        )
        
        val activeCount = activeDownloads.size
        val totalProgress = if (activeDownloads.isNotEmpty()) {
            activeDownloads.sumOf { it.progress } / activeDownloads.size
        } else 0
        
        // Group by source for per-source display
        val downloadsBySource = activeDownloads.groupBy { it.sourceId }
        
        // Build title
        val title = when {
            isPaused -> "Downloads Paused"
            activeCount == 0 && queuedCount > 0 -> "Preparing downloads..."
            activeCount == 1 -> activeDownloads.first().bookTitle
            activeCount > 1 -> "$activeCount downloads active"
            else -> "Downloads"
        }
        
        // Build content text
        val contentText = when {
            isPaused -> "$queuedCount queued, $completedCount completed"
            activeCount == 1 -> activeDownloads.first().chapterName
            activeCount > 1 && downloadsBySource.size > 1 -> {
                // Multiple sources - show source count
                "${downloadsBySource.size} sources • $queuedCount queued"
            }
            activeCount > 1 -> {
                // Single source, multiple downloads
                "$queuedCount queued • $completedCount completed"
            }
            queuedCount > 0 -> "$queuedCount chapters waiting"
            else -> "Ready"
        }
        
        // Build sub text with stats
        val subText = buildString {
            if (completedCount > 0) append("✓$completedCount ")
            if (failedCount > 0) append("✗$failedCount ")
            if (queuedCount > 0 && activeCount > 0) append("⏳$queuedCount")
        }.trim().ifEmpty { null }
        
        // Build expanded style for multiple downloads
        val inboxStyle = if (activeCount > 1) {
            NotificationCompat.InboxStyle().also { style ->
                activeDownloads.take(5).forEach { download ->
                    val progressStr = "${download.progress}%"
                    style.addLine("${download.bookTitle}: ${download.chapterName} ($progressStr)")
                }
                if (activeCount > 5) {
                    style.addLine("...and ${activeCount - 5} more")
                }
                style.setSummaryText("$activeCount active, $queuedCount queued")
            }
        } else null
        
        return NotificationCompat.Builder(context, NotificationsIds.CHANNEL_DOWNLOADER_PROGRESS).apply {
            setContentTitle(title)
            setContentText(contentText)
            subText?.let { setSubText(it) }
            setSmallIcon(if (isPaused) R.drawable.baseline_pause_24 else R.drawable.ic_downloading)
            
            // Progress bar
            if (!isPaused && activeCount > 0) {
                setProgress(100, totalProgress, false)
            }
            
            // Expanded style
            inboxStyle?.let { setStyle(it) }
            
            setOnlyAlertOnce(true)
            priority = NotificationCompat.PRIORITY_LOW
            setAutoCancel(false)
            setOngoing(!isPaused)
            setContentIntent(openDownloadsPendingIntent)
            
            // Color based on state
            color = when {
                isPaused -> 0xFFFF9800.toInt() // Orange
                failedCount > 0 -> 0xFFF44336.toInt() // Red
                else -> 0xFF2196F3.toInt() // Blue
            }
            
            // Actions
            if (isPaused) {
                addAction(R.drawable.ic_baseline_play_arrow, "Resume", resumeIntent)
            } else {
                addAction(R.drawable.baseline_pause_24, "Pause", pauseIntent)
            }
            addAction(R.drawable.baseline_close_24, localizeHelper.localize(Res.string.cancel), cancelDownloadIntent)
        }
    }
    
    /**
     * Creates a WiFi-only warning notification when downloads are paused due to mobile data.
     * Includes action to allow mobile data temporarily.
     */
    fun createWifiOnlyWarningNotification(): NotificationCompat.Builder {
        val allowMobileIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_ALLOW_MOBILE,
            Intent(DownloadActions.ACTION_ALLOW_MOBILE_DATA).setPackage(context.packageName),
            pendingIntentFlags
        )
        
        val openSettingsIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_OPEN_SETTINGS,
            Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
            pendingIntentFlags
        )
        
        return NotificationCompat.Builder(context, NotificationsIds.CHANNEL_DOWNLOADER_PROGRESS).apply {
            setContentTitle("Downloads Paused")
            setContentText("Waiting for WiFi connection")
            setSubText("WiFi-only mode is enabled")
            setSmallIcon(R.drawable.baseline_wifi_off_24)
            setOnlyAlertOnce(true)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setAutoCancel(false)
            setOngoing(true)
            setContentIntent(openDownloadsPendingIntent)
            color = 0xFFFF9800.toInt() // Orange
            
            // Action to allow mobile data temporarily
            addAction(
                R.drawable.baseline_signal_cellular_alt_24,
                "Use Mobile Data",
                allowMobileIntent
            )
            
            // Action to open WiFi settings
            addAction(
                R.drawable.baseline_settings_24,
                "WiFi Settings",
                openSettingsIntent
            )
        }
    }
    
    /**
     * Creates a disk space warning notification when downloads are paused due to low storage.
     * Includes action to open storage settings.
     */
    fun createDiskSpaceWarningNotification(
        availableSpaceMb: Long,
        requiredSpaceMb: Long = 200
    ): NotificationCompat.Builder {
        val openStorageIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_OPEN_STORAGE,
            Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
            pendingIntentFlags
        )
        
        return NotificationCompat.Builder(context, NotificationsIds.CHANNEL_DOWNLOADER_ERROR).apply {
            setContentTitle("Downloads Paused")
            setContentText("Not enough storage space")
            setSubText("${availableSpaceMb}MB available, ${requiredSpaceMb}MB required")
            setSmallIcon(R.drawable.baseline_storage_24)
            setOnlyAlertOnce(true)
            priority = NotificationCompat.PRIORITY_HIGH
            setAutoCancel(false)
            setOngoing(true)
            setContentIntent(openDownloadsPendingIntent)
            color = 0xFFF44336.toInt() // Red
            
            // Action to open storage settings
            addAction(
                R.drawable.baseline_settings_24,
                "Manage Storage",
                openStorageIntent
            )
        }
    }
    
    /**
     * Creates a retry notification for failed downloads.
     * @param failedCount Number of failed downloads
     * @param errorSummary Brief summary of errors
     */
    fun createRetryNotification(
        failedCount: Int,
        errorSummary: String? = null
    ): NotificationCompat.Builder {
        val retryAllIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_RETRY_ALL,
            Intent(DownloadActions.ACTION_RETRY_ALL).setPackage(context.packageName),
            pendingIntentFlags
        )
        
        val clearFailedIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_CLEAR_FAILED,
            Intent(DownloadActions.ACTION_CLEAR_FAILED).setPackage(context.packageName),
            pendingIntentFlags
        )
        
        return NotificationCompat.Builder(context, NotificationsIds.CHANNEL_DOWNLOADER_ERROR).apply {
            setContentTitle("$failedCount downloads failed")
            setContentText(errorSummary ?: "Tap to view details")
            setSmallIcon(R.drawable.baseline_error_24)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setAutoCancel(true)
            setContentIntent(openDownloadsPendingIntent)
            color = 0xFFF44336.toInt() // Red
            
            // Retry all action
            addAction(
                R.drawable.baseline_refresh_24,
                "Retry All",
                retryAllIntent
            )
            
            // Clear failed action
            addAction(
                R.drawable.baseline_close_24,
                "Clear",
                clearFailedIntent
            )
        }
    }
    
    /**
     * Shows a notification.
     */
    fun showNotification(id: Int, notification: NotificationCompat.Builder) {
        try {
            notificationManager.notify(id, notification.build())
        } catch (e: SecurityException) {
            // Notification permission not granted
        }
    }
    
    /**
     * Cancels a notification.
     */
    fun cancelNotification(id: Int) {
        notificationManager.cancel(id)
    }
    
    /**
     * Download notification status enum.
     */
    enum class DownloadNotificationStatus {
        QUEUED,
        DOWNLOADING,
        PAUSED,
        COMPLETED,
        FAILED,
        CANCELLED
    }
}