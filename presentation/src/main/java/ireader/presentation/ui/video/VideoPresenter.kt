package ireader.presentation.ui.video

import android.content.ContentResolver
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.ui.SubtitleView
import com.google.common.collect.ImmutableList
import ireader.presentation.BuildConfig
import ireader.presentation.R
import ireader.presentation.ui.component.LockScreenOrientation
import ireader.presentation.ui.video.component.SimpleController
import ireader.presentation.ui.video.component.core.Media
import ireader.presentation.ui.video.component.core.ResizeMode
import ireader.presentation.ui.video.component.core.ShowBuffering
import ireader.presentation.ui.video.component.core.SurfaceType
import ireader.presentation.ui.video.component.core.rememberMediaState
import java.io.File

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
    onShowMenu: () -> Unit
) {
    LockScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
    val context = LocalContext.current

    val url by rememberSaveable {
        mutableStateOf(
            Uri.parse(
                ContentResolver.SCHEME_ANDROID_RESOURCE
                        + File.pathSeparator + File.separator + File.separator
                        + context.packageName
                        + File.separator
                        + R.raw.sample
            )
        )
    }

    val setPlayer by rememberSaveable { mutableStateOf(true) }
    val playWhenReady by rememberSaveable { mutableStateOf(true) }
    // need to change this part later
    val uri = if (BuildConfig.DEBUG) {
        url
    } else vm.videoUri.value?.toUri()
    val mediaItem = remember {
        MediaItem.Builder().setMediaId(uri.toString()).setUri(uri)
            .setSubtitleConfigurations(ImmutableList.of(vm.subtitle)).build()
    }
    var rememberedMediaItemIdAndPosition: Pair<String, Long>? by remember { mutableStateOf(null) }
    val player = vm.player
    DisposableEffect(player, playWhenReady) {
        player?.playWhenReady = playWhenReady
        onDispose {}
    }
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    DisposableEffect(mediaItem, player) {
        player?.run {
            if (mediaItem != null) {
                setMediaItem(mediaItem)
            }
            rememberedMediaItemIdAndPosition?.let { (id, position) ->
                if (id == mediaItem?.mediaId) seekTo(position)
            }?.also { rememberedMediaItemIdAndPosition = null }
            prepare()
        }
        onDispose {}
    }
    val state = rememberMediaState(player = player.takeIf { setPlayer })
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
                CircularProgressIndicator()
            }
        },
        subtitles = {
            AndroidView(factory = {
                SubtitleView(it)
            }, update = { view ->

            })
            Text(text = "Test")
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

