//package org.ireader.domain.services.tts_service.media_player
//
//import android.media.AudioManager
//import android.os.Bundle
//import android.support.v4.media.MediaBrowserCompat
//import android.support.v4.media.session.MediaSessionCompat
//import android.support.v4.media.session.PlaybackStateCompat
//import androidx.core.content.PackageManagerCompat.LOG_TAG
//import androidx.media.MediaBrowserServiceCompat
//import org.ireader.presentation.feature_ttl.TTSState
//import javax.inject.Inject
//
//
//private const val MY_MEDIA_ROOT_ID = "media_root_id"
//private const val MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id"
//
//class TTSMediaService @Inject constructor(
//    private val ttsState: TTSState
//): MediaBrowserServiceCompat() {
//
//    private var mediaSession: MediaSessionCompat? = null
//    private lateinit var stateBuilder: PlaybackStateCompat.Builder
//
//    override fun onCreate() {
//        super.onCreate()
//
//        // Create a MediaSessionCompat
//        mediaSession = MediaSessionCompat(baseContext, "LOG_TAG").apply {
//
//            // Enable callbacks from MediaButtons and TransportControls
//            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
//                    or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
//            )
//
//            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
//            stateBuilder = PlaybackStateCompat.Builder()
//                .setActions(PlaybackStateCompat.ACTION_PLAY
//                        or PlaybackStateCompat.ACTION_PLAY_PAUSE
//                )
//            setPlaybackState(stateBuilder.build())
//
//            // MySessionCallback() has methods that handle callbacks from a media controller
//            setCallback(MySessionCallback())
//
//            // Set the session's token so that client activities can communicate with it.
//            setSessionToken(sessionToken)
//        }
//    }
//
//    override fun onGetRoot(
//        clientPackageName: String,
//        clientUid: Int,
//        rootHints: Bundle?,
//    ): BrowserRoot? {
//        return  MediaBrowserServiceCompat.BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null)
//    }
//
//    override fun onLoadChildren(
//        parentMediaId: String,
//        result: Result<MutableList<MediaBrowserCompat.MediaItem>>,
//    ) {
//        if (MY_EMPTY_MEDIA_ROOT_ID == parentMediaId) {
//            result.sendResult(null)
//            return
//        }
//        val mediaItems = emptyList<MediaBrowserCompat.MediaItem>()
//
//        if (MY_MEDIA_ROOT_ID == parentMediaId) {
//
//        } else {
//
//        }
//        result.sendResult(mediaItems)
//    }
//}