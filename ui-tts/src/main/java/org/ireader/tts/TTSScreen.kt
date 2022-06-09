package org.ireader.tts

import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.launch
import org.ireader.common_models.entities.Chapter
import org.ireader.common_resources.UiText
import org.ireader.components.components.BookImageComposable
import org.ireader.components.components.ShowLoading
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.BigSizeTextComposable
import org.ireader.components.reusable_composable.MidSizeTextComposable
import org.ireader.components.reusable_composable.SuperSmallTextComposable
import org.ireader.core.R
import org.ireader.core_api.source.Source
import org.ireader.core_ui.ui.string
import org.ireader.domain.services.tts_service.TTSState
import org.ireader.image_loader.BookCover

@OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TTSScreen(
    modifier: Modifier = Modifier,
    vm: TTSState,
    onPrev: () -> Unit,
    onNextPar: () -> Unit,
    onPrevPar: () -> Unit,
    onPlay: () -> Unit,
    onNext: () -> Unit,
    source: Source?,
    drawerState: DrawerState,
    onChapter: (Chapter) -> Unit,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    onPopStack: () -> Unit,
    sliderInteractionSource: MutableInteractionSource,
    bottomSheetState: ModalBottomSheetState,
    pagerState: PagerState,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxHeight = remember {
            constraints.maxHeight
        }
        val maxWidth = remember {
            constraints.maxWidth
        }
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
        ) {
            Spacer(modifier = (Modifier.height(35.dp)))
            AppIconButton(
                modifier = Modifier,
                imageVector = Icons.Default.ArrowBack,
                contentDescription = stringResource(R.string.return_to_previous_screen),
                onClick = onPopStack
            )
        }
        Column(
            modifier = Modifier
                .padding(top = 35.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            vm.ttsBook?.let { book ->
                vm.ttsChapter?.let { chapter ->
                    vm.ttsContent?.value?.let { content ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            BookImageComposable(
                                image = BookCover.from(book),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .height((maxHeight / 15).dp)
                                    .width((maxWidth / 10).dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .border(
                                        2.dp,
                                        MaterialTheme.colorScheme.onBackground.copy(alpha = .2f)
                                    ),
                                contentScale = ContentScale.Crop,
                                useSavedCoverImage = true
                            )

                            BigSizeTextComposable(
                                text = chapter.name,
                                align = TextAlign.Center,
                                maxLine = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            MidSizeTextComposable(
                                text = book.title,
                                align = TextAlign.Center,
                                maxLine = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            SuperSmallTextComposable(text = "${vm.currentReadingParagraph + 1}/${vm.ttsContent?.value?.size ?: 0L}")
                        }
                        HorizontalPager(
                            modifier = Modifier.weight(6f), count = chapter.content.size,
                            state = pagerState
                        ) { index ->
                            Text(
                                modifier = modifier
                                    .padding(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    )
                                    .fillMaxWidth()
                                    .height((maxHeight / 15).dp)
                                    .verticalScroll(rememberScrollState())
                                    .align(Alignment.CenterHorizontally),
                                text = if (content.isNotEmpty() && vm.currentReadingParagraph <= content.lastIndex && index <= content.lastIndex) content[index] else "",
                                fontSize = vm.fontSize.sp,
                                fontFamily = vm.font.fontFamily,
                                textAlign = TextAlign.Start,
                                color = MaterialTheme.colorScheme.onBackground,
                                lineHeight = vm.lineHeight.sp,
                                maxLines = 12,
                            )
                        }

                        Column(
                            modifier = Modifier
                                .height((maxHeight / 10).dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            TTLScreenSetting(
                                modifier = Modifier.weight(4f),
                                onSetting = {
                                    scope.launch {
                                        bottomSheetState.show()
                                    }
                                },
                                onContent = {
                                    scope.launch {
                                        drawerState.animateTo(
                                            androidx.compose.material3.DrawerValue.Open,
                                            TweenSpec()
                                        )
                                    }
                                }
                            )
                            //   Spacer(modifier = Modifier.height(32.dp))
                            TTLScreenPlay(
                                modifier = Modifier
                                    .height((maxHeight / 15).dp),
                                onPlay = onPlay,
                                onNext = onNext,
                                onPrev = onPrev,
                                vm = vm,
                                onNextPar = onNextPar,
                                onPrevPar = onPrevPar,
                                content = content,
                                onValueChange = onValueChange,
                                onValueChangeFinished = onValueChangeFinished,
                                sliderInteractionSource = sliderInteractionSource
                            )
                        }
                    }
                }

            }
        }
    }
}

@Composable
private fun TTLScreenSetting(
    modifier: Modifier = Modifier,
    onSetting: () -> Unit,
    onContent: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = MaterialTheme.colorScheme.onBackground.copy(.1f)),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clickable { onContent() }
                .weight(1f)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(40.dp), imageVector = Icons.Default.List,
                contentDescription = UiText.DynamicString(string(id = R.string.content).uppercase())
                    .asString(
                        LocalContext.current
                    )
            )
            Spacer(modifier = Modifier.width(16.dp))
            MidSizeTextComposable(text = string(id = R.string.content).uppercase())
        }
        Divider(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
        )
        Row(
            modifier = Modifier
                .clickable { onSetting() }
                .weight(1f)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(40.dp), imageVector = Icons.Default.Settings,
                contentDescription = UiText.DynamicString(string(id = R.string.settings).uppercase())
                    .asString(
                        LocalContext.current
                    )
            )
            Spacer(modifier = Modifier.width(16.dp))
            MidSizeTextComposable(
                text =
                string(id = R.string.settings).uppercase()

            )
        }
    }
}

@Composable
private fun TTLScreenPlay(
    modifier: Modifier = Modifier,
    vm: TTSState,
    content: List<String>?,
    onPrev: () -> Unit,
    onPlay: () -> Unit,
    onNext: () -> Unit,
    onNextPar: () -> Unit,
    onPrevPar: () -> Unit,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    sliderInteractionSource: MutableInteractionSource
) {
    Column(modifier = modifier.fillMaxWidth()) {
        content?.let { chapter ->
            Slider(

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                value = if (content.isEmpty()) 0F else vm.uiPage.value.toFloat(),
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
                interactionSource = sliderInteractionSource
            )
        }

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .padding(bottom = 16.dp, top = 4.dp)
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
    }
}
