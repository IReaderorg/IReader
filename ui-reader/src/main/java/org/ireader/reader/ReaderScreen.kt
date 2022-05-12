package org.ireader.reader

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.DrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import kotlinx.coroutines.launch
import org.ireader.common_models.entities.Chapter
import org.ireader.components.components.ISnackBarHost
import org.ireader.core_ui.ui.TextAlign
import org.ireader.reader.components.MainBottomSettingComposable
import org.ireader.reader.components.ReaderSettingMainLayout
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
    onChapter: (Chapter) -> Unit,
    onSliderFinished: () -> Unit,
    onSliderChange: (index: Float) -> Unit,
    onDrawerRevereIcon: (Chapter?) -> Unit,
    onReaderRefresh: (Chapter?) -> Unit,
    onReaderWebView: (ModalBottomSheetState) -> Unit,
    onReaderBookmark: () -> Unit,
    onReaderBottomOnSetting: () -> Unit,
    onReaderPlay: () -> Unit,
    onFontSelected: (Int) -> Unit,
    onToggleScrollMode: (Boolean) -> Unit,
    onToggleAutoScroll: (Boolean) -> Unit,
    onToggleOrientation: (Boolean) -> Unit,
    onToggleImmersiveMode: (Boolean) -> Unit,
    onToggleSelectedMode: (Boolean) -> Unit,
    onFontSizeIncrease: (Boolean) -> Unit,
    onParagraphIndentIncrease: (Boolean) -> Unit,
    onParagraphDistanceIncrease: (Boolean) -> Unit,
    onLineHeightIncrease: (Boolean) -> Unit,
    onAutoscrollIntervalIncrease: (Boolean) -> Unit,
    onAutoscrollOffsetIncrease: (Boolean) -> Unit,
    onScrollIndicatorPaddingIncrease: (Boolean) -> Unit,
    onScrollIndicatorWidthIncrease: (Boolean) -> Unit,
    onToggleAutoBrightness: () -> Unit,
    onChangeBrightness: (Float) -> Unit,
    onBackgroundChange: (Int) -> Unit,
    onMap: (LazyListState) -> Unit,
    onPopBackStack: () -> Unit,
    readerScreenPreferencesState: ReaderScreenPreferencesState,
    toggleReaderMode: () -> Unit,
    onDismiss: () -> Unit,
    onBackgroundValueChange: (String) -> Unit,
    onTextColorValueChange: (String) -> Unit,
    onBackgroundColorAndTextColorApply: (bgColor: String, txtColor: String) -> Unit,
    scaffoldState: ScaffoldState,
    onShowScrollIndicator: (Boolean) -> Unit,
    onTextAlign: (TextAlign) -> Unit,
    snackBarHostState:SnackbarHostState
) {

    val modalState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()
    val chapter = vm.stateChapter

    LaunchedEffect(key1 = scaffoldState.drawerState.targetValue) {
        if (chapter != null && scaffoldState.drawerState.targetValue == DrawerValue.Open && vm.stateChapters.isNotEmpty()) {
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

    LaunchedEffect(key1 = modalState.currentValue) {
        when (modalState.currentValue) {
            ModalBottomSheetValue.Expanded -> vm.isReaderModeEnable = false
            ModalBottomSheetValue.Hidden -> vm.isReaderModeEnable = true
            else -> {}
        }
    }
    LaunchedEffect(key1 = vm.isReaderModeEnable) {
        when (vm.isReaderModeEnable) {
            false -> {
                scope.launch {
                    modalState.snapTo(ModalBottomSheetValue.Expanded)
                }
            }
            true -> {
                scope.launch {
                    modalState.snapTo(ModalBottomSheetValue.Hidden)
                }
            }
        }
    }




    ModalNavigationDrawer(
        drawerContent = {
            ReaderScreenDrawer(
                modifier = Modifier.statusBarsPadding(),
                onReverseIcon = {
                    onDrawerRevereIcon(chapter)
                },
                onChapter = onChapter,
                chapter = chapter,
                chapters = vm.drawerChapters.value,
                drawerScrollState = drawerScrollState,
                onMap = onMap,
            )
        }
    ) {



    androidx.compose.material3.Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            ReaderScreenTopBar(
                isReaderModeEnable = vm.isReaderModeEnable,
                isLoaded = vm.isChapterLoaded.value,
                modalBottomSheetValue = modalState.targetValue,
                onRefresh = {
                    onReaderRefresh(chapter)
                },
                chapter = chapter,
                onWebView = {
                    onReaderWebView(modalState)
                },
                vm = readerScreenPreferencesState,
                state = vm,
                scrollState = scrollState,
                onBookMark = onReaderBookmark,
                onPopBackStack = onPopBackStack
            )
        },
        snackbarHost = { ISnackBarHost(snackBarHostState = snackBarHostState) },
        bottomBar = {
            if (!vm.isReaderModeEnable && chapter != null) {
                ModalBottomSheetLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max)
                        .height(if (vm.isMainBottomModeEnable) 130.dp else 320.dp),
                    sheetBackgroundColor = MaterialTheme.colorScheme.background,
                    sheetElevation = 8.dp,
                    sheetState = modalState,
                    sheetContent = {
                        Column(modifier.fillMaxSize()) {
                            Divider(
                                modifier = modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .2f),
                                thickness = 1.dp
                            )
                            Spacer(modifier = modifier.height(5.dp))
                            if (vm.isMainBottomModeEnable) {
                                MainBottomSettingComposable(
                                    scope = scope,
                                    scaffoldState = scaffoldState,
                                    scrollState = scrollState,
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
                            if (vm.isSettingModeEnable) {
                                ReaderSettingMainLayout(
                                    onFontSelected = onFontSelected,
                                    onAutoscrollIntervalIncrease = onAutoscrollIntervalIncrease,
                                    onAutoscrollOffsetIncrease = onAutoscrollOffsetIncrease,
                                    onFontSizeIncrease = onFontSizeIncrease,
                                    onLineHeightIncrease = onLineHeightIncrease,
                                    onParagraphDistanceIncrease = onParagraphDistanceIncrease,
                                    onParagraphIndentIncrease = onParagraphIndentIncrease,
                                    onScrollIndicatorPaddingIncrease = onScrollIndicatorPaddingIncrease,
                                    onScrollIndicatorWidthIncrease = onScrollIndicatorWidthIncrease,
                                    onToggleAutoScroll = onToggleAutoScroll,
                                    onToggleImmersiveMode = onToggleImmersiveMode,
                                    onToggleOrientation = onToggleOrientation,
                                    onToggleScrollMode = onToggleScrollMode,
                                    onToggleSelectedMode = onToggleSelectedMode,
                                    onChangeBrightness = onChangeBrightness,
                                    onToggleAutoBrightness = onToggleAutoBrightness,
                                    onBackgroundChange = onBackgroundChange,
                                    vm = readerScreenPreferencesState,
                                    onShowScrollIndicator = onShowScrollIndicator,
                                    onTextAlign = onTextAlign
                                )
//                                ReaderSettingComposable(
//                                    onFontSelected = onFontSelected,
//                                    onAutoscrollIntervalIncrease = onAutoscrollIntervalIncrease,
//                                    onAutoscrollOffsetIncrease = onAutoscrollOffsetIncrease,
//                                    onFontSizeIncrease = onFontSizeIncrease,
//                                    onLineHeightIncrease = onLineHeightIncrease,
//                                    onParagraphDistanceIncrease = onParagraphDistanceIncrease,
//                                    onParagraphIndentIncrease = onParagraphIndentIncrease,
//                                    onScrollIndicatorPaddingIncrease = onScrollIndicatorPaddingIncrease,
//                                    onScrollIndicatorWidthIncrease = onScrollIndicatorWidthIncrease,
//                                    onToggleAutoScroll = onToggleAutoScroll,
//                                    onToggleImmersiveMode = onToggleImmersiveMode,
//                                    onToggleOrientation = onToggleOrientation,
//                                    onToggleScrollMode = onToggleScrollMode,
//                                    onToggleSelectedMode = onToggleSelectedMode,
//                                    onChangeBrightness = onChangeBrightness,
//                                    onToggleAutoBrightness = onToggleAutoBrightness,
//                                    onBackgroundChange = onBackgroundChange,
//                                    vm = readerScreenPreferencesState
//                                )
                            }
                        }
                    },
                    content = {}
                )
            }
        },

    ) { padding ->
        ScrollIndicatorSetting(
            enable = readerScreenPreferencesState.scrollIndicatorDialogShown,
            readerScreenPreferencesState,
            onDismiss = onDismiss,
            onBackgroundColorValueChange = onBackgroundValueChange,
            onTextColorValueChange = onTextColorValueChange,
            onBackgroundColorAndTextColorApply = onBackgroundColorAndTextColorApply,
        )
        if (chapter != null) {
            Box(modifier = modifier
                .fillMaxSize()
                .padding(padding)) {
                if (!chapter.isEmpty() && !vm.isLoading) {
                    ReaderText(
                        vm = readerScreenPreferencesState,
                        chapter = chapter,
                        onNext = onNext,
                        swipeState = swipeState,
                        onPrev = { onPrev(true) },
                        scrollState = scrollState,
                        modalState = modalState,
                        toggleReaderMode = toggleReaderMode,
                        uiState = vm
                    )
                }

                if (vm.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
    }
}
