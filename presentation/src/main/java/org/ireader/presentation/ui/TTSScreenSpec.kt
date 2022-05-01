package org.ireader.presentation.ui

import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.DrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import org.ireader.core_api.log.Log
import org.ireader.domain.services.tts_service.Player
import org.ireader.domain.services.tts_service.media_player.isPlaying
import org.ireader.domain.ui.NavigationArgs
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
                "https://www.ireader.org/tts_screen_route/{bookId}/{chapterId}/{sourceId}/{readingParagraph}}"
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
    )

    @OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class)
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
    ) {
        val vm: TTSViewModel = hiltViewModel()
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        val sliderInteractionSource = MutableInteractionSource()
        val isSliderDragging = sliderInteractionSource.collectIsDraggedAsState()

        val pagerState = rememberPagerState()

        val drawerScrollState = rememberLazyListState()
        val textScroll = rememberScrollState()
        val chapter = vm.ttsChapter
        val chapters = vm.ttsChapters
        val scaffoldState = rememberScaffoldState()
        val bottomSheetState =
            rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
        LaunchedEffect(key1 = true) {
            vm.initMedia(context)
        }

        DisposableEffect(key1 = true) {
            onDispose {
                vm.browser.disconnect()
            }
        }

        vm.ttsSource?.let { source ->
            TTSScreen(
                vm = vm,
                onPrev = {
                    vm.controller?.transportControls?.skipToPrevious()
                },
                onPlay = {
                    if (vm.controller?.playbackState?.state == PlaybackStateCompat.STATE_NONE) {
                        vm.runTTSService(Player.PLAY)
                    } else if (vm.controller?.playbackState?.isPlaying == true) {
                        vm.controller?.transportControls?.stop()
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
                source = source,
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
                onPopStack = {
                    navController.popBackStack()
                },
                sliderInteractionSource = sliderInteractionSource,
                pagerState = pagerState,
                drawerScrollState = drawerScrollState,
                scaffoldState = scaffoldState,
                bottomSheetState = bottomSheetState,
                onAutoNextChapterToggle = {
                    vm.autoNextChapter = !vm.autoNextChapter
                    vm.speechPrefUseCases.saveAutoNext(vm.autoNextChapter)
                },
                onVoice = { voice ->
                    vm.currentVoice = voice.locale.displayName
                    vm.speechPrefUseCases.saveVoice(voice.locale.displayName)
                },
                onLanguage = { language ->
                    vm.currentLanguage = language.displayName
                    vm.speechPrefUseCases.saveLanguage(language.displayName)
                },
                onSpeechRateChange = { isIncreased ->
                    when (isIncreased) {
                        true -> {
                            if (vm.speechSpeed < 3.0f) {
                                vm.speechSpeed += .1f
                                vm.speechSpeed =
                                    vm.speechSpeed.toBigDecimal().setScale(1, RoundingMode.FLOOR)
                                        .toFloat()
                                vm.speechPrefUseCases.saveRate(vm.speechSpeed)
                            }
                        }
                        else -> {
                            if (vm.speechSpeed >= .5F) {
                                vm.speechSpeed -= .1f
                                vm.speechSpeed =
                                    vm.speechSpeed.toBigDecimal().setScale(1, RoundingMode.FLOOR)
                                        .toFloat()
                                vm.speechPrefUseCases.saveRate(vm.speechSpeed)
                            }
                        }
                    }

                },
                onSpeechPitchChange = { isIncreased ->
                    when (isIncreased) {
                        true -> {
                            if (vm.pitch <= 2.0F) {
                                vm.pitch += .1f
                                vm.pitch =
                                    vm.pitch.toBigDecimal().setScale(1, RoundingMode.FLOOR)
                                        .toFloat()
                                vm.speechPrefUseCases.savePitch(vm.pitch)
                            }
                        }
                        else -> {
                            if (vm.pitch >= .5F) {
                                vm.pitch -= .1f
                                vm.pitch =
                                    vm.pitch.toBigDecimal().setScale(1, RoundingMode.FLOOR)
                                        .toFloat()
                                vm.speechPrefUseCases.savePitch(vm.pitch)
                            }
                        }
                    }
                },
                onDrawerReverseIcon = {
                    if (vm.ttsChapter != null) {
                        vm.isDrawerAsc = !vm.isDrawerAsc
                    }
                }
            )
        }
        LaunchedEffect(key1 = scaffoldState.drawerState.targetValue) {
            if (chapter != null && scaffoldState.drawerState.targetValue == DrawerValue.Open && vm.ttsChapters.isNotEmpty()) {

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
}