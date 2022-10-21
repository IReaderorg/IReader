package ireader.presentation.ui.video.component.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.Timeline

/**
 * Create and [remember] a [ControllerState] instance.
 */
@Composable
fun rememberControllerState(
    mediaState: MediaState
): ControllerState {
    return remember { ControllerState(mediaState) }
}

/**
 * Create a [ControllerState] instance.
 */
fun ControllerState(mediaState: MediaState): ControllerState {
    return ControllerState(mediaState.stateOfPlayerState)
}

@Stable
class ControllerState internal constructor(
    stateOfPlayerState: State<PlayerState?>
) {
    private val playerState: PlayerState? by stateOfPlayerState
    private val player: Player? get() = playerState?.player

    /**
     * If ture, show pause button. Otherwise, show play button.
     */
    val showPause: Boolean by derivedStateOf {
        playerState?.run {
            playbackState != Player.STATE_ENDED
                    && playbackState != Player.STATE_IDLE
                    && playWhenReady
        } ?: false
    }

    /**
     * Play or pause the player.
     */
    fun playOrPause() {
        player?.run {
            if (playbackState == Player.STATE_IDLE
                || playbackState == Player.STATE_ENDED
                || !playWhenReady
            ) {
                if (playbackState == Player.STATE_IDLE) {
                    prepare()
                } else if (playbackState == Player.STATE_ENDED) {
                    seekTo(currentMediaItemIndex, C.TIME_UNSET)
                }
                play()
            } else {
                pause()
            }
        }
    }

    /**
     * The duration, in milliseconds. Return [C.TIME_UNSET] if it's unset or unknown.
     */
    val durationMs: Long by derivedStateOf {
        windowOffsetAndDurations
            ?.run {
                if (multiWindowTimeBar) this.lastOrNull()?.run { first + second }
                else this[playerState?.mediaItemIndex!!].second
            } ?: C.TIME_UNSET
    }

    /**
     * The current position, in milliseconds.
     */
    val positionMs: Long by derivedStateOf {
        positionUpdateTrigger
        playerState?.run { currentWindowOffset + player.contentPosition } ?: 0L
    }

    /**
     * The current buffered position, in milliseconds.
     */
    val bufferedPositionMs: Long by derivedStateOf {
        positionUpdateTrigger
        playerState?.run { currentWindowOffset + player.contentBufferedPosition } ?: 0L
    }

    /**
     * Whether the time bar should show all windows, as opposed to just the current one. If the
     * timeline has a period with unknown duration or more than
     * [MAX_WINDOWS_FOR_MULTI_WINDOW_TIME_BAR] windows the time bar will fall back to showing
     * a single window.
     */
    var showMultiWindowTimeBar: Boolean by mutableStateOf(false)

    fun triggerPositionUpdate() {
        positionUpdateTrigger++
    }

    fun seekTo(positionMs: Long) {
        playerState?.run {
            var position = positionMs
            var windowIndex = mediaItemIndex
            if (multiWindowTimeBar) {
                val windowCount = timeline.windowCount
                windowIndex = 0
                while (true) {
                    val windowDurationMs = timeline.getWindow(windowIndex, window).durationMs
                    if (position < windowDurationMs) {
                        break
                    } else if (windowIndex == windowCount - 1) {
                        // Seeking past the end of the last window should seek to the end of the timeline.
                        position = windowDurationMs
                        break
                    }
                    position -= windowDurationMs
                    windowIndex++
                }
            }
            player.seekTo(windowIndex, position)
            triggerPositionUpdate()
        }
    }

    private var positionUpdateTrigger by mutableStateOf(0L)

    private val window: Timeline.Window = Timeline.Window()
    private val Timeline.windows: Sequence<Timeline.Window>
        get() = sequence {
            for (index in 0 until windowCount) {
                getWindow(index, window)
                yield(window)
            }
        }
    private val Timeline.canShowMultiWindowTimeBar: Boolean
        get() = windowCount <= MAX_WINDOWS_FOR_MULTI_WINDOW_TIME_BAR
                && windows.all { it.durationUs != C.TIME_UNSET }
    private val multiWindowTimeBar: Boolean by derivedStateOf {
        showMultiWindowTimeBar && (playerState?.timeline?.canShowMultiWindowTimeBar ?: false)
    }

    // Offset and duration pairs of all windows in current timeline.
    private val windowOffsetAndDurations: List<Pair<Long, Long>>? by derivedStateOf {
        playerState?.takeIf { !it.timeline.isEmpty }?.run {
            if (multiWindowTimeBar) {
                timeline.windows.fold(mutableListOf()) { acc, window ->
                    val windowOffset = acc.lastOrNull()?.run { first + second } ?: 0L
                    acc.add(windowOffset to window.durationMs)
                    acc
                }
            } else {
                timeline.windows.map { window -> 0L to window.durationMs }.toList()
            }
        }
    }

    // Current window offset, in milliseconds.
    private val currentWindowOffset: Long by derivedStateOf {
        windowOffsetAndDurations?.get(playerState?.mediaItemIndex!!)?.first ?: 0L
    }

    companion object {
        const val MAX_WINDOWS_FOR_MULTI_WINDOW_TIME_BAR = 100
    }
}
