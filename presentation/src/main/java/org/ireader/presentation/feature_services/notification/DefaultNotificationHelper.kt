package org.ireader.presentation.feature_services.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
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
import androidx.work.*
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import org.ireader.core.R
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.infinity.feature_services.flags
import org.ireader.presentation.MainActivity
import org.ireader.presentation.feature_ttl.TTSService
import org.ireader.presentation.feature_ttl.TTSService.Companion.CANCEL
import org.ireader.presentation.feature_ttl.TTSService.Companion.NEXT_PAR
import org.ireader.presentation.feature_ttl.TTSService.Companion.PAUSE
import org.ireader.presentation.feature_ttl.TTSService.Companion.PLAY
import org.ireader.presentation.feature_ttl.TTSService.Companion.PREV_PAR
import org.ireader.presentation.feature_ttl.TTSService.Companion.SKIP_NEXT
import org.ireader.presentation.feature_ttl.TTSService.Companion.SKIP_PREV
import timber.log.Timber
import java.util.*
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
        return Intent(
            Intent.ACTION_VIEW,
            "https://www.ireader.org/book_detail_route/$bookId/$sourceId".toUri(),
            applicationContext,
            MainActivity::class.java
        )
    }

    fun openBookDetailPendingIntent(
        bookId: Long,
        sourceId: Long,
    ): PendingIntent {
        return PendingIntent.getActivity(
            applicationContext, 0, openBookDetailIntent(bookId, sourceId), flags
        )
    }

    val openDownloadIntent = Intent(
        Intent.ACTION_VIEW,
        "www.ireader/downloader_route".toUri(),
        applicationContext,
        MainActivity::class.java
    )


    val openDownloadsPendingIntent: PendingIntent = PendingIntent.getActivity(
        applicationContext, 0, openDownloadIntent, flags
    )

    fun baseNotificationDownloader(
        chapter: Chapter,
        book: Book,
        workManagerId: UUID,
    ): NotificationCompat.Builder {
        val cancelDownloadIntent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(workManagerId)
        return NotificationCompat.Builder(applicationContext,
            Notifications.CHANNEL_DOWNLOADER_PROGRESS).apply {
            setContentTitle("Downloading ${book.title}")
            setSmallIcon(R.drawable.ic_downloading)
            setOnlyAlertOnce(true)
            priority = NotificationCompat.PRIORITY_LOW
            setAutoCancel(true)
            setOngoing(true)
            addAction(R.drawable.baseline_close_24, "Cancel", cancelDownloadIntent)
            setContentIntent(openDownloadsPendingIntent)
        }
    }

    fun updateDownloaderNotification(
        chapter: Chapter,
        book: Book,
        workManagerId: UUID,
        index: Int,
        maxIndex: Int,
    ) {
        val notification = baseNotificationDownloader(chapter, book, workManagerId).apply {
            setContentText(chapter.title)
            setSubText(index.toString())
            setProgress(maxIndex, index, false)
        }.build()

        NotificationManagerCompat.from(applicationContext)
            .notify(Notifications.ID_DOWNLOAD_CHAPTER_PROGRESS, notification)

    }


    val skipPrev = PendingIntent.getBroadcast(
        applicationContext.applicationContext,
        SKIP_PREV,
        Intent(applicationContext, TextReaderBroadcastReceiver::class.java).apply {
            putExtra("PLAYER", SKIP_PREV)
        },
        pendingIntentFlags
    )
    val rewind = PendingIntent.getBroadcast(
        applicationContext.applicationContext,
        PREV_PAR,
        Intent(applicationContext.applicationContext,
            TextReaderBroadcastReceiver::class.java).apply {
            putExtra("PLAYER", PREV_PAR)
        },
        pendingIntentFlags
    )
    val pause = PendingIntent.getBroadcast(
        applicationContext.applicationContext,
        PAUSE,
        Intent(applicationContext, TextReaderBroadcastReceiver::class.java).apply {
            putExtra("PLAYER", PAUSE)
        },
        pendingIntentFlags
    )
    val play = PendingIntent.getBroadcast(
        applicationContext.applicationContext,
        PLAY,
        Intent(applicationContext, TextReaderBroadcastReceiver::class.java).apply {
            putExtra("PLAYER", PLAY)
        },
        pendingIntentFlags
    )
    val next = PendingIntent.getBroadcast(
        applicationContext.applicationContext,
        NEXT_PAR,
        Intent(applicationContext, TextReaderBroadcastReceiver::class.java).apply {
            putExtra("PLAYER", NEXT_PAR)
        },
        pendingIntentFlags
    )
    val skipNext = PendingIntent.getBroadcast(
        applicationContext.applicationContext,
        SKIP_NEXT,
        Intent(applicationContext, TextReaderBroadcastReceiver::class.java).apply {
            putExtra("PLAYER", SKIP_NEXT)
        },
        pendingIntentFlags
    )
    val cancelMediaPlater = PendingIntent.getBroadcast(
        applicationContext.applicationContext,
        CANCEL,
        Intent(applicationContext, TextReaderBroadcastReceiver::class.java).apply {
            putExtra("PLAYER", CANCEL)
        },
        pendingIntentFlags
    )

    fun openReaderScreenIntent(
        chapter: Chapter,
        book: Book,
    ): PendingIntent = PendingIntent.getActivity(
        applicationContext.applicationContext,
        5,
        Intent(
            Intent.ACTION_VIEW,
            "https://www.ireader.org/reader_screen_route/${book.id}/${chapter.id}/${book.sourceId}".toUri(),
            applicationContext,
            MainActivity::class.java
        ),
        flags
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
                else -> "${progress}/${chapter.content.lastIndex}"
            }
        mediaSessionCompat.apply {
            isActive = true
            setMetadata(MediaMetadataCompat.Builder().apply {
                putText(MediaMetadata.METADATA_KEY_TITLE, chapter.title)
            }.build())
            val stateBuilder = PlaybackStateCompat.Builder()
            stateBuilder.addCustomAction(
                PlaybackStateCompat.CustomAction.Builder(
                    "PLAY",
                    "Play",
                    R.drawable.ic_baseline_play_arrow
                ).build()
            )
            stateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)
            setPlaybackState(PlaybackStateCompat.Builder().apply {
                setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_STOP or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)

            }.build())
        }
        return NotificationCompat.Builder(applicationContext,
            Notifications.CHANNEL_TEXT_READER_PROGRESS).apply {
            setContentTitle(chapter.title)
            setContentText(contentText)
            setSmallIcon(org.ireader.core.R.drawable.ic_infinity)
            setOnlyAlertOnce(true)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setLargeIcon(applicationContext, book.cover)
            priority = NotificationCompat.PRIORITY_LOW

            setContentIntent(openReaderScreenIntent(chapter, book))
            addAction(R.drawable.ic_baseline_skip_previous,
                "Previous Chapter",
                skipPrev)
            addAction(
                R.drawable.ic_baseline_fast_rewind,
                "Previous Paragraph",
                rewind)

            if (playing) {
                addAction(R.drawable.ic_baseline_pause, "Pause", pause)
            } else {
                addAction(R.drawable.ic_baseline_play_arrow, "Start", play)

            }
            addAction(R.drawable.ic_baseline_fast_forward, "Next Paragraph", next)
            addAction(R.drawable.ic_baseline_skip_next, "Next Chapter", skipNext)
            setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setCancelButtonIntent(cancelMediaPlater)
                .setMediaSession(mediaSessionCompat.sessionToken)
                .setShowActionsInCompactView(1, 2, 3)
            )
            setSubText(book.title)

            //setColorized(true)
            //setAutoCancel(true)
            setOngoing(false)
        }


    }
}

@AndroidEntryPoint()
class TextReaderBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var state: NotificationStates

    val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    lateinit var ttsWork: OneTimeWorkRequest
    override fun onReceive(context: Context, intent: Intent) {
        intent.getIntExtra("PLAYER", -1).let { command ->
            Timber.e("Command is : $command")

            ttsWork = OneTimeWorkRequestBuilder<TTSService>().apply {
                setInputData(
                    Data.Builder().apply {
                        putInt(TTSService.COMMAND, command)
                    }.build()
                )
                addTag(TTSService.TTS_SERVICE_NAME)

            }.build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                TTSService.TTS_SERVICE_NAME,
                ExistingWorkPolicy.REPLACE,
                ttsWork
            )
        }
    }

}

@Singleton
class NotificationStates @Inject constructor() {
    val mediaPlayerNotification = MutableSharedFlow<Int>()
}

