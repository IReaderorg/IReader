package ireader.presentation.ui.reader

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Chapter
import ireader.domain.services.tts_service.DesktopTTSService
import ireader.presentation.ui.reader.components.DesktopTTSControls
import ireader.presentation.ui.reader.components.DesktopTTSIndicator
import ireader.presentation.ui.reader.reverse_swip_refresh.SwipeRefreshState
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel
import org.koin.compose.koinInject

/**
 * Desktop-specific reader screen wrapper that includes TTS functionality
 */
@ExperimentalAnimationApi
@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun DesktopReaderScreenWithTTS(
    vm: ReaderScreenViewModel,
    scrollState: ScrollState,
    lazyListState: LazyListState,
    swipeState: SwipeRefreshState,
    onNext: (reset: Boolean) -> Unit,
    onPrev: (reset: Boolean) -> Unit,
    readerScreenPreferencesState: ReaderScreenViewModel,
    toggleReaderMode: () -> Unit,
    onBackgroundColorAndTextColorApply: (bgColor: String, txtColor: String) -> Unit,
    snackBarHostState: SnackbarHostState,
    drawerState: DrawerState,
    onReaderBottomOnSetting: () -> Unit,
    onSliderFinished: () -> Unit,
    onSliderChange: (index: Float) -> Unit,
    onReaderPlay: () -> Unit,
    onChapterShown: (chapter: Chapter) -> Unit,
    paddingValues: PaddingValues,
    onNavigateToTranslationSettings: () -> Unit,
    ttsService: DesktopTTSService = koinInject()
) {
    var showTTSControls by remember { mutableStateOf(false) }
    
    // Sync TTS with current chapter
    LaunchedEffect(vm.stateChapter?.id, vm.state.book?.id) {
        val chapter = vm.stateChapter
        val book = vm.state.book
        
        if (chapter != null && book != null) {
            // Update TTS service with current chapter
            if (ttsService.state.ttsChapter?.id != chapter.id) {
                ttsService.startReading(book.id, chapter.id)
                // Don't auto-play, just load the chapter
                ttsService.startService(DesktopTTSService.ACTION_PAUSE)
            }
        }
    }
    
    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            // Don't shutdown, just pause
            ttsService.startService(DesktopTTSService.ACTION_PAUSE)
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Original reader screen
        ReadingScreen(
            vm = vm,
            scrollState = scrollState,
            lazyListState = lazyListState,
            swipeState = swipeState,
            onNext = onNext,
            onPrev = onPrev,
            readerScreenPreferencesState = readerScreenPreferencesState,
            toggleReaderMode = toggleReaderMode,
            onBackgroundColorAndTextColorApply = onBackgroundColorAndTextColorApply,
            snackBarHostState = snackBarHostState,
            drawerState = drawerState,
            onReaderBottomOnSetting = onReaderBottomOnSetting,
            onSliderFinished = onSliderFinished,
            onSliderChange = onSliderChange,
            onReaderPlay = onReaderPlay,
            onChapterShown = onChapterShown,
            paddingValues = paddingValues,
            onNavigateToTranslationSettings = onNavigateToTranslationSettings
        )
        
        // Desktop-specific top bar with TTS button (overlay)
        DesktopReaderScreenTopBar(
            vm = vm,
            chapter = vm.stateChapter,
            onPopBackStack = { /* Handle back */ },
            onBookMark = { /* Handle bookmark */ },
            onWebView = { /* Handle webview */ },
            onRefresh = { /* Handle refresh */ },
            onToggleTTSControls = { showTTSControls = !showTTSControls }
        )
        
        // TTS Indicator (always visible when playing)
        if (ttsService.state.isPlaying) {
            DesktopTTSIndicator(
                ttsService = ttsService,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )
        }
        
        // TTS Controls (toggleable)
        if (showTTSControls) {
            DesktopTTSControls(
                ttsService = ttsService,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp) // Above bottom bar
            )
        }
    }
}
