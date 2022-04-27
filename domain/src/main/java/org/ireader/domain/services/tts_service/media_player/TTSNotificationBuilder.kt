package org.ireader.domain.services.tts_service.media_player


import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.media.session.MediaButtonReceiver
import org.ireader.core.utils.K
import org.ireader.domain.R
import org.ireader.domain.feature_service.io.BookCover
import org.ireader.domain.notification.Notifications
import org.ireader.domain.notification.Notifications.CHANNEL_TTS
import org.ireader.domain.notification.flags
import org.ireader.domain.notification.setLargeIcon
import org.ireader.domain.services.tts_service.Player

/**
 * Helper class to encapsulate code for building notifications.
 */

class TTSNotificationBuilder constructor(
    private val context: Context,
    private val mbrComponent: ComponentName,
) {

    private val notificationManager = NotificationManagerCompat.from(context)
    private val pendingIntentFlags =
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT


    val skipPrevActionButton = NotificationCompat.Action(
        org.ireader.core.R.drawable.ic_baseline_skip_previous,
        context.getString(R.string.previous_chapter),
        MediaButtonReceiver.buildMediaButtonPendingIntent(context,
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
    )
    val rewindAction = NotificationCompat.Action(
        org.ireader.core.R.drawable.ic_baseline_fast_rewind,
        context.getString(R.string.previous_paragraph),
        MediaButtonReceiver.buildMediaButtonPendingIntent(context,
            PlaybackStateCompat.ACTION_REWIND)
    )


    val pauseAction = NotificationCompat.Action(
        org.ireader.core.R.drawable.ic_baseline_pause,
        context.getString(R.string.pause),
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP)
    )


    val play = NotificationCompat.Action(
        org.ireader.core.R.drawable.ic_baseline_play_arrow,
        context.getString(R.string.play),
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY)
    )


    val next = NotificationCompat.Action(
        org.ireader.core.R.drawable.ic_baseline_fast_forward,
        context.getString(R.string.next_chapter),
        MediaButtonReceiver.buildMediaButtonPendingIntent(context,
            PlaybackStateCompat.ACTION_FAST_FORWARD)
    )


    val skipNext =
        NotificationCompat.Action(
            org.ireader.core.R.drawable.ic_baseline_skip_next,
            context.getString(R.string.next_chapter),
            MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
        )
    val cancelMediaPlater =
        PendingIntent.getBroadcast(
            context,
            Player.CANCEL,
            Intent(context,
                Class.forName(K.TTSService)).apply {
                putExtra("PLAYER", Player.CANCEL)
            },
            pendingIntentFlags
        )


    @SuppressLint("WrongConstant")
    suspend fun buildNotification(sessionToken: MediaSessionCompat.Token): Notification {
        if (shouldCreateNowPlayingChannel()) {
            createNowPlayingChannel(context)
        }


        val controller = MediaControllerCompat(context, sessionToken)
        val description = controller.metadata?.description
        val playbackState = controller.playbackState
        val cover = controller.metadata?.getString("Cover")

        val builder = NotificationCompat.Builder(context, CHANNEL_TTS)

        builder.addAction(skipPrevActionButton)
        builder.addAction(rewindAction)
        if (playbackState?.isPlaying == true) {
            builder.addAction(pauseAction)
        } else {
            builder.addAction(play)
        }
        builder.addAction(next)
        builder.addAction(skipNext)


        val mediaStyle = androidx.media.app.NotificationCompat.DecoratedMediaCustomViewStyle()
            .setCancelButtonIntent(cancelMediaPlater)
            .setMediaSession(sessionToken)
            .setShowActionsInCompactView(2,3,4)
            .setShowCancelButton(true)

        return builder.setContentIntent(controller.sessionActivity)
            .setStyle(mediaStyle)
            .setSmallIcon(R.drawable.ic_infinity)
            .setContentText(description?.subtitle)
            .setContentTitle(description?.title)
            .setDeleteIntent(cancelMediaPlater)
            .setLargeIcon(context, cover)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()


    }

    fun buildReaderScreenDeepLink(
        bookId: Long,
        sourceId: Long,
        chapterId: Long,
        readingParagraph: Long,
        voiceMode: Long,
    ): String {
        return "https://www.ireader.org/reader_screen_route/$bookId/$chapterId/$sourceId/$readingParagraph/$voiceMode"
    }

    private fun openReaderScreenIntent(
        bookId: Long,
        sourceId: Long,
        chapterId: Long,
        currentReadingParagraph: Int = 0,
    ): PendingIntent = PendingIntent.getActivity(
        context,
        5,
        Intent(
            Intent.ACTION_VIEW,
            buildReaderScreenDeepLink(
                bookId = bookId,
                chapterId = chapterId,
                sourceId = sourceId,
                readingParagraph = currentReadingParagraph.toLong(),
                voiceMode = 1L
            ).toUri(),
            context,
            Class.forName("org.ireader.infinity.MainActivity")
        ),
        flags
    )


    suspend fun buildTTSNotification(
        mediaSessionCompat: MediaSessionCompat,
        isLoading: Boolean = false,
        isError: Boolean = false,
    ): NotificationCompat.Builder {
        val controller = MediaControllerCompat(context,mediaSessionCompat.sessionToken)

        val bookName = controller.metadata.getText(TTSService.NOVEL_TITLE)
        val bookId = controller.metadata.getLong(TTSService.NOVEL_ID)
        val favorite = controller.metadata.getLong(TTSService.FAVORITE)
        val sourceId = controller.metadata.getLong(TTSService.SOURCE_ID)
        val chapterTitle = controller.metadata.getText(TTSService.CHAPTER_TITLE)
        val chapterId = controller.metadata.getLong(TTSService.CHAPTER_ID)
        val cover = controller.metadata.getText(TTSService.NOVEL_COVER)
        val progress = controller.metadata.getLong(TTSService.PROGRESS)
        val lastPar = controller.metadata.getLong(TTSService.LAST_PARAGRAPH)

        val playbackState = controller.playbackState

        val contentText =
            when {
                isLoading -> "Loading..."
                isError -> "ERROR"
                else -> "${progress}/${lastPar}"
            }
        val bookCover = BookCover(id = bookId, sourceId =sourceId ,cover = cover.toString(), favorite = favorite == 1L)
        return NotificationCompat.Builder(context,
            Notifications.CHANNEL_TTS).apply {
            setContentTitle(chapterTitle)
            setContentText(contentText)
            setSmallIcon(org.ireader.core.R.drawable.ic_infinity)
            setOnlyAlertOnce(true)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setLargeIcon(context, cover)
            priority = NotificationCompat.PRIORITY_LOW

            setContentIntent(openReaderScreenIntent(
                chapterId = chapterId,
                bookId = bookId,
                sourceId = sourceId,
                currentReadingParagraph = progress.toInt()))
            addAction(skipPrevActionButton)
            addAction(rewindAction)

            if (playbackState?.isPlaying == true) {
                addAction(pauseAction)
            } else {
                addAction(play)
            }
            addAction(next)
            addAction(skipNext)
            setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setCancelButtonIntent(cancelMediaPlater)
                .setMediaSession(mediaSessionCompat.sessionToken)
                .setShowActionsInCompactView(1, 2, 3)
            )
            setSubText(bookName)

            //setColorized(true)
            //setAutoCancel(true)
            setOngoing(false)
        }
    }

    private fun shouldCreateNowPlayingChannel() =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !nowPlayingChannelExists()

    @RequiresApi(Build.VERSION_CODES.O)
    private fun nowPlayingChannelExists() =
        notificationManager.getNotificationChannel(Notifications.CHANNEL_TTS) != null

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNowPlayingChannel(context: Context) {
        notificationManager.createNotificationChannel(
            NotificationChannel(
                Notifications.CHANNEL_TTS,
                CHANNEL_TTS,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = ""
                setSound(null, null)
                enableVibration(false)
            })
    }
}


