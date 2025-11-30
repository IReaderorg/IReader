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
import ireader.core.log.Log
import ireader.domain.notification.NotificationsIds
import ireader.domain.notification.NotificationsIds.CHANNEL_TTS
import ireader.domain.notification.legacyFlags
import ireader.domain.notification.setLargeIcon
import ireader.domain.services.tts_service.Player
import ireader.domain.utils.extensions.launchMainActivityIntent
import ireader.i18n.Args
import ireader.i18n.LocalizeHelper
import ireader.i18n.R
import ireader.i18n.SHORTCUTS
import ireader.i18n.resources.Res
import ireader.i18n.resources.*

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

    // Previous paragraph action using SKIP_TO_PREVIOUS (shows skip icon on most devices)
    val prevParagraphAction = NotificationCompat.Action(
        R.drawable.ic_baseline_skip_previous,
        localizeHelper.localize(Res.string.previous_paragraph),
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        )
    )
    
    // Alternative previous paragraph using REWIND (shows rewind icon on some devices)
    val rewindAction = NotificationCompat.Action(
        R.drawable.ic_baseline_fast_rewind,
        localizeHelper.localize(Res.string.previous_paragraph),
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_REWIND
        )
    )

    val pauseAction = NotificationCompat.Action(
        R.drawable.ic_baseline_pause,
        localizeHelper.localize(Res.string.pause),
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PAUSE)
    )

    val play = NotificationCompat.Action(
        R.drawable.ic_baseline_play_arrow,
        localizeHelper.localize(Res.string.play),
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY)
    )

    // Next paragraph action using FAST_FORWARD (shows fast-forward icon on some devices)
    val nextParagraphFastForward = NotificationCompat.Action(
        R.drawable.ic_baseline_fast_forward,
        localizeHelper.localize(Res.string.next_paragraph),
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_FAST_FORWARD
        )
    )

    // Next paragraph action using SKIP_TO_NEXT (shows skip icon on most devices)
    val nextParagraphAction = NotificationCompat.Action(
        R.drawable.ic_baseline_skip_next,
        localizeHelper.localize(Res.string.next_paragraph),
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
            localizeHelper.localize(Res.string.next_chapter),
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
            localizeHelper.localize(Res.string.close),
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
                action = TTSService.ACTION_CANCEL
            },
            pendingIntentFlags
        )

    @SuppressLint("WrongConstant")
    suspend fun buildNotification(sessionToken: MediaSessionCompat.Token): Notification {
        if (shouldCreateNowPlayingChannel()) {
            createNowPlayingChannel(context)
        }

        val controller = MediaControllerCompat(context, sessionToken)
        val playbackState = controller.playbackState
        
        // Get metadata
        val bookName = controller.metadata?.getText(TTSService.NOVEL_TITLE) ?: ""
        val chapterTitle = controller.metadata?.getText(TTSService.CHAPTER_TITLE) ?: ""
        val cover = controller.metadata?.getText(TTSService.NOVEL_COVER)
        val progress = controller.metadata?.getLong(TTSService.PROGRESS) ?: 0
        val lastPar = controller.metadata?.getLong(TTSService.LAST_PARAGRAPH) ?: 0
        val isLoading = controller.metadata?.getLong(TTSService.IS_LOADING) == 1L
        
        val paragraphText = when {
            isLoading -> "Loading..."
            else -> "Paragraph ${progress + 1} of ${lastPar + 1}"
        }

        // Build actions list first to avoid concurrent modification
        // Order: prev paragraph (0), play/pause (1), next paragraph (2), close (3)
        // Compact view shows indices 0, 1, 2 = prev paragraph, play/pause, next paragraph
        // Using SKIP actions since Android prefers to show these on most devices
        val actions = buildList {
            add(prevParagraphAction)  // SKIP_TO_PREVIOUS (does paragraph nav)
            add(if (playbackState?.isPlaying == true) pauseAction else play)
            add(nextParagraphAction)  // SKIP_TO_NEXT (does paragraph nav)
            add(close)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_TTS)
        
        // Add all actions at once to avoid concurrent modification
        actions.forEach { action -> builder.addAction(action) }

        val mediaStyle = androidx.media.app.NotificationCompat.DecoratedMediaCustomViewStyle()
            .setCancelButtonIntent(cancelMediaPlayer())
            .setMediaSession(sessionToken)
            .setShowActionsInCompactView(0, 1, 2)
            .setShowCancelButton(true)

        return builder.setContentIntent(controller.sessionActivity)
            .setStyle(mediaStyle)
            .setSmallIcon(R.drawable.ic_infinity)
            .setContentTitle(paragraphText)
            .setContentText(chapterTitle)
            .setDeleteIntent(cancelMediaPlayer())
            .setLargeIcon(context, cover)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
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
                else -> "Paragraph ${progress + 1} of ${lastPar + 1}"
            }
        
        val builder = NotificationCompat.Builder(context, NotificationsIds.CHANNEL_TTS)
        
        // Add actions in order: prev paragraph (0), play/pause (1), next paragraph (2), close (3), open (4)
        // Compact view will show indices 0, 1, 2
        // Using SKIP actions since Android prefers to show these on most devices
        builder.addAction(prevParagraphAction)  // index 0 - SKIP_TO_PREVIOUS (does paragraph nav)
        builder.addAction(if (playbackState?.isPlaying == true) pauseAction else play)  // index 1
        builder.addAction(nextParagraphAction)  // index 2 - SKIP_TO_NEXT (does paragraph nav)
        builder.addAction(close)  // index 3
        builder.addAction(openTTSScreen(bookId, sourceId, chapterId))  // index 4
        
        return builder.apply {
            setContentTitle(contentText)
            setContentText(chapterTitle)
            setSmallIcon(R.drawable.ic_infinity)
            setOnlyAlertOnce(true)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setLargeIcon(context, cover)
            priority = NotificationCompat.PRIORITY_LOW
            
            // Add progress bar showing paragraph position
            if (!isLoading && !isError && lastPar > 0) {
                setProgress(lastPar.toInt(), progress.toInt(), false)
            }

            setContentIntent(
                openReaderScreenIntent(
                    chapterId = chapterId,
                    bookId = bookId,
                    sourceId = sourceId,
                    currentReadingParagraph = progress.toInt()
                )
            )
            setDeleteIntent(cancelMediaPlayer())
            
            // MediaStyle with explicit compact view actions
            // Actions 0, 1, 2 = rewind, play/pause, fast-forward
            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSessionCompat.sessionToken)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(cancelMediaPlayer())
                    .setShowActionsInCompactView(0, 1, 2)
            )
            setAutoCancel(false)
            setColorized(true)
            // Allow dismissal when paused, prevent when playing
            setOngoing(playbackState?.isPlaying == true)
        }
    }

    private fun shouldCreateNowPlayingChannel() =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !nowPlayingChannelExists()

    @RequiresApi(Build.VERSION_CODES.O)
    private fun nowPlayingChannelExists() =
        notificationManager.getNotificationChannel(NotificationsIds.CHANNEL_TTS) != null

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNowPlayingChannel(context: Context) {
        notificationManager.createNotificationChannel(
            NotificationChannel(
                NotificationsIds.CHANNEL_TTS,
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
