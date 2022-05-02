package org.ireader.domain.services.downloaderService

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.Chapter
import org.ireader.core.R
import org.ireader.domain.notification.Notifications
import org.ireader.domain.notification.flags
import org.ireader.domain.notification.setLargeIcon
import org.ireader.domain.services.tts_service.Player
import org.ireader.domain.services.tts_service.media_player.TTSService
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultNotificationHelper @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
) {
    private val notificationManager = NotificationManagerCompat.from(applicationContext)

    private val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }

    fun openBookDetailIntent(
        bookId: Long,
        sourceId: Long,
    ): Intent {
        return org.ireader.common_extensions.launchMainActivityIntent(applicationContext)
            .apply {
                action = Intent.ACTION_VIEW
                data = "https://www.ireader.org/book_detail_route/$bookId/$sourceId".toUri()
            }
    }

    fun openBookDetailPendingIntent(
        bookId: Long,
        sourceId: Long,
    ): PendingIntent {
        return PendingIntent.getActivity(
            applicationContext, 0, openBookDetailIntent(bookId, sourceId), flags
        )
    }

    val openDownloadIntent = org.ireader.common_extensions.launchMainActivityIntent(
        applicationContext
    )
        .apply {
            action = Intent.ACTION_VIEW
            data = "https://www.ireader/downloader_route".toUri()
        }

    val openDownloadsPendingIntent: PendingIntent = PendingIntent.getActivity(
        applicationContext, 0, openDownloadIntent, flags
    )

    fun baseNotificationDownloader(
        chapter: Chapter? = null,
        workManagerId: UUID,
    ): NotificationCompat.Builder {
        val cancelDownloadIntent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(workManagerId)
        return NotificationCompat.Builder(
            applicationContext,
            Notifications.CHANNEL_DOWNLOADER_PROGRESS
        ).apply {
            chapter?.let {
                setContentTitle("Downloading ${chapter.title}")
            }
            setSmallIcon(R.drawable.ic_downloading)
            setOnlyAlertOnce(true)
            priority = NotificationCompat.PRIORITY_LOW
            setAutoCancel(true)
            setOngoing(true)
            addAction(R.drawable.baseline_close_24, "Cancel", cancelDownloadIntent)
            setContentIntent(openDownloadsPendingIntent)
        }
    }

    fun baseCancelledNotificationDownloader(
        bookName: String? = null,
        e: Throwable,
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(
            applicationContext.applicationContext,
            Notifications.CHANNEL_DOWNLOADER_ERROR
        ).apply {
            if (e.localizedMessage == "Job was cancelled") {
                setSubText("Download was cancelled")
                setContentTitle("Download of $bookName was canceled.")
            } else {
                setContentTitle("Failed to download $bookName")
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
            applicationContext,
            Notifications.CHANNEL_DOWNLOADER_ERROR
        ).apply {
            if (e.localizedMessage == "Job was cancelled") {
                setSubText("Download was cancelled")
                setContentTitle("Download of ${book.title} was canceled.")
            } else {
                setContentTitle("Failed to download ${book.title}")
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

//    fun updateDownloaderNotification(
//        chapter: Chapter,
//        book: Book,
//        workManagerId: UUID,
//        index: Int,
//        maxIndex: Int,
//    ) {
//        val notification = baseNotificationDownloader(chapter, book, workManagerId).apply {
//            setContentText(chapter.title)
//            setSubText(index.toString())
//            setProgress(maxIndex, index, false)
//        }.build()
//
//        NotificationManagerCompat.from(applicationContext)
//            .notify(Notifications.ID_DOWNLOAD_CHAPTER_PROGRESS, notification)
//
//    }

    val skipPrev = PendingIntent.getBroadcast(
        applicationContext,
        Player.SKIP_PREV,
        Intent(
            applicationContext,
            TTSService::class.java
        ).apply {
            putExtra("PLAYER", Player.SKIP_PREV)
        },
        pendingIntentFlags
    )
    val rewind = PendingIntent.getBroadcast(
        applicationContext,
        Player.PREV_PAR,
        Intent(
            applicationContext.applicationContext,
                     TTSService::class.java
        ).apply {
            putExtra("PLAYER", Player.PREV_PAR)
        },
        pendingIntentFlags
    )
    val pause = PendingIntent.getBroadcast(
        applicationContext,
        Player.PAUSE,
        Intent(
            applicationContext,
                     TTSService::class.java
        ).apply {
            putExtra("PLAYER", Player.PAUSE)
        },
        pendingIntentFlags
    )
    val play = PendingIntent.getBroadcast(
        applicationContext,
        Player.PLAY,
        Intent(
            applicationContext,
                     TTSService::class.java
        ).apply {
            putExtra("PLAYER", Player.PLAY)
        },
        pendingIntentFlags
    )
    val next = PendingIntent.getBroadcast(
        applicationContext,
        Player.NEXT_PAR,
        Intent(
            applicationContext,
                     TTSService::class.java
        ).apply {
            putExtra("PLAYER", Player.NEXT_PAR)
        },
        pendingIntentFlags
    )
    val skipNext = PendingIntent.getBroadcast(
        applicationContext,
        Player.SKIP_NEXT,
        Intent(
            applicationContext,
                     TTSService::class.java
        ).apply {
            putExtra("PLAYER", Player.SKIP_NEXT)
        },
        pendingIntentFlags
    )
    val cancelMediaPlater = PendingIntent.getBroadcast(
        applicationContext,
        Player.CANCEL,
        Intent(
            applicationContext,
                     TTSService::class.java
        ).apply {
            putExtra("PLAYER", Player.CANCEL)
        },
        pendingIntentFlags
    )

    suspend fun basicPlayingTextReaderNotification(
        chapter: Chapter,
        book: Book,
        playing: Boolean,
        progress: Int,
        mediaSessionCompat: MediaSessionCompat,
        isLoading: Boolean = false,
        isError: Boolean = false,
    ): NotificationCompat.Builder {
        val contentText =
            when {
                isLoading -> "Loading..."
                isError -> "ERROR"
                else -> "$progress/${chapter.content.lastIndex}"
            }
        mediaSessionCompat.apply {
            isActive = true
            setMetadata(
                MediaMetadataCompat.Builder().apply {
                    putText(MediaMetadata.METADATA_KEY_TITLE, chapter.title)
                }.build()
            )
            val stateBuilder = PlaybackStateCompat.Builder()
            stateBuilder.addCustomAction(
                PlaybackStateCompat.CustomAction.Builder(
                    "PLAY",
                    "Play",
                    R.drawable.ic_baseline_play_arrow
                ).build()
            )
            stateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)
            setPlaybackState(
                PlaybackStateCompat.Builder().apply {
                    setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_STOP or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                }.build()
            )
        }
        return NotificationCompat.Builder(
            applicationContext,
            Notifications.CHANNEL_TTS
        ).apply {
            setContentTitle(chapter.title)
            setContentText(contentText)
            setSmallIcon(org.ireader.core.R.drawable.ic_infinity)
            setOnlyAlertOnce(true)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setLargeIcon(applicationContext, book.cover)
            priority = NotificationCompat.PRIORITY_LOW

            setContentIntent(openReaderScreenIntent(chapter, book, progress))
            addAction(
                R.drawable.ic_baseline_skip_previous,
                "Previous Chapter",
                skipPrev
            )
            addAction(
                R.drawable.ic_baseline_fast_rewind,
                "Previous Paragraph",
                rewind
            )

            if (playing) {
                addAction(R.drawable.ic_baseline_pause, "Pause", pause)
            } else {
                addAction(R.drawable.ic_baseline_play_arrow, "Start", play)
            }
            addAction(R.drawable.ic_baseline_fast_forward, "Next Paragraph", next)
            addAction(R.drawable.ic_baseline_skip_next, "Next Chapter", skipNext)
            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setCancelButtonIntent(cancelMediaPlater)
                    .setMediaSession(mediaSessionCompat.sessionToken)
                    .setShowActionsInCompactView(1, 2, 3)
            )
            setSubText(book.title)

            // setColorized(true)
            // setAutoCancel(true)
            setOngoing(false)
        }
    }

    fun buildDownloadScreenDeepLink(
        bookId: Long,
        sourceId: Long,
        chapterId: Long,
        readingParagraph: Long,
        voiceMode: Long,
    ): String {
        return "https://www.ireader.org/reader_screen_route/$bookId/$chapterId/$sourceId/$readingParagraph/$voiceMode"
    }

    private fun openReaderScreenIntent(
        chapter: Chapter,
        book: Book,
        currentReadingParagraph: Int = 0,
    ): PendingIntent = PendingIntent.getActivity(
        applicationContext.applicationContext,
        5,
        org.ireader.common_extensions.launchMainActivityIntent(applicationContext)
            .apply {
                action = Intent.ACTION_VIEW
                data = buildDownloadScreenDeepLink(
                    bookId = book.id,
                    chapterId = chapter.id,
                    sourceId = book.sourceId,
                    readingParagraph = currentReadingParagraph.toLong(),
                    voiceMode = 1L
                ).toUri()
            },
        flags
    )
}

@Singleton
class NotificationStates @Inject constructor() {
    val mediaPlayerNotification = MutableSharedFlow<Int>()
}
