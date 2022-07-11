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
import org.ireader.common_resources.string
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
    @ApplicationContext private val context: Context,
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
        return org.ireader.common_extensions.launchMainActivityIntent(context)
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
            context, 0, openBookDetailIntent(bookId, sourceId), flags
        )
    }

    val openDownloadIntent = org.ireader.common_extensions.launchMainActivityIntent(
        context
    )
        .apply {
            action = Intent.ACTION_VIEW
            data = "https://www.ireader/downloader_route".toUri()
        }

    val openDownloadsPendingIntent: PendingIntent = PendingIntent.getActivity(
        context, 0, openDownloadIntent, flags
    )

    fun baseNotificationDownloader(
        chapter: Chapter? = null,
        workManagerId: UUID,
    ): NotificationCompat.Builder {
        val cancelDownloadIntent = WorkManager.getInstance(context)
            .createCancelPendingIntent(workManagerId)
        return NotificationCompat.Builder(
            context,
            Notifications.CHANNEL_DOWNLOADER_PROGRESS
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
                context.resources.getString(R.string.cancel),
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
            Notifications.CHANNEL_DOWNLOADER_ERROR
        ).apply {
            if (e.localizedMessage == context.string(R.string.the_downloads_was_interrupted)) {
                setSubText(context.string(R.string.the_downloads_was_cancelled))
                setContentTitle(
                    context.string(R.string.download_of) + " $bookName" + context.string(
                        R.string.was_cancelled
                    )
                )
            } else {
                setContentTitle(context.string(R.string.failed_to_download) + " $bookName")
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
            Notifications.CHANNEL_DOWNLOADER_ERROR
        ).apply {
            if (e.localizedMessage == context.string(R.string.the_downloads_was_interrupted)) {
                setSubText(context.string(R.string.the_downloads_was_cancelled))
                setContentTitle(
                    context.string(R.string.download_of) + " ${book.title}" + context.string(
                        R.string.was_cancelled
                    )
                )
            } else {
                setContentTitle(context.string(R.string.failed_to_download) + " $${book.title}")
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
                isLoading -> context.string(R.string.loading)
                isError -> context.string(R.string.error)
                else -> "$progress/${chapter.content.lastIndex}"
            }
        mediaSessionCompat.apply {
            isActive = true
            setMetadata(
                MediaMetadataCompat.Builder().apply {
                    putText(MediaMetadata.METADATA_KEY_TITLE, chapter.name)
                }.build()
            )
            val stateBuilder = PlaybackStateCompat.Builder()
            stateBuilder.addCustomAction(
                PlaybackStateCompat.CustomAction.Builder(
                    "PLAY",
                    context.string(R.string.play),
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
            context,
            Notifications.CHANNEL_TTS
        ).apply {
            setContentTitle(chapter.name)
            setContentText(contentText)
            setSmallIcon(org.ireader.core.R.drawable.ic_infinity)
            setOnlyAlertOnce(true)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setLargeIcon(context, book.cover)
            priority = NotificationCompat.PRIORITY_LOW

            setContentIntent(openReaderScreenIntent(chapter, book, progress))
            addAction(
                R.drawable.ic_baseline_skip_previous,
                context.string(R.string.previous_chapter),
                skipPrev
            )
            addAction(
                R.drawable.ic_baseline_fast_rewind,
                context.string(R.string.previous_paragraph),
                rewind
            )

            if (playing) {
                addAction(R.drawable.ic_baseline_pause, context.string(R.string.pause), pause)
            } else {
                addAction(R.drawable.ic_baseline_play_arrow, context.string(R.string.play), play)
            }
            addAction(
                R.drawable.ic_baseline_fast_forward,
                context.string(R.string.next_paragraph),
                next
            )
            addAction(
                R.drawable.ic_baseline_skip_next,
                context.string(R.string.next_chapter),
                skipNext
            )
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
        context.applicationContext,
        5,
        org.ireader.common_extensions.launchMainActivityIntent(context)
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
