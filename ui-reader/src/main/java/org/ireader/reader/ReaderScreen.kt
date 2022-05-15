package org.ireader.reader

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import kotlinx.coroutines.launch
import org.ireader.reader.components.MainBottomSettingComposable
import org.ireader.reader.reverse_swip_refresh.SwipeRefreshState
import org.ireader.reader.viewmodel.ReaderScreenPreferencesState
import org.ireader.reader.viewmodel.ReaderScreenState

@ExperimentalAnimationApi
@OptIn(
    ExperimentalMaterialApi::class, ExperimentalPagerApi::class,
    ExperimentalSnapperApi::class, ExperimentalMaterial3Api::class
)
@Composable
fun ReadingScreen(
    modifier: Modifier = Modifier,
    vm: ReaderScreenState,
    scrollState: LazyListState,
    drawerScrollState: LazyListState,
    swipeState: SwipeRefreshState,
    onNext: () -> Unit,
    onPrev: (scrollToEnd: Boolean) -> Unit,
    readerScreenPreferencesState: ReaderScreenPreferencesState,
    toggleReaderMode: () -> Unit,
    onDismiss: () -> Unit,
    onBackgroundValueChange: (String) -> Unit,
    onTextColorValueChange: (String) -> Unit,
    onBackgroundColorAndTextColorApply: (bgColor: String, txtColor: String) -> Unit,
    snackBarHostState: SnackbarHostState,
    drawerState: DrawerState,
    onReaderBottomOnSetting: () -> Unit,
    onSliderFinished: () -> Unit,
    onSliderChange: (index: Float) -> Unit,
    onReaderPlay: () -> Unit,
    scaffoldPaddingValues: PaddingValues
) {

    val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    LaunchedEffect(key1 = scrollState.hashCode()) {
        vm.modalBottomSheetState = modalBottomSheetState
    }
    val scope = rememberCoroutineScope()
    val chapter = vm.stateChapter
    LaunchedEffect(key1 = drawerState.targetValue) {
        if (chapter != null && drawerState.targetValue == androidx.compose.material3.DrawerValue.Open && vm.stateChapters.isNotEmpty()) {
            val index = vm.stateChapters.indexOfFirst { it.id == chapter.id }
            if (index != -1) {
                scope.launch {
                    drawerScrollState.scrollToItem(
                        index,
                        -drawerScrollState.layoutInfo.viewportEndOffset / 2
                    )
                }
            }
        }
    }

    LaunchedEffect(key1 = modalBottomSheetState.targetValue,) {
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
                    modalBottomSheetState.animateTo(ModalBottomSheetValue.Expanded, tween(250))
                }
            }
            true -> {
                scope.launch {
                    modalBottomSheetState.snapTo(ModalBottomSheetValue.Hidden)
                }
            }
        }
    }


    ScrollIndicatorSetting(
        enable = readerScreenPreferencesState.scrollIndicatorDialogShown,
        readerScreenPreferencesState,
        onDismiss = onDismiss,
        onBackgroundColorValueChange = onBackgroundValueChange,
        onTextColorValueChange = onTextColorValueChange,
        onBackgroundColorAndTextColorApply = onBackgroundColorAndTextColorApply,
    )
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(
            modifier = Modifier.fillMaxSize(),
            targetState = vm.isLoading && readerScreenPreferencesState.initialized,
        ) { loading ->
            when (loading) {
                true -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                }
                false -> {
                    ModalBottomSheetLayout(
                        modifier = Modifier
                            .fillMaxWidth(),
                        sheetBackgroundColor = MaterialTheme.colorScheme.surface,
                        sheetContentColor = MaterialTheme.colorScheme.onSurface,
                        scrimColor = Color.Transparent,
                        sheetElevation = 0.dp,
                        sheetState = modalBottomSheetState,
                        sheetContent = {
                            Column(
                                Modifier.height(130.dp),
                            ) {
                                Divider(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = .2f),
                                    thickness = 1.dp
                                )
                                Spacer(modifier = Modifier.height(5.dp))
                                MainBottomSettingComposable(
                                    scope = scope,
                                    drawerState = drawerState,
                                    chapter = chapter,
                                    chapters = vm.stateChapters,
                                    currentChapterIndex = vm.currentChapterIndex,
                                    onSetting = onReaderBottomOnSetting,
                                    onNext = onNext,
                                    onPrev = {
                                        onPrev(false)
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
                            onNext = onNext,
                            swipeState = swipeState,
                            onPrev = { onPrev(true) },
                            scrollState = scrollState,
                            modalState = modalBottomSheetState,
                            toggleReaderMode = toggleReaderMode,
                            uiState = vm
                        )
                    }
                }

            }
        }

    }
}

