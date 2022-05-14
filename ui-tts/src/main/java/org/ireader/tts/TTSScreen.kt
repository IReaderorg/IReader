package org.ireader.tts

import android.speech.tts.Voice
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import org.ireader.reader.ReaderScreenDrawer
import org.ireader.reader.components.SettingItemComposable
import org.ireader.reader.components.SettingItemToggleComposable
import java.util.Locale

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
    source: Source,
    drawerState:DrawerState,
    onChapter: (Chapter) -> Unit,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    onMap: (LazyListState) -> Unit,
    onPopStack: () -> Unit,
    sliderInteractionSource: MutableInteractionSource,
    bottomSheetState : ModalBottomSheetState,
    drawerScrollState:LazyListState,
    pagerState:PagerState,
    onAutoNextChapterToggle:(Boolean) -> Unit,
    onVoice:(Voice) -> Unit,
    onLanguage:(Locale) -> Unit,
    onSpeechRateChange:(isIncreased: Boolean) -> Unit,
    onSpeechPitchChange:(isIncreased: Boolean) -> Unit,
    onDrawerReverseIcon:() -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ModalBottomSheetLayout(
        modifier = Modifier.systemBarsPadding(),
        sheetContent = {
            Box(modifier.defaultMinSize(minHeight = 1.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    VoiceChip(
                        viewModel = vm,
                        modifier = Modifier.height(32.dp),
                        onVoice = onVoice
                    )
                    LanguageChip(
                        viewModel = vm,
                        modifier = Modifier.height(32.dp),
                        onLanguage = onLanguage
                    )
                    SettingItemToggleComposable(
                        text = UiText.StringResource( R.string.auto_next_chapter),
                        value = vm.autoNextChapter,
                        onToggle = onAutoNextChapterToggle
                    )
                    SettingItemComposable(
                        text =UiText.StringResource( R.string.auto_next_chapter),
                        value = UiText.DynamicString(vm.speechSpeed.toString()),
                        onAdd = {
                            onSpeechRateChange(true)

                        },
                        onMinus = {
                            onSpeechRateChange(false)
                        }
                    )

                    SettingItemComposable(
                        text =UiText.StringResource( R.string.pitch),
                        value = UiText.DynamicString(vm.pitch.toString()),
                        onAdd = {
                            onSpeechPitchChange(true)
                        },
                        onMinus = {
                            onSpeechPitchChange(false)
                        }
                    )
                }
            }
        },
        sheetState = bottomSheetState,
        sheetBackgroundColor = MaterialTheme.colorScheme.background,
        sheetContentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
            ReaderScreenDrawer(
                modifier = Modifier.statusBarsPadding(),
                onReverseIcon = onDrawerReverseIcon,
                onChapter = onChapter,
                chapter = vm.ttsChapter,
                chapters = vm.uiChapters.value,
                drawerScrollState = drawerScrollState,
                onMap = onMap,
            )
        }) {

        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                AppIconButton(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 4.dp),
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource( R.string.return_to_previous_screen),
                    onClick = onPopStack
                )

                Column(
                    modifier = Modifier
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
                                        .weight(6f)
                                        .padding(top = 16.dp),
                                    verticalArrangement = Arrangement.Top,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    BookImageComposable(
                                        image = BookCover.from(book),
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .height(180.dp)
                                            .width(120.dp)
                                            .clip(MaterialTheme.shapes.medium)
                                            .border(
                                                2.dp,
                                                MaterialTheme.colorScheme.onBackground.copy(alpha = .2f)
                                            ),
                                        contentScale = ContentScale.Crop,
                                    )

                                    BigSizeTextComposable(
                                        text = chapter.name,
                                        align = TextAlign.Center,
                                        maxLine = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    MidSizeTextComposable(
                                        text = UiText.DynamicString(book.title),
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
                                            .height(200.dp)
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
                                        .weight(6f)
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
                                    Spacer(modifier = Modifier.height(32.dp))
                                    TTLScreenPlay(
                                        modifier = Modifier.weight(8f),
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
                contentDescription = UiText.DynamicString(string(id =  R.string.content).uppercase()).asString(
                    LocalContext.current)
            )
            Spacer(modifier = Modifier.width(16.dp))
            MidSizeTextComposable(text = UiText.DynamicString(string(id = R.string.content).uppercase()))
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
                contentDescription = UiText.DynamicString(string(id = R.string.settings).uppercase()).asString(
                    LocalContext.current)
            )
            Spacer(modifier = Modifier.width(16.dp))
            MidSizeTextComposable(text = UiText.DynamicString(UiText.DynamicString(string(id = R.string.settings).uppercase()).asString(
                LocalContext.current)))
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
                   contentDescription = stringResource( R.string.previous_chapter),
                    onClick = onPrev,
                    tint = MaterialTheme.colorScheme.onBackground
                )
                AppIconButton(
                    modifier = Modifier.size(50.dp),
                    imageVector = Icons.Filled.FastRewind,
                   contentDescription = stringResource( R.string.previous_paragraph),
                    onClick = onPrevPar,
                    tint = MaterialTheme.colorScheme.onBackground
                )
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        vm.isLoading.value -> {
                            ShowLoading()
                        }
                        vm.isPlaying -> {
                            AppIconButton(
                                modifier = Modifier.size(80.dp),
                                imageVector = Icons.Filled.Pause,
                               contentDescription = stringResource( R.string.play),
                                onClick = onPlay,
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        else -> {
                            AppIconButton(
                                modifier = Modifier.size(80.dp),
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = stringResource( R.string.pause),
                                onClick = onPlay,
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                AppIconButton(
                    modifier = Modifier.size(50.dp),
                    imageVector = Icons.Filled.FastForward,
                   contentDescription = stringResource( R.string.next_paragraph),
                    onClick = onNextPar,
                    tint = MaterialTheme.colorScheme.onBackground
                )
                AppIconButton(
                    modifier = Modifier.size(50.dp),
                    imageVector = Icons.Filled.SkipNext,
                   contentDescription = stringResource( R.string.next_chapter),
                    onClick = onNext,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
