package org.ireader.domain.feature_services.notification

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
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.ireader.core.R
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.infinity.feature_services.flags
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
            "ireader/book_detail_route/$bookId/$sourceId".toUri(),
            applicationContext,
            applicationContext::class.java
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
        "ireader/downloader_route".toUri(),
        applicationContext,
        applicationContext::class.java
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
        0,
        Intent(applicationContext, TextReaderBroadcastReceiver::class.java).apply {
            putExtra("PLAYER", 1)
        },
        pendingIntentFlags
    )
    val rewind = PendingIntent.getBroadcast(
        applicationContext.applicationContext,
        1,
        Intent(applicationContext.applicationContext,
            TextReaderBroadcastReceiver::class.java).apply {
            putExtra("PLAYER", 2)
        },
        pendingIntentFlags
    )
    val pause = PendingIntent.getBroadcast(
        applicationContext.applicationContext,
        2,
        Intent(applicationContext, TextReaderBroadcastReceiver::class.java).apply {
            putExtra("PLAYER", 3)
        },
        pendingIntentFlags
    )
    val next = PendingIntent.getBroadcast(
        applicationContext.applicationContext,
        3,
        Intent(applicationContext, TextReaderBroadcastReceiver::class.java).apply {
            putExtra("PLAYER", 4)
        },
        pendingIntentFlags
    )
    val skipNext = PendingIntent.getBroadcast(
        applicationContext.applicationContext,
        4,
        Intent(applicationContext, TextReaderBroadcastReceiver::class.java).apply {
            putExtra("PLAYER", 5)
        },
        pendingIntentFlags
    )
    val cancelMediaPlater = PendingIntent.getBroadcast(
        applicationContext.applicationContext,
        5,
        Intent(applicationContext, TextReaderBroadcastReceiver::class.java).apply {
            putExtra("PLAYER", 6)
        },
        pendingIntentFlags
    )


    suspend fun basicPlayingTextReaderNotification(
        chapter: Chapter,
        book: Book,
        playing: Boolean,
        progress: Int,
        mediaSessionCompat: MediaSessionCompat,
    ): NotificationCompat.Builder {
        mediaSessionCompat.apply {
            isActive = true
            setMetadata(MediaMetadataCompat.Builder().apply {
                putText(MediaMetadata.METADATA_KEY_TITLE, chapter.title)
            }.build())
            setPlaybackState(PlaybackStateCompat.Builder().apply {
                setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_STOP or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)

            }.build())
        }
        return NotificationCompat.Builder(applicationContext,
            Notifications.CHANNEL_TEXT_READER_PROGRESS).apply {
            setContentTitle(chapter.title)
            setContentText("${progress}/${chapter.content.size}")
            setSmallIcon(org.ireader.core.R.drawable.ic_infinity)
            setOnlyAlertOnce(true)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setLargeIcon(applicationContext, book.cover)
            priority = NotificationCompat.PRIORITY_LOW

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
                addAction(R.drawable.ic_baseline_play_arrow, "Start", pause)

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

    override fun onReceive(p0: Context?, intent: Intent?) {
        intent?.getIntExtra("PLAYER", -1)?.let {
            scope.launch {
                state.mediaPlayerNotification.emit(it)
            }
        }


    }

}

@Singleton
class NotificationStates @Inject constructor() {
    val mediaPlayerNotification = MutableSharedFlow<Int>()
}

