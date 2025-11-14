package ireader.presentation.ui.home.tts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.core.source.Source
import ireader.domain.models.entities.Chapter
import ireader.domain.models.prefs.mapAlignment
import ireader.domain.models.prefs.mapTextAlign
import ireader.domain.services.tts_service.TTSState
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.components.ShowLoading
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.SuperSmallTextComposable
import ireader.presentation.ui.core.modifier.clickableNoIndication
import ireader.presentation.core.toComposeColor
import ireader.presentation.core.toComposeAlignment
import ireader.presentation.core.toComposeFontFamily
import ireader.presentation.core.toComposeTextAlign


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TTSScreen(
        modifier: Modifier = Modifier,
        vm: TTSViewModel,
        source: Source?,
        drawerState: DrawerState,
        onChapter: (Chapter) -> Unit,
        onPopStack: () -> Unit,
        onPlay: () -> Unit,
        bottomSheetState: ModalBottomSheetState,
        lazyState: LazyListState,
        paddingValues: PaddingValues
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            vm.theme.value.backgroundColor.toComposeColor().copy(alpha = .8f),
            vm.theme.value.backgroundColor.toComposeColor().copy(alpha = .8f)
        ),
        startY = 1f,  // 1/3
        endY = 1f,
    )
    vm.ttsBook.let { book ->
        vm.ttsChapter.let { chapter ->
            (vm.ttsContent?.value ?: emptyList()).let { content ->
                Box(
                    modifier = modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(gradient)
                    )
                    LazyColumn(modifier = Modifier.matchParentSize(), state = lazyState, contentPadding = paddingValues) {
                        items(
                            count = chapter?.content?.size ?: 0
                        ) { index ->
                            androidx.compose.material.Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = vm.paragraphsIndent.value.dp)
                                    .clickableNoIndication(
                                        onClick = {
                                            vm.currentReadingParagraph = index

                                        },
                                        onLongClick = {
                                            vm.fullScreenMode = !vm.fullScreenMode
                                        }
                                    ),
                                text = if (content.isNotEmpty() && vm.currentReadingParagraph <= content.lastIndex && index <= content.lastIndex) content[index].plus(
                                    "\n".repeat(vm.paragraphDistance.value)
                                ) else "",
                                fontSize = vm.fontSize.value.sp,
                                fontFamily = vm.font?.value?.fontFamily?.toComposeFontFamily(),
                                textAlign = mapTextAlign(vm.textAlignment.value).toComposeTextAlign(),
                                color = vm.theme.value.onTextColor.toComposeColor().copy(alpha = if (index == vm.currentReadingParagraph) 1f else .6f),
                                lineHeight = vm.lineHeight.value.sp,
                                letterSpacing = vm.betweenLetterSpaces.value.sp,
                                fontWeight = FontWeight(vm.textWeight.value),
                            )
                        }
                    }

                    Box(modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()) {
                        Row(
                            horizontalArrangement = Arrangement.Center, modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                        ) {
                            SuperSmallTextComposable(
                                text = "${vm.currentReadingParagraph + 1}/${vm.ttsContent?.value?.size ?: 0L}",
                                color = vm.theme.value.onTextColor.toComposeColor(),
                            )
                        }
                        vm.ttsIconAlignments.value.mapAlignment()?.let { alignment ->
                            Row(modifier = Modifier
                                .align(alignment.toComposeAlignment())) {
                                AppIconButton(
                                    modifier = Modifier.size(48.dp), // Minimum 48dp touch target
                                    onClick = onPlay,
                                    imageVector = if (vm.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    tint = vm.theme.value.onTextColor.toComposeColor(),
                                    contentDescription = if (vm.isPlaying) "Pause" else "Play"
                                )
                                AppIconButton(
                                    modifier = Modifier.size(48.dp), // Minimum 48dp touch target
                                    onClick = { vm.fullScreenMode = !vm.fullScreenMode },
                                    imageVector = if (vm.fullScreenMode) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    tint = vm.theme.value.onTextColor.toComposeColor(),
                                    contentDescription = if (vm.fullScreenMode) "Exit fullscreen" else "Enter fullscreen"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TTLScreenPlay(
    modifier: Modifier = Modifier,
    vm: TTSState,
    content: List<String>?,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
    ) {
        content?.let { chapter ->
            Slider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                value = if (content.isEmpty()) 0F else vm.currentReadingParagraph.toFloat(),
                onValueChange = {
                    onValueChange(it)
                },
                onValueChangeFinished = {
                    onValueChangeFinished()
                },
                valueRange = 0f..(if (content.isNotEmpty()) content.lastIndex else 0).toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = .6f),
                    inactiveTickColor = MaterialTheme.colorScheme.onBackground.copy(alpha = .6f),
                    inactiveTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = .6f),
                    activeTickColor = MaterialTheme.colorScheme.primary.copy(alpha = .6f)
                ),
            )
        }

    }
}

/**
 * Media controllers with minimum 48dp touch targets for accessibility
 * Requirements: 16.1, 16.2, 16.3, 16.4, 16.5
 */
@Composable
fun MediaControllers(
    modifier: Modifier = Modifier,
    vm: TTSState,
    onPrev: () -> Unit,
    onPlay: () -> Unit,
    onNext: () -> Unit,
    onNextPar: () -> Unit,
    onPrevPar: () -> Unit,
) {
    Row(
        modifier = modifier
            .padding(top = 4.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        AppIconButton(
            modifier = Modifier.size(56.dp), // Increased from 50dp to 56dp for better touch target
            imageVector = Icons.Filled.SkipPrevious,
            contentDescription = localize(Res.string.previous_chapter),
            onClick = onPrev,
            tint = MaterialTheme.colorScheme.onBackground
        )
        AppIconButton(
            modifier = Modifier.size(56.dp), // Increased from 50dp to 56dp for better touch target
            imageVector = Icons.Filled.FastRewind,
            contentDescription = localize(Res.string.previous_paragraph),
            onClick = onPrevPar,
            tint = MaterialTheme.colorScheme.onBackground
        )
        Box {
            when {
                vm.isLoading.value -> {
                    Box(
                        modifier = Modifier.size(80.dp)
                    ) {
                        ShowLoading()
                    }
                }
                vm.isPlaying -> {
                    AppIconButton(
                        modifier = Modifier.size(80.dp),
                        imageVector = Icons.Filled.PauseCircle,
                        contentDescription = localize(Res.string.play),
                        onClick = onPlay,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                else -> {
                    AppIconButton(
                        modifier = Modifier.size(80.dp),
                        imageVector = Icons.Filled.PlayCircle,
                        contentDescription = localize(Res.string.pause),
                        onClick = onPlay,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        AppIconButton(
            modifier = Modifier.size(56.dp), // Increased from 50dp to 56dp for better touch target
            imageVector = Icons.Filled.FastForward,
            contentDescription = localize(Res.string.next_paragraph),
            onClick = onNextPar,
            tint = MaterialTheme.colorScheme.onBackground
        )
        AppIconButton(
            modifier = Modifier.size(56.dp), // Increased from 50dp to 56dp for better touch target
            imageVector = Icons.Filled.SkipNext,
            contentDescription = localize(Res.string.next_chapter),
            onClick = onNext,
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
}