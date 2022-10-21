package ireader.presentation.ui.video.component.core

import android.graphics.BitmapFactory
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.text.Cue


/**
 * The type of surface view used for video playbacks.
 */
enum class SurfaceType {
    None,
    SurfaceView,
    TextureView;
}

/**
 * Determines when the buffering indicator is shown.
 */
enum class ShowBuffering {
    /**
     * The buffering indicator is never shown.
     */
    Never,

    /**
     * The buffering indicator is shown when the player is in the [buffering][Player.STATE_BUFFERING]
     * state and [playWhenReady][Player.getPlayWhenReady] is true.
     */
    WhenPlaying,

    /**
     * The buffering indicator is always shown when the player is in the
     * [buffering][Player.STATE_BUFFERING] state.
     */
    Always;
}

/**
 * Composable component for [Player] media playbacks.
 *
 * @param state The state object to be used to control or observe the [Media] state.
 * @param modifier The modifier to apply to this layout.
 * @param surfaceType The type of surface view used for video playbacks.Using [SurfaceType.None] is
 * recommended for audio only applications, since creating the surface can be expensive. Using
 * [SurfaceType.SurfaceView] is recommended for video applications. Note, [SurfaceType.TextureView]
 * can only be used in a hardware accelerated window. When rendered in software, TextureView will
 * draw nothing.
 * @param resizeMode Controls how video and album art is resized.
 * @param shutterColor The color of the shutter, which used for hiding the currently displayed video
 * frame or media artwork when the player is reset and [keepContentOnPlayerReset] is false.
 * @param keepContentOnPlayerReset Whether the currently displayed video frame or media artwork is
 * kept visible when the player is reset. A player reset is defined to mean the player being
 * re-prepared with different media, the player transitioning to unprepared media or an empty list
 * of media items, or the player being changed.
 * If true is provided, the currently displayed video frame or media artwork will be kept visible
 * until the player has been successfully prepared with new media and loaded enough of it to have
 * determined the available tracks. Hence enabling this option allows transitioning from playing one
 * piece of media to another, or from using one player instance to another, without clearing the
 * content.
 * If false is provided, the currently displayed video frame or media artwork will be hidden as soon
 * as the player is reset.
 * @param useArtwork Whether artwork is used if available in audio streams.
 * @param defaultArtworkPainter The [Painter], which will be used to draw default artwork if no
 * artwork available in audio streams.
 * @param subtitles The subtitles. Default is null.
 * @param showBuffering Determines when the buffering indicator is shown.
 * @param buffering The buffering indicator, typically a circular progress indicator. Default is
 * null.
 * @param errorMessage The error message, which will be shown when an [error][PlaybackException]
 * occurred. Default is null.
 * @param overlay An overlay, which can be shown on top of the player. Default is null.
 * @param controllerHideOnTouch Whether the playback controls are hidden by touch. Default is true.
 * @param controllerAutoShow Whether the playback controls are automatically shown when playback
 * starts, pauses, ends, or fails.
 * @param controller The controller. Since a controller is always a subject to be customized,
 * default is null. The [Media] only provides logic for controller visibility controlling.
 */
@Composable
fun Media(
    state: MediaState,
    modifier: Modifier = Modifier,
    surfaceType: SurfaceType = SurfaceType.SurfaceView,
    resizeMode: ResizeMode = ResizeMode.Fit,
    shutterColor: Color = Color.Black,
    keepContentOnPlayerReset: Boolean = false,
    useArtwork: Boolean = true,
    defaultArtworkPainter: Painter? = null,
    subtitles: @Composable ((List<Cue>) -> Unit)? = null,
    showBuffering: ShowBuffering = ShowBuffering.Never,
    buffering: @Composable (() -> Unit)? = null,
    errorMessage: @Composable ((PlaybackException) -> Unit)? = null,
    overlay: @Composable (() -> Unit)? = null,
    controllerHideOnTouch: Boolean = true,
    controllerAutoShow: Boolean = true,
    controller: @Composable ((MediaState) -> Unit)? = null
) {
    if (showBuffering != ShowBuffering.Never) require(buffering != null) {
        "buffering should not be null if showBuffering is 'ShowBuffering.$showBuffering'"
    }

    LaunchedEffect(Unit) {
        snapshotFlow { state.contentAspectRatioRaw }
            .collect { contentAspectRatioRaw ->
                state.contentAspectRatio = contentAspectRatioRaw
            }
    }

    Box(
        modifier = modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                if (controller != null && state.player != null) {
                    state.controllerVisibility = when (state.controllerVisibility) {
                        ControllerVisibility.Visible -> {
                            if (controllerHideOnTouch) ControllerVisibility.Invisible
                            else ControllerVisibility.Visible
                        }
                        ControllerVisibility.PartiallyVisible -> ControllerVisibility.Visible
                        ControllerVisibility.Invisible -> ControllerVisibility.Visible
                    }
                }
            }
    ) {
        // video
        Box(modifier = Modifier
            .align(Alignment.Center)
            .run {
                if (state.contentAspectRatio <= 0) fillMaxSize()
                else resize(state.contentAspectRatio, resizeMode)
            }
        ) {
            VideoSurface(
                state = state,
                surfaceType = surfaceType,
                modifier = Modifier
                    .testTag(TestTag_VideoSurface)
                    .fillMaxSize()
            )

            // shutter
            if (state.closeShutter) {
                Spacer(
                    modifier = Modifier
                        .testTag(TestTag_Shutter)
                        .fillMaxSize()
                        .background(shutterColor)
                )
            }
            LaunchedEffect(keepContentOnPlayerReset) {
                snapshotFlow { state.isVideoTrackSelected }
                    .collect { isVideoTrackSelected ->
                        when (isVideoTrackSelected) {
                            // non video track is selected, so the shutter must be closed
                            false -> state.closeShutter = true
                            // no track
                            // If keepContentOnPlayerReset is false, close shutter
                            // Otherwise, open it
                            null -> state.closeShutter = !keepContentOnPlayerReset
                            true -> {}
                        }
                    }
            }
            LaunchedEffect(Unit) {
                snapshotFlow { state.player }
                    .collect { player ->
                        val isNewPlayer = player != null
                        if (isNewPlayer && !keepContentOnPlayerReset) {
                            // hide any video from the previous player.
                            state.closeShutter = true
                        }
                    }
            }
        }

        // artwork in audio stream
        val artworkPainter: Painter? = when {
            // non video track is selected, can use artwork
            state.isVideoTrackSelected == false -> {
                val painter =
                    if (!useArtwork) null
                    else rememberBitmapPainter(state.artworkData) ?: defaultArtworkPainter
                state.artworkPainter = painter
                painter
            }
            keepContentOnPlayerReset -> state.artworkPainter
            else -> null
        }
        if (artworkPainter != null) {
            Image(
                painter = artworkPainter,
                contentDescription = null,
                modifier = Modifier
                    .testTag(TestTag_Artwork)
                    .fillMaxSize(),
                contentScale = resizeMode.contentScale
            )
        }

        // subtitles
        if (subtitles != null) {
            val cues = state.playerState?.cues.takeIf { !it.isNullOrEmpty() } ?: emptyList()
            subtitles(cues)
        }

        // buffering
        val isBufferingShowing by remember(showBuffering) {
            derivedStateOf {
                state.playerState?.run {
                    playbackState == Player.STATE_BUFFERING
                            && (showBuffering == ShowBuffering.Always
                            || (showBuffering == ShowBuffering.WhenPlaying && playWhenReady))
                } ?: false
            }
        }
        if (isBufferingShowing) buffering?.invoke()

        // error message
        if (errorMessage != null) {
            state.playerError?.run { errorMessage(this) }
        }

        // overlay
        overlay?.invoke()

        // controller
        if (controller != null) {
            state.controllerAutoShow = controllerAutoShow
            LaunchedEffect(Unit) {
                snapshotFlow { state.player }.collect { player ->
                    if (player != null) {
                        state.maybeShowController()
                    }
                }
            }
            controller(state)
        }
    }
}

@Composable
private fun VideoSurface(
    state: MediaState,
    surfaceType: SurfaceType,
    modifier: Modifier
) {
    val context = LocalContext.current
    key(surfaceType, context) {
        if (surfaceType != SurfaceType.None) {
            fun Player.clearVideoView(view: View) {
                when (surfaceType) {
                    SurfaceType.None -> throw IllegalStateException()
                    SurfaceType.SurfaceView -> clearVideoSurfaceView(view as SurfaceView)
                    SurfaceType.TextureView -> clearVideoTextureView(view as TextureView)
                }
            }

            fun Player.setVideoView(view: View) {
                when (surfaceType) {
                    SurfaceType.None -> throw IllegalStateException()
                    SurfaceType.SurfaceView -> setVideoSurfaceView(view as SurfaceView)
                    SurfaceType.TextureView -> setVideoTextureView(view as TextureView)
                }
            }

            val videoView = remember {
                when (surfaceType) {
                    SurfaceType.None -> throw IllegalStateException()
                    SurfaceType.SurfaceView -> SurfaceView(context)
                    SurfaceType.TextureView -> TextureView(context)
                }
            }
            AndroidView(
                factory = { videoView },
                modifier = modifier,
            ) {
                // update player
                val currentPlayer = state.player
                val previousPlayer = it.tag as? Player
                if (previousPlayer === currentPlayer) return@AndroidView

                previousPlayer?.clearVideoView(it)

                it.tag = currentPlayer?.apply {
                    setVideoView(it)
                }
            }
            DisposableEffect(Unit) {
                onDispose {
                    (videoView.tag as? Player)?.clearVideoView(videoView)
                }
            }
        }
    }
}

internal const val TestTag_VideoSurface = "VideoSurface"
internal const val TestTag_Shutter = "Shutter"
internal const val TestTag_Artwork = "Artwork"

@Composable
private fun rememberBitmapPainter(artworkData: ByteArray?): BitmapPainter? {
    return if (artworkData == null) null
    else remember(artworkData) {
        BitmapFactory.decodeByteArray(artworkData, 0, artworkData.size)
            ?.asImageBitmap()
            ?.run { BitmapPainter(this) }
    }
}
