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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import ireader.domain.models.entities.Chapter
import ireader.core.source.Source
import ireader.presentation.ui.core.modifier.clickableNoIndication
import ireader.domain.models.prefs.mapAlignment
import ireader.domain.models.prefs.mapTextAlign
import ireader.domain.services.tts_service.TTSState
import ireader.presentation.R
import ireader.presentation.ui.component.components.ShowLoading
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.SuperSmallTextComposable


@OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
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
            vm.theme.value.backgroundColor.copy(alpha = .8f),
            vm.theme.value.backgroundColor.copy(alpha = .8f)
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
                                fontFamily = vm.font.value.fontFamily,
                                textAlign = mapTextAlign(vm.textAlignment.value),
                                color = vm.theme.value.onTextColor.copy(alpha = if (index == vm.currentReadingParagraph) 1f else .6f),
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
                                color = vm.theme.value.onTextColor,
                            )
                        }
                        vm.ttsIconAlignments.value.mapAlignment()?.let { alignment ->
                            Row(modifier = Modifier
                                .align(alignment)) {
                                AppIconButton(
                                    onClick = onPlay,
                                    imageVector = if (vm.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    tint = vm.theme.value.onTextColor,
                                    contentDescription = null
                                )
                                AppIconButton(
                                    onClick = { vm.fullScreenMode = !vm.fullScreenMode },
                                    imageVector = if (vm.fullScreenMode) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    tint = vm.theme.value.onTextColor,
                                    contentDescription = null
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
            modifier = Modifier.size(50.dp),
            imageVector = Icons.Filled.SkipPrevious,
            contentDescription = stringResource(R.string.previous_chapter),
            onClick = onPrev,
            tint = MaterialTheme.colorScheme.onBackground
        )
        AppIconButton(
            modifier = Modifier.size(50.dp),
            imageVector = Icons.Filled.FastRewind,
            contentDescription = stringResource(R.string.previous_paragraph),
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
                        contentDescription = stringResource(R.string.play),
                        onClick = onPlay,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                else -> {
                    AppIconButton(
                        modifier = Modifier.size(80.dp),
                        imageVector = Icons.Filled.PlayCircle,
                        contentDescription = stringResource(R.string.pause),
                        onClick = onPlay,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        AppIconButton(
            modifier = Modifier.size(50.dp),
            imageVector = Icons.Filled.FastForward,
            contentDescription = stringResource(R.string.next_paragraph),
            onClick = onNextPar,
            tint = MaterialTheme.colorScheme.onBackground
        )
        AppIconButton(
            modifier = Modifier.size(50.dp),
            imageVector = Icons.Filled.SkipNext,
            contentDescription = stringResource(R.string.next_chapter),
            onClick = onNext,
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
}