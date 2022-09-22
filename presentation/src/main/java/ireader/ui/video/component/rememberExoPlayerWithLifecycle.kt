package ireader.ui.video.component

import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.media3.ui.PlayerView.SHOW_BUFFERING_ALWAYS
import ireader.domain.utils.extensions.findComponentActivity
import ireader.domain.utils.extensions.hideSystemUI
import ireader.domain.utils.extensions.showSystemUI


@Composable
fun rememberPlayerView(exoPlayer: ExoPlayer): PlayerView {
    val context = LocalContext.current
    val playerView = remember {
        PlayerView(context).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            useController = true
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            player = exoPlayer
            setShowBuffering(SHOW_BUFFERING_ALWAYS)
        }
    }
    DisposableEffect(key1 = true) {
        onDispose {
            playerView.player = null
        }
    }
    return playerView
}

@Composable
fun VideoView(videoUri: Uri) {
    val context = LocalContext.current
    context.findComponentActivity()!!.hideSystemUI()

    val exoPlayer = remember(videoUri) {
        ExoPlayer.Builder(context)
            .build()
            .also { exoPlayer ->
                exoPlayer.setHandleAudioBecomingNoisy(true)
                val mediaItem = MediaItem.Builder()
                    .setUri(videoUri)
                    .build()
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.setHandleAudioBecomingNoisy(true)
                exoPlayer.prepare()

            }
    }

    val playerView = rememberPlayerView(exoPlayer)
    AndroidView(factory = {
        it.findComponentActivity()!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        playerView
    })
    DisposableEffect(true) {
        onDispose {
            exoPlayer.stop()
            exoPlayer.release()
            playerView.player = null
            context.findComponentActivity()!!.showSystemUI()
            context.findComponentActivity()!!.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
}