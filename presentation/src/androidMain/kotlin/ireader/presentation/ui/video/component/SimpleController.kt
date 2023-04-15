package ireader.presentation.ui.video.component

import android.text.format.DateUtils
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ireader.i18n.R
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.video.component.core.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * A simple controller, which consists of a play/pause button and a time bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleController(
    title: String,
    mediaState: MediaState,
    modifier: Modifier = Modifier,
    onShowMenu:() -> Unit
) {
    val maxDuration = remember {
        mutableStateOf("")
    }

    Crossfade(targetState = mediaState.isControllerShowing, modifier) { isShowing ->

        if (isShowing) {
            val controllerState = rememberControllerState(mediaState)
            val maxDurationInFormat = remember {
                getMaxDuration(controllerState.durationMs, maxDuration)
            }
            val currentDuration = remember(controllerState.positionMs) {
                mutableStateOf(formatDuration(
                    controllerState.positionMs,
                ))
            }
            var scrubbing by remember { mutableStateOf(false) }
            val hideWhenTimeout = !mediaState.shouldShowControllerIndefinitely && !scrubbing
            var hideEffectReset by remember { mutableStateOf(0) }
            LaunchedEffect(hideWhenTimeout, hideEffectReset) {
                if (hideWhenTimeout) {
                    // hide after 3s
                    delay(3000)
                    mediaState.isControllerShowing = false
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x98000000))
            ) {
                Toolbar(title = {
                    MidSizeTextComposable(text = title)
                }, backgroundColor = Color.Transparent, contentColor = Color.White, actions = {
                    AppIconButton(imageVector = Icons.Default.Menu, onClick = onShowMenu, tint = Color.White)
                })



                Row(modifier = Modifier.align(Alignment.Center)) {
                    AppIconButton(painter = painterResource(R.drawable.go_back_30), modifier = Modifier
                        .size(52.dp), onClick = {
                        mediaState.player?.currentPosition?.let { position ->
                            controllerState.seekTo(position.minus(30000L))
                        }

                    }, tint = Color.White.copy(alpha = .9f)
                    )
                    Spacer(modifier = Modifier.width(30.dp))
                    Image(
                        imageVector =
                        if (controllerState.showPause) Icons.Default.Pause
                        else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier
                            .size(52.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                hideEffectReset++
                                controllerState.playOrPause()
                            },
                        colorFilter = ColorFilter.tint(Color.White.copy(alpha = 1f))
                    )
                    Spacer(modifier = Modifier.width(30.dp))
                    AppIconButton(painter = painterResource(R.drawable.go_forward_30), modifier = Modifier
                        .size(52.dp), onClick = {
                        mediaState.player?.currentPosition?.let { position ->
                            controllerState.seekTo(position.plus(30000L))
                        }
                    }, tint = Color.White.copy(alpha = .9f)
                    )
                }


                LaunchedEffect(Unit) {
                    while (true) {
                        delay(200)
                        controllerState.triggerPositionUpdate()
                    }
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(vertical = 16.dp)
                        .fillMaxWidth()

                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MidSizeTextComposable(
                            modifier = Modifier
                                .padding(horizontal = 10.dp),
                            text = currentDuration.value.plus("/$maxDurationInFormat"),
                            color = Color.White
                        )
                        AppIconButton(imageVector =  if (mediaState.playerState?.isFulLScreen == true) Icons.Default.FullscreenExit else Icons.Default.Fullscreen, onClick = {
                            mediaState.playerState?.isFulLScreen = !(mediaState.playerState?.isFulLScreen ?: false)
                        }, tint = Color.White)

                    }

                    TimeBar(
                        controllerState.durationMs,
                        controllerState.positionMs,
                        controllerState.bufferedPositionMs,
                        modifier = Modifier
                            .systemGestureExclusion()
                            .fillMaxWidth()
                            .height(28.dp),
                        contentPadding = PaddingValues(12.dp),
                        scrubberCenterAsAnchor = true,
                        onScrubStart = { scrubbing = true },
                        onScrubStop = { positionMs ->
                            scrubbing = false
                            controllerState.seekTo(positionMs)
                        },
                        scrubber = { enabled, scrubbing ->
                            TimeBarScrubber(
                                enabled,
                                scrubbing,
                                draggedSize = 20.dp,
                                color = Color.Red
                            )
                        },
                        progress = { current, _, buffered ->
                            TimeBarProgress(current, buffered, playedColor = Color.Red)
                        }
                    )
                }
            }
        }
    }
}

fun getMaxDuration(time: Long, lastTime: MutableState<String>): String {
    if (lastTime.value != "") return lastTime.value
    if (time < 0) return "00:00"
    val dd = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    return dd.format(time - TimeZone.getDefault().rawOffset)
}


private fun formatDuration(totalSecsInMillis: Long): String {
    return DateUtils.formatElapsedTime(totalSecsInMillis / 1000)
}
