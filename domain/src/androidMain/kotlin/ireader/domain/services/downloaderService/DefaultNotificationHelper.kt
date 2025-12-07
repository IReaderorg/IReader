package ireader.domain.services.downloaderService

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import ireader.i18n.R
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