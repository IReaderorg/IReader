package ireader.presentation.ui.video

import android.content.pm.ActivityInfo
import android.view.accessibility.CaptioningManager.CaptionStyle.EDGE_TYPE_NONE
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.SubtitleView
import ireader.presentation.ui.component.LockScreenOrientation
import ireader.presentation.ui.video.component.SimpleController
import ireader.presentation.ui.video.component.core.*

val SurfaceTypes = SurfaceType.values().toList()
val ResizeModes = ResizeMode.values().toList()
val ShowBufferingValues = ShowBuffering.values().toList()
val Urls = listOf(
        "https://storage.googleapis.com/downloads.webmproject.org/av1/exoplayer/bbb-av1-480p.mp4",
        "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3",
        "https://storage.googleapis.com/exoplayer-test-media-1/mkv/android-screens-lavf-56.36.100-aac-avc-main-1280x720.mkv",
        "https://storage.googleapis.com/exoplayer-test-media-1/mp4/frame-counter-one-hour.mp4",
        "https://html5demos.com/assets/dizzy.mp4",
)

private enum class ControllerType {
    None, Simple, StyledPlayerControlView
}

private val ControllerTypes = ControllerType.values().toList()

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun VideoPresenter(
        vm: VideoScreenViewModel,
        onShowMenu: () -> Unit,
        player: ExoPlayer?,
        state: MediaState
) {
    LockScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,state.playerState?.isFulLScreen)
    val playWhenReady by rememberSaveable { mutableStateOf(true) }
    DisposableEffect(player, playWhenReady) {
        player?.playWhenReady = playWhenReady
        onDispose {}
    }
    Media(
            state = state,
            // following parameters are optional
            modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
            surfaceType = SurfaceType.SurfaceView,
            resizeMode = ResizeMode.Fit,
            keepContentOnPlayerReset = false,
            useArtwork = true,
            showBuffering = ShowBuffering.Always,
            buffering = {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            },
            subtitles = {
                AndroidView(factory = { context ->
                    SubtitleView(context)
                }, update = { view ->
                    view.setStyle(CaptionStyleCompat(Color.Black.toArgb(),Color.Transparent.toArgb(),Color.Transparent.toArgb(),EDGE_TYPE_NONE,Color.Transparent.toArgb(),null))
                    view.setCues(it)
                    view.setStyle(CaptionStyleCompat(Color.Black.toArgb(), Color.White.toArgb(), Color.White.toArgb(), Color.White.toArgb(), Color.White.toArgb(), null))
                })
            },
    ) { playerState ->
        SimpleController(
                vm.chapter?.name ?: "",
                playerState,
                Modifier.fillMaxSize(),
                onShowMenu = onShowMenu
        )
    }
}


