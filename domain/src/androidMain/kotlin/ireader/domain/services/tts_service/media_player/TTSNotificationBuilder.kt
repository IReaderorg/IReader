package ireader.domain.services.tts_service.media_player

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.session.MediaButtonReceiver
import ireader.domain.R
import ireader.domain.notification.Notifications
import ireader.domain.notification.Notifications.CHANNEL_TTS
import ireader.domain.notification.legacyFlags
import ireader.domain.notification.setLargeIcon
import ireader.domain.services.tts_service.Player
import ireader.domain.services.tts_service.media_player.TTSService.Companion.ACTION_CANCEL
import ireader.domain.utils.extensions.launchMainActivityIntent
import ireader.i18n.Args
import ireader.i18n.LocalizeHelper
import ireader.i18n.SHORTCUTS
import ireader.i18n.resources.MR
/**
 * Helper class to encapsulate code for building notifications.
 */

class TTSNotificationBuilder constructor(
    private val context: Context,
    private val localizeHelper: LocalizeHelper
) {

    private val notificationManager = NotificationManagerCompat.from(context)
    private val pendingIntentFlags =
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

    val skipPrevActionButton = NotificationCompat.Action(
        R.drawable.ic_baseline_skip_previous,
        localizeHelper.localize(MR.strings.previous_chapter),
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        )
    )
    val rewindAction = NotificationCompat.Action(
        R.drawable.ic_baseline_fast_rewind,
        localizeHelper.localize(MR.strings.previous_paragraph),
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_REWIND
        )
    )

    val pauseAction = NotificationCompat.Action(
        R.drawable.ic_baseline_pause,
        localizeHelper.localize(MR.strings.pause),
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP)
    )

    val play = NotificationCompat.Action(
        R.drawable.ic_baseline_play_arrow,
        localizeHelper.localize(MR.strings.play),
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY)
    )

    val next = NotificationCompat.Action(
        R.drawable.ic_baseline_fast_forward,
        localizeHelper.localize(MR.strings.next_chapter),
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_FAST_FORWARD
        )
    )

    val skipNext =
        NotificationCompat.Action(
            R.drawable.ic_baseline_skip_next,
            localizeHelper.localize(MR.strings.next_chapter),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                context,
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            )
        )

    fun openTTSScreen(
        bookId: Long,
        sourceId: Long,
        chapterId: Long,
        progress: Int = 0,
    ) =
        NotificationCompat.Action(
            R.drawable.ic_baseline_open_in_new_24,
            localizeHelper.localize(MR.strings.next_chapter),
            openReaderScreenIntent(
                chapterId = chapterId,
                bookId = bookId,
                sourceId = sourceId,
                currentReadingParagraph = progress.toInt()
            )
        )

    val close =
        NotificationCompat.Action(
            R.drawable.baseline_close_24,
            localizeHelper.localize(MR.strings.close),
            cancelMediaPlayer()
        )

    fun cancelMediaPlayer(): PendingIntent =
        PendingIntent.getService(
            context,
            Player.CANCEL,
            Intent(
                context,
                TTSService::class.java
            ).apply {
                action = ACTION_CANCEL
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
            .setCancelButtonIntent(cancelMediaPlayer())
            .setMediaSession(sessionToken)
            .setShowActionsInCompactView(2, 3, 4)
            .setShowCancelButton(true)

        return builder.setContentIntent(controller.sessionActivity)
            .setStyle(mediaStyle)
            .setSmallIcon(R.drawable.ic_infinity)
            .setContentText(description?.subtitle)
            .setContentTitle(description?.title)
            .setDeleteIntent(cancelMediaPlayer())
            .setLargeIcon(context, cover)
            .setOnlyAlertOnce(true)
            .setOngoing(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun openReaderScreenIntent(
        bookId: Long,
        sourceId: Long,
        chapterId: Long,
        currentReadingParagraph: Int = 0,
    ): PendingIntent = PendingIntent.getActivity(
        context,
        5,
        launchMainActivityIntent(context)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            .apply {
                action = SHORTCUTS.SHORTCUT_TTS
                putExtra(Args.ARG_BOOK_ID,bookId)
                putExtra(Args.ARG_CHAPTER_ID,chapterId)
                putExtra(Args.ARG_SOURCE_ID,sourceId)
                putExtra(Args.ARG_READING_PARAGRAPH,currentReadingParagraph.toLong())
//                data = buildReaderScreenDeepLink(
//                    bookId = bookId,
//                    chapterId = chapterId,
//                    sourceId = sourceId,
//                    readingParagraph = currentReadingParagraph.toLong(),
//                ).toUri()
            },
        legacyFlags
    )

    suspend fun buildTTSNotification(
        mediaSessionCompat: MediaSessionCompat,
    ): NotificationCompat.Builder {
        val controller = MediaControllerCompat(context, mediaSessionCompat.sessionToken)

        val bookName = controller.metadata.getText(TTSService.NOVEL_TITLE)
        val bookId = controller.metadata.getLong(TTSService.NOVEL_ID)
        val favorite = controller.metadata.getLong(TTSService.FAVORITE)
        val sourceId = controller.metadata.getLong(TTSService.SOURCE_ID)
        val chapterTitle = controller.metadata.getText(TTSService.CHAPTER_TITLE)
        val chapterId = controller.metadata.getLong(TTSService.CHAPTER_ID)
        val cover = controller.metadata.getText(TTSService.NOVEL_COVER)
        val progress = controller.metadata.getLong(TTSService.PROGRESS)
        val lastPar = controller.metadata.getLong(TTSService.LAST_PARAGRAPH)
        val isLoading: Boolean = controller.metadata.getLong(TTSService.IS_LOADING) == 1L
        val isError: Boolean = controller.metadata.getLong(TTSService.ERROR) == 1L

        val playbackState = controller.playbackState

        val contentText =
            when {
                isLoading -> "Loading..."
                isError -> "ERROR"
                else -> "${progress + 1}/${lastPar + 1}"
            }
        return NotificationCompat.Builder(
            context,
            Notifications.CHANNEL_TTS
        ).apply {
            setContentTitle(chapterTitle)
            setContentText(contentText)
            setSmallIcon(R.drawable.ic_infinity)
            setOnlyAlertOnce(true)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setLargeIcon(context, cover)
            priority = NotificationCompat.PRIORITY_LOW
            //  setProgress(lastPar.toInt(), progress.toInt(), false)
            // setTicker(contentText)

//            setContentIntent(
//                openReaderScreenIntent(
//                    chapterId = chapterId,
//                    bookId = bookId,
//                    sourceId = sourceId,
//                    currentReadingParagraph = progress.toInt()
//                )
//            )
            setDeleteIntent(cancelMediaPlayer())
            //   addAction(skipPrevActionButton)
            addAction(rewindAction)

            if (playbackState?.isPlaying == true) {
                addAction(pauseAction)
            } else {
                addAction(play)
            }
            addAction(next)
            //  addAction(skipNext)
            addAction(close)
            addAction(openTTSScreen(bookId, sourceId, chapterId))
            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSessionCompat.sessionToken)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(cancelMediaPlayer())
                    .setShowActionsInCompactView(0, 1, 2)

            )

            setSubText(bookName)
            setAutoCancel(false)
            setColorized(true)
            setOngoing(true)
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
            }
        )
    }
}
