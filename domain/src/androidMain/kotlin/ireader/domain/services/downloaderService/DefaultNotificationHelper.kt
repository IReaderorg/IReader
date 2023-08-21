package ireader.domain.services.downloaderService

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.notification.NotificationsIds
import ireader.domain.notification.legacyFlags
import ireader.domain.services.tts_service.Player
import ireader.domain.services.tts_service.media_player.TTSService
import ireader.domain.utils.extensions.launchMainActivityIntent
import ireader.i18n.Args
import ireader.i18n.LocalizeHelper
import ireader.i18n.R
import ireader.i18n.SHORTCUTS
import java.util.UUID


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

    fun openBookDetailIntent(
        bookId: Long,
        sourceId: Long,
    ): Intent {
        return launchMainActivityIntent(context)
            .apply {
                action = SHORTCUTS.SHORTCUT_DETAIL
                putExtra(Args.ARG_BOOK_ID, bookId)
                putExtra(Args.ARG_SOURCE_ID, sourceId)
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
                    context.resources.getString(R.string.cancel),
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
            setAutoCancel(true)
            setOngoing(true)
            addAction(
                R.drawable.baseline_close_24,
                localizeHelper.localize() { xml ->
                    xml.cancel
                },
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
            if (e.localizedMessage == localizeHelper.localize() { xml ->
                    xml.theDownloadsWasInterrupted
                }) {
                setSubText(localizeHelper.localize() { xml ->
                    xml.downloadsWasCancelled
                })
                setContentTitle(
                    localizeHelper.localize() { xml ->
                        xml.downloadOf
                    } + " $bookName" + localizeHelper.localize() { xml ->
                        xml.downloadsWasCancelled
                    }
                )
            } else {
                setContentTitle(localizeHelper.localize() { xml ->
                    xml.failedToDownload
                } + " $bookName")
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
            if (e.localizedMessage == localizeHelper.localize() { xml ->
                    xml.theDownloadsWasInterrupted
                }) {
                setSubText(localizeHelper.localize() { xml ->
                    xml.downloadsWasCancelled
                })
                setContentTitle(
                    localizeHelper.localize() { xml ->
                        xml.downloadOf
                    } + " ${book.title}" + localizeHelper.localize() { xml ->
                        xml.wasCancelled
                    }
                )
            } else {
                setContentTitle(localizeHelper.localize() { xml ->
                    xml.failedToDownload
                } + " $${book.title}")
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

    val skipPrev = PendingIntent.getBroadcast(
        context,
        Player.SKIP_PREV,
        Intent(
            context,
            TTSService::class.java
        ).apply {
            putExtra(Player.KEY, Player.SKIP_PREV)
        },
        pendingIntentFlags
    )
    val rewind = PendingIntent.getBroadcast(
        context,
        Player.PREV_PAR,
        Intent(
            context.applicationContext,
            TTSService::class.java
        ).apply {
            putExtra(Player.KEY, Player.PREV_PAR)
        },
        pendingIntentFlags
    )
    val pause = PendingIntent.getBroadcast(
        context,
        Player.PAUSE,
        Intent(
            context,
            TTSService::class.java
        ).apply {
            putExtra(Player.KEY, Player.PAUSE)
        },
        pendingIntentFlags
    )
    val play = PendingIntent.getBroadcast(
        context,
        Player.PLAY,
        Intent(
            context,
            TTSService::class.java
        ).apply {
            putExtra(Player.KEY, Player.PLAY)
        },
        pendingIntentFlags
    )
    val next = PendingIntent.getBroadcast(
        context,
        Player.NEXT_PAR,
        Intent(
            context,
            TTSService::class.java
        ).apply {
            putExtra(Player.KEY, Player.NEXT_PAR)
        },
        pendingIntentFlags
    )
    val skipNext = PendingIntent.getBroadcast(
        context,
        Player.SKIP_NEXT,
        Intent(
            context,
            TTSService::class.java
        ).apply {
            putExtra(Player.KEY, Player.SKIP_NEXT)
        },
        pendingIntentFlags
    )
    val cancelMediaPlater = PendingIntent.getBroadcast(
        context,
        Player.CANCEL,
        Intent(
            context,
            TTSService::class.java
        ).apply {
            putExtra(Player.KEY, Player.CANCEL)
        },
        pendingIntentFlags
    )
}