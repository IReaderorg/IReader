//package org.ireader.domain.services.tts_service.media_player
//
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.media.AudioFocusRequest
//import android.media.AudioManager
//import android.os.Bundle
//import android.os.ResultReceiver
//import android.support.v4.media.MediaBrowserCompat
//import android.support.v4.media.session.MediaControllerCompat
//import android.support.v4.media.session.MediaSessionCompat
//import android.support.v4.media.session.PlaybackStateCompat
//import android.util.Log
//import androidx.core.app.NotificationManagerCompat
//import androidx.media.MediaBrowserServiceCompat
//import dagger.hilt.android.qualifiers.ApplicationContext
//import org.ireader.domain.notification.Notifications.ID_TEXT_READER_PROGRESS
//import javax.inject.Inject
//
//
//private const val MY_MEDIA_ROOT_ID = "media_root_id"
//private const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"
//
//class TTSMediaService @Inject constructor(
//     @ApplicationContext private val context: Context
//): MediaBrowserServiceCompat(), AudioManager.OnAudioFocusChangeListener {
//
//    companion object {
//        const val TAG = "NLTTS_Main"
//
//        const val AUDIO_TEXT_KEY = "audioTextKey"
//        const val TITLE = "title"
//        const val NOVEL_ID = "novelId"
//        const val TRANSLATOR_SOURCE_NAME = "translatorSourceName"
//        const val CHAPTER_INDEX = "chapterIndex"
//        const val LINKED_PAGES = "linkedPages"
//        const val KEY_SENTENCES = "sentences"
//
//        const val ACTION_OPEN_CONTROLS = "open_controls"
//        const val ACTION_OPEN_SETTINGS = "open_settings"
//        const val ACTION_OPEN_READER = "open_reader"
//        const val ACTION_UPDATE_SETTINGS = "update_settings"
//
//        const val ACTION_STOP = "actionStop"
//        const val ACTION_PAUSE = "actionPause"
//        const val ACTION_PLAY_PAUSE = "actionPlayPause"
//        const val ACTION_PLAY = "actionPlay"
//        const val ACTION_NEXT = "actionNext"
//        const val ACTION_PREVIOUS = "actionPrevious"
//
//        const val ACTION_STARTUP = "startup"
//
//        const val COMMAND_REQUEST_LINKED_PAGES = "cmd_$LINKED_PAGES"
//        const val COMMAND_REQUEST_SENTENCES = "cmd_$KEY_SENTENCES"
//        const val COMMAND_UPDATE_LANGUAGE = "update_language"
//        const val COMMAND_UPDATE_TIMER = "update_timer"
//        const val COMMAND_LOAD_BUFFER_LINK = "cmd_load_buffer_link"
//        const val COMMAND_RELOAD_CHAPTER = "cmd_reload_chapter"
//        const val EVENT_SENTENCE_LIST = "event_$KEY_SENTENCES"
//        const val EVENT_LINKED_PAGES = "event_$LINKED_PAGES"
//
//        private const val STATE_PREFIX = "TTSService."
//        const val STATE_NOVEL_ID = STATE_PREFIX + NOVEL_ID
//        const val STATE_TRANSLATOR_SOURCE_NAME = STATE_PREFIX + TRANSLATOR_SOURCE_NAME
//        const val STATE_CHAPTER_INDEX = STATE_PREFIX + CHAPTER_INDEX
//
//        const val PITCH_MIN = 0.5f
//        const val PITCH_MAX = 2.0f
//        const val SPEECH_RATE_MIN = 0.5f
//        const val SPEECH_RATE_MAX = 3.0f
//    }
//    lateinit var mediaSession: MediaSessionCompat
//    private lateinit var mediaCallback: TTSSessionCallback
//    private lateinit var stateBuilder: PlaybackStateCompat.Builder
//
//    private val noisyReceiver = NoisyReceiver()
//    private var noisyReceiverHooked = false
//
//    private lateinit var notificationBuilder: TTSNotificationBuilder
//    private lateinit var notificationManager: NotificationManagerCompat
//    private lateinit var notification: NotificationController
//    private lateinit var stopTimer: StopTimerController
//
//    private lateinit var focusRequest: AudioFocusRequest
//
//    private var isHooked = false
//    private var isForeground = false
//
//    private val focusLock = Any()
//    private var resumeOnFocus = true
//
//    lateinit var player: TTSPlayer
//    var initialized: Boolean = false
//
//
//
//    private inner class TTSSessionCallback : MediaSessionCompat.Callback() {
//
//        override fun onPlay() {
//            Log.d(TAG, "onPlay")
//            if (hookSystem()) {
//                if (player.isDisposed) {
//                    val old = player
//                    player = TTSPlayer(this@TTSMediaService, mediaSession, stateBuilder)
//                    player.setFrom(old)
//                }
//                player.start()
//                stopTimer.reset()
//            }
//        }
//
//        override fun onStop() {
//            Log.d(TAG, "onStop")
//            if (unhookSystem()) {
//                player.destroy()
//            }
//        }
//
//        override fun onPause() {
//            Log.d(TAG, "onPause")
//            player.stop()
//            stopTimer.reset()
//            resumeOnFocus = false
//            if (isForeground) {
//                stopForeground(false)
//                isForeground = false
//            }
//            if (noisyReceiverHooked) {
//                unregisterReceiver(noisyReceiver)
//                noisyReceiverHooked = false
//            }
//        }
//
//        override fun onSeekTo(pos: Long) {
//            player.goto((pos / 1000L).toInt())
//            stopTimer.reset()
//        }
//
//        override fun onRewind() {
//            player.goto(player.lineNumber - 1)
//            stopTimer.reset()
//        }
//
//        override fun onFastForward() {
//            player.goto(player.lineNumber + 1)
//            stopTimer.reset()
//        }
//
//        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
//            mediaId?.toIntOrNull()?.let {
//                Log.d(TAG, "Play from media ID: $mediaId")
//                player.gotoChapter(it)
//                stopTimer.reset()
//            }
//        }
////        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
////            super.onPlayFromMediaId(mediaId, extras)
//        // TODO: Support chapter navigation
////        }
//
//        override fun onSkipToNext() {
//            player.nextChapter()
//            stopTimer.reset()
//        }
//
//        override fun onSkipToPrevious() {
//            player.previousChapter()
//            stopTimer.reset()
//        }
//
//        override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
////            Log.d(TAG, "Command: $command")
//            when (command) {
//                COMMAND_REQUEST_SENTENCES -> player.sendSentences()
//                COMMAND_REQUEST_LINKED_PAGES -> player.sendLinkedPages()
//                ACTION_UPDATE_SETTINGS -> player.updateVoiceConfig()
//                COMMAND_UPDATE_LANGUAGE -> player.selectLanguage()
//                COMMAND_UPDATE_TIMER -> {
//                    if (extras?.containsKey("active") == true) extras.getBoolean("active").let {
//                        if (it)
//                            Log.d(TAG, "Starting auto-stop timer")
//                        else
//                            Log.d(TAG, "Stopping auto-stop timer (user-request)")
//                        if (it && !stopTimer.isActive) stopTimer.reset(false)
//                        stopTimer.isActive = it
//                    }
//                    if (extras?.getBoolean("reset") == true)
//                        stopTimer.reset(false)
//                    cb?.send(if (stopTimer.isActive) 1 else 0, Bundle().apply {
//                        putLong("time", stopTimer.stopTime)
//                        putBoolean("active", stopTimer.isActive)
//                    })
//                }
//                COMMAND_LOAD_BUFFER_LINK -> extras?.getString("href")?.let { player.loadLinkedPage(it) }
//                COMMAND_RELOAD_CHAPTER -> {
//                    player.clearChapterCache()
//                    player.loadCurrentChapter()
//                }
//            }
//            super.onCommand(command, extras, cb)
//        }
//
//    }
//    private inner class NoisyReceiver : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
//                resumeOnFocus = false
//                player.stop()
//            }
//        }
//    }
//    private inner class NotificationController : MediaControllerCompat.Callback() {
//
//        private val controller = MediaControllerCompat(this@TTSMediaService, mediaSession.sessionToken)
//
//        fun start() {
//            controller.registerCallback(this)
//        }
//
//        fun stop() {
//            controller.unregisterCallback(this)
//        }
//
//        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
//            if (state != null && mediaSession.controller.metadata != null) {
//                notificationManager.notify(ID_TEXT_READER_PROGRESS, notificationBuilder.buildNotification(mediaSession.sessionToken))
//            }
//        }
//    }
//    private inner class StopTimerController : MediaControllerCompat.Callback() {
//        private val controller = MediaControllerCompat(context, mediaSession.sessionToken)
//
//        var isActive: Boolean = false
//        var stopTime: Long = 0
//
//        fun reset(withEvent: Boolean = true) {
//            // In case we pause or do something else that interrupts playback - reset the timer.
//           // stopTime = System.currentTimeMillis() + java.util.concurrent.TimeUnit.MINUTES.toMillis(player.dataCenter.ttsPreferences.stopTimer)
//            if (isActive && withEvent) {
//                Log.d(TAG, "Resetting the auto-stop timer")
//                mediaSession.sendSessionEvent(COMMAND_UPDATE_TIMER, Bundle().apply {
//                    putBoolean("active", isActive)
//                    putLong("time", stopTime)
//                })
//            }
//        }
//
//        fun start() {
//            controller.registerCallback(this)
//        }
//
//        fun stop() {
//            controller.unregisterCallback(this)
//        }
//
//        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
//            if (isActive) {
//                if (state?.isPlaying == true && System.currentTimeMillis() >= stopTime) {
//                    Log.d(TAG, "Pausing TTS due to auto-stop timer")
//                    isActive = false
//                    controller.transportControls.pause()
//                    mediaSession.sendSessionEvent(COMMAND_UPDATE_TIMER, Bundle().apply {
//                        putBoolean("active", isActive)
//                        putLong("time", 0)
//                    })
//                    // TODO: Save state on exit so user can restore it
//                }
//            }
//        }
//
//    }
//
//    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot {
//        return BrowserRoot("NONE", null)
//    }
//
//    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
//        result.sendResult(mutableListOf())
//    }
//
//    override fun onAudioFocusChange(focusChange: Int) {
//        Log.d(TAG, "Focus change $focusChange")
//        when (focusChange) {
//            AudioManager.AUDIOFOCUS_GAIN -> if (resumeOnFocus) {
//                synchronized(focusLock) { resumeOnFocus = false }
//                player.start()
//            }
//            AudioManager.AUDIOFOCUS_LOSS -> {
//                synchronized(focusLock) { resumeOnFocus = false }
//                player.stop()
//            }
//            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
//                synchronized(focusLock) { resumeOnFocus = player.isPlaying }
//                player.stop()
//            }
//        }
//    }
//}