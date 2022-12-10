package ireader.presentation.ui.video.component.core

import android.content.Context
import android.os.Build
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource

@Composable
fun rememberManagedExoPlayer(): ExoPlayer  {
    val context = LocalContext.current
    return remember {
        val builder = ExoPlayer.Builder(context)
        builder.setMediaSourceFactory(ProgressiveMediaSource.Factory(DefaultDataSource.Factory(context)))
        return@remember builder.build().apply {
            playWhenReady = true
        }
    }

}

@Composable
fun rememberManagedPlayer(
        lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle,
        factory: (Context) -> Player
): State<Player?> {
    val currentContext = LocalContext.current.applicationContext
    val playerManager = remember { PlayerManager { factory(currentContext) } }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when {
                (event == Lifecycle.Event.ON_START && Build.VERSION.SDK_INT > 23)
                        || (event == Lifecycle.Event.ON_RESUME && Build.VERSION.SDK_INT <= 23) -> {
                    playerManager.initialize()
                }
                (event == Lifecycle.Event.ON_PAUSE && Build.VERSION.SDK_INT <= 23)
                        || (event == Lifecycle.Event.ON_STOP && Build.VERSION.SDK_INT > 23) -> {
                    playerManager.release()
                }
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
    return playerManager.player
}

@Stable
internal class PlayerManager(
        private val factory: () -> Player,
) : RememberObserver {
    var player = mutableStateOf<Player?>(null)
    private var rememberedState: Triple<String, Int, Long>? = null
    private val window: Timeline.Window = Timeline.Window()

    internal fun initialize() {
        if (player.value != null) return
        player.value = factory().also { player ->
            player.addListener(object : Player.Listener {
                override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                    // recover the remembered state if media id matched
                    rememberedState
                            ?.let { (id, index, position) ->
                                if (!timeline.isEmpty
                                        && timeline.windowCount > index
                                        && id == timeline.getWindow(index, window).mediaItem.mediaId
                                ) {
                                    player.seekTo(index, position)
                                }
                            }
                            ?.also { rememberedState = null }
                }
            })
        }
    }

    internal fun release() {
        player.value?.let { player ->
            // remember the current state before release
            player.currentMediaItem?.let { mediaItem ->
                rememberedState = Triple(
                        mediaItem.mediaId,
                        player.currentMediaItemIndex,
                        player.currentPosition
                )
            }
            player.release()
        }
        player.value = null
    }

    override fun onAbandoned() {
        release()
    }

    override fun onForgotten() {
        release()
    }

    override fun onRemembered() {
    }
}
