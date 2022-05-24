package org.ireader.presentation.ui

import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import org.ireader.components.components.component.ChipChoicePreference
import org.ireader.components.components.component.SliderPreference
import org.ireader.components.components.component.SwitchPreference
import org.ireader.core.R
import org.ireader.core_api.log.Log
import org.ireader.domain.services.tts_service.Player
import org.ireader.domain.services.tts_service.media_player.isPlaying
import org.ireader.domain.ui.NavigationArgs
import org.ireader.reader.ReaderScreenDrawer
import org.ireader.tts.TTSScreen
import org.ireader.tts.TTSViewModel
import java.math.RoundingMode

object TTSScreenSpec : ScreenSpec {
    override val navHostRoute: String = "tts_screen_route/{bookId}/{chapterId}/{sourceId}"

    fun buildRoute(
        bookId: Long,
        sourceId: Long,
        chapterId: Long,
    ): String {
        return "tts_screen_route/$bookId/$chapterId/$sourceId"
    }

    fun buildDeepLink(
        bookId: Long,
        sourceId: Long,
        chapterId: Long,
        readingParagraph: Long,
    ): String {
        return "https://www.ireader.org/tts_screen_route/$bookId/$chapterId/$sourceId/$readingParagraph"
    }

    override val deepLinks: List<NavDeepLink> = listOf(

        navDeepLink {
            uriPattern =
                "https://www.ireader.org/tts_screen_route/{bookId}/{chapterId}/{sourceId}/{readingParagraph}"
            NavigationArgs.bookId
            NavigationArgs.chapterId
            NavigationArgs.sourceId
            NavigationArgs.readingParagraph
        }
    )
    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.bookId,
        NavigationArgs.chapterId,
        NavigationArgs.sourceId,
        NavigationArgs.showModalSheet,
        NavigationArgs.haveDrawer,

        )

    @OptIn(
        ExperimentalMaterialApi::class, ExperimentalPagerApi::class,
        ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content(
        controller: ScreenSpec.Controller
    ) {
        val vm: TTSViewModel = hiltViewModel   (controller.navBackStackEntry)
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        val sliderInteractionSource = MutableInteractionSource()
        val isSliderDragging = sliderInteractionSource.collectIsDraggedAsState()

        val pagerState = rememberPagerState()

        val drawerScrollState = rememberLazyListState()
        LaunchedEffect(key1 = controller.drawerState.hashCode()) {
            vm.drawerState = drawerScrollState
        }

        val chapter = vm.ttsChapter

        LaunchedEffect(key1 = true) {
            vm.initMedia(context)
        }

        DisposableEffect(key1 = true) {
            onDispose {
                vm.browser?.disconnect()
            }
        }

        TTSScreen(
            modifier = Modifier,
            vm = vm,
            onPrev = {
                vm.controller?.transportControls?.skipToPrevious()
            },
            onPlay = {
                if (vm.controller?.playbackState?.state == PlaybackStateCompat.STATE_NONE) {
                    vm.initMedia(context)
                    vm.initController()
                    vm.runTTSService(Player.PLAY)
                } else if (vm.controller?.playbackState?.isPlaying == true) {
                    vm.controller?.transportControls?.pause()
                } else {
                    vm.controller?.transportControls?.play()
                }
            },
            onNext = {
                vm.controller?.transportControls?.skipToNext()
            },
            onChapter = { ch ->
                vm.getLocalChapter(ch.id)
            },
            source = vm.ttsSource,
            onPrevPar = {
                vm.controller?.transportControls?.rewind()
            },
            onNextPar = {
                vm.controller?.transportControls?.fastForward()
            },
            onValueChange = {
                vm.controller?.transportControls?.seekTo(it.toLong())
            },
            onValueChangeFinished = {},
            onPopStack = {
                controller.navController.popBackStack()
            },
            sliderInteractionSource = sliderInteractionSource,
            pagerState = pagerState,
            drawerScrollState = drawerScrollState,
            bottomSheetState = controller.sheetState,
            drawerState = controller.drawerState
        )
        LaunchedEffect(key1 = controller.drawerState.targetValue) {
            if (chapter != null && controller.drawerState.targetValue == androidx.compose.material3.DrawerValue.Open && vm.ttsChapters.isNotEmpty()) {

                val index = vm.ttsChapters.indexOfFirst { it.id == chapter.id }
                if (index != -1) {
                    drawerScrollState.scrollToItem(index)
                }
            }
        }


        LaunchedEffect(key1 = vm.ttsState.currentReadingParagraph) {
            try {
                if (vm.currentReadingParagraph != pagerState.targetPage && vm.currentReadingParagraph != vm.prevPar && vm.ttsState.currentReadingParagraph < pagerState.pageCount) {
                    pagerState.scrollToPage(vm.ttsState.uiPage.value)
                    if (!isSliderDragging.value) {
                        vm.controller?.transportControls?.seekTo(vm.ttsState.currentReadingParagraph.toLong())
                    }
                }
            } catch (e: Throwable) {
                Log.error(e, "")
            }
        }
        LaunchedEffect(key1 = pagerState.currentPage) {
            if (vm.currentReadingParagraph != pagerState.currentPage) {
                vm.currentReadingParagraph = pagerState.currentPage

                vm.controller?.transportControls?.seekTo(pagerState.currentPage.toLong())

                vm.prevPar = pagerState.currentPage
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun ModalDrawer(
        controller: ScreenSpec.Controller
    ) {
        val vm: TTSViewModel = hiltViewModel   (controller.navBackStackEntry)
        val scope = rememberCoroutineScope()
        ReaderScreenDrawer(
            modifier = Modifier.statusBarsPadding(),
            onReverseIcon = {
                if (vm.ttsChapter != null) {
                    vm.isDrawerAsc = !vm.isDrawerAsc
                }
            },
            onChapter = { ch ->
                vm.getLocalChapter(ch.id)
            },
            chapter = vm.ttsChapter,
            chapters = vm.uiChapters.value,
            drawerScrollState = vm.drawerState ?: rememberLazyListState(),
            onMap = { drawer ->
                scope.launch {
                    try {
                        val index =
                            vm.ttsState.uiChapters.value.indexOfFirst { it.id == vm.ttsChapter?.id }
                        if (index != -1) {
                            drawer.scrollToItem(
                                index,
                                -drawer.layoutInfo.viewportEndOffset / 2
                            )
                        }
                    } catch (e: Throwable) {
                    }
                }
            },
        )
    }

    @OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun BottomModalSheet(
        controller: ScreenSpec.Controller
    ) {
        val vm: TTSViewModel = hiltViewModel   (controller.navBackStackEntry)

        LaunchedEffect(key1 = controller.sheetState.hashCode()) {
            controller.sheetState.hide()
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            ChipChoicePreference(
                preference = vm.voice,
                choices = vm.uiVoices.associate { voice ->
                    return@associate voice to voice.localDisplayName
                },
                title = stringResource(id = R.string.voices)
            )
            ChipChoicePreference(
                preference = vm.language,
                choices = vm.languages.associate { language ->
                    return@associate language.displayName to language.displayName
                },
                title = stringResource(id = R.string.languages)
            )
            SwitchPreference(preference = vm.autoNext, title = stringResource(id = R.string.auto_next_chapter))
            SliderPreference(
                title = stringResource(R.string.speech_rate),
                preferenceAsFloat = vm.speechRate,
                valueRange = .5F..3F,
                trailing = vm.speechRate.value.toBigDecimal().setScale(1, RoundingMode.FLOOR).toString()
            )
            SliderPreference(
                title = stringResource(R.string.pitch),
                preferenceAsFloat = vm.speechPitch,
                valueRange = .5F..2.1F,
                trailing = vm.speechPitch.value.toBigDecimal().setScale(1, RoundingMode.FLOOR).toString()
            )
            SwitchPreference(preference = vm.sleepModeUi, title = stringResource(id = R.string.enable_sleep_timer))
            SliderPreference(
                title = stringResource(R.string.sleep),
                preferenceAsLong = vm.sleepTimeUi,
                valueRange = 0F..60F,
                trailing = "${vm.sleepTimeUi.value.toInt()} M",
                isEnable = vm.sleepModeUi.value
            )

        }
    }
}