package ireader.presentation.ui.reader

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.prefs.ReadingMode
import ireader.presentation.ui.core.ui.Colour.Transparent
import ireader.presentation.ui.reader.components.MainBottomSettingComposable
import ireader.presentation.ui.reader.reverse_swip_refresh.SwipeRefreshState
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel
import kotlinx.coroutines.launch

@ExperimentalAnimationApi
@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun ReadingScreen(
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
) {

    val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    DisposableEffect(key1 = modalBottomSheetState.hashCode()) {
        vm.modalBottomSheetState = modalBottomSheetState

        onDispose {  }
    }
    val scope = rememberCoroutineScope()
    val chapter = vm.stateChapter

    LaunchedEffect(key1 = modalBottomSheetState.targetValue) {
        when (modalBottomSheetState.targetValue) {
            ModalBottomSheetValue.Expanded -> vm.isReaderModeEnable = false
            ModalBottomSheetValue.Hidden -> vm.isReaderModeEnable = true
            else -> {}
        }
    }
    LaunchedEffect(key1 = vm.isReaderModeEnable) {
        when (vm.isReaderModeEnable) {
            false -> {
                scope.launch {
                    modalBottomSheetState.show()
                }
            }
            true -> {
                scope.launch {
                    modalBottomSheetState.hide()
                }
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(vm.backgroundColor.value),
        contentAlignment = Alignment.Center,
    ) {
        if (vm.webViewManger.inProgress && vm.webViewIntegration.value) {
            vm.prefFunc.WebView()
        }
        Crossfade(
            modifier = Modifier.fillMaxSize(),
            targetState = vm.isLoading && if (vm.readingMode.value == ReadingMode.Continues) vm.chapterShell.isEmpty() else true
        ) { isLoading ->

            when (isLoading) {
                true -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                else -> {
                    ModalBottomSheetLayout(
                        modifier = Modifier
                            .fillMaxWidth(),
                        sheetBackgroundColor = if(vm.isSettingChanging) MaterialTheme.colorScheme.Transparent.copy(0f) else MaterialTheme.colorScheme.surface.copy(ContentAlpha.high),
                        sheetContentColor = if(vm.isSettingChanging) MaterialTheme.colorScheme.Transparent.copy(0f) else MaterialTheme.colorScheme.onSurface,
                        scrimColor = Color.Transparent,
                        sheetElevation = 0.dp,
                        sheetState = modalBottomSheetState,
                        sheetContent = {
                            Column(
                                Modifier.height(130.dp),
                            ) {
                                Spacer(modifier = Modifier.height(5.dp))
                                MainBottomSettingComposable(
                                    scope = scope,
                                    drawerState = drawerState,
                                    chapter = chapter,
                                    chapters = vm.stateChapters,
                                    currentChapterIndex = vm.currentChapterIndex,
                                    onSetting = onReaderBottomOnSetting,
                                    onNext = {
                                        onNext(true)
                                    },
                                    onPrev = {
                                        onPrev(true)
                                    },
                                    onSliderChange = onSliderChange,
                                    onSliderFinished = onSliderFinished,
                                    onPlay = onReaderPlay
                                )
                            }
                        },
                    ) {

                        ReaderText(
                            vm = readerScreenPreferencesState,
                            onNext = {
                                onNext(false)
                            },
                            swipeState = swipeState,
                            onPrev = { onPrev(false) },
                            scrollState = scrollState,
                            modalState = modalBottomSheetState,
                            toggleReaderMode = toggleReaderMode,
                            uiState = vm,
                            lazyListState = lazyListState,
                            onChapterShown = onChapterShown

                        )
                    }
                }
            }
        }
    }
}
