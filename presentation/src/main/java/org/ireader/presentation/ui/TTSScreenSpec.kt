package org.ireader.presentation.ui

import android.support.v4.media.session.PlaybackStateCompat
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
import kotlinx.coroutines.launch
import org.ireader.domain.services.tts_service.Player
import org.ireader.domain.services.tts_service.media_player.isPlaying
import org.ireader.domain.ui.NavigationArgs
import org.ireader.tts.TTSScreen
import org.ireader.tts.TTSViewModel

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

    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
    ) {
        val vm: TTSViewModel = hiltViewModel()
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        LaunchedEffect(key1 = true, ) {
            vm.initMedia(context)
        }

        DisposableEffect(key1 = true ) {
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
                    vm.controller?.transportControls?.stop()
                    vm.currentReadingParagraph = it.toInt()
                },
                onValueChangeFinished = {
                    if (vm.isPlaying) {
                        vm.controller?.transportControls?.stop()
                        vm.controller?.transportControls?.seekTo(vm.currentReadingParagraph.toLong())
                    }
                },
                onMap = { drawer ->
                    scope.launch {
                        try {
                            val index =
                                vm.ttsState.ttsChapters.indexOfFirst { it.id == vm.ttsChapter?.id }
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
                }
            )
        }
    }
}