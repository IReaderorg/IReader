package ireader.presentation.ui.reader

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.prefs.ReadingMode
import ireader.presentation.ui.core.ui.Colour.Transparent
import ireader.presentation.ui.reader.components.GlossaryDialogWithFilePickers
import ireader.presentation.ui.reader.components.MainBottomSettingComposable
import ireader.presentation.ui.reader.components.PreloadIndicator
import ireader.presentation.ui.reader.components.TranslationBadge
import ireader.presentation.ui.reader.components.TranslationProgressIndicator
import ireader.presentation.ui.reader.components.TranslationToggleButton
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
        paddingValues: PaddingValues
) {

    val modalBottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        animationSpec = tween(durationMillis = 250)
    )

    DisposableEffect(key1 = modalBottomSheetState.hashCode()) {
        vm.modalBottomSheetState = modalBottomSheetState
        onDispose { }
    }
    
    val scope = rememberCoroutineScope()
    val chapter = vm.stateChapter

    // Initialize the modal sheet state based on reader mode
    LaunchedEffect(Unit) {
        // Ensure that on initialization, the bottom sheet state is set correctly
        if (!vm.isReaderModeEnable) {
            modalBottomSheetState.show()
        }
    }
    
    // Handle changes from modal sheet to reader mode
    LaunchedEffect(key1 = modalBottomSheetState.currentValue) {
        when (modalBottomSheetState.currentValue) {
            ModalBottomSheetValue.Expanded -> {
                if (vm.isReaderModeEnable) vm.isReaderModeEnable = false
            }
            ModalBottomSheetValue.Hidden -> {
                if (!vm.isReaderModeEnable) vm.isReaderModeEnable = true
            }
            else -> {}
        }
    }
    
    // Handle changes from reader mode to modal sheet
    LaunchedEffect(key1 = vm.isReaderModeEnable) {
        when (vm.isReaderModeEnable) {
            false -> {
                if (modalBottomSheetState.currentValue != ModalBottomSheetValue.Expanded) {
                    scope.launch {
                        modalBottomSheetState.show()
                    }
                }
            }
            true -> {
                if (modalBottomSheetState.currentValue != ModalBottomSheetValue.Hidden) {
                    scope.launch {
                        modalBottomSheetState.hide()
                    }
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
            targetState = vm.isLoading && if (vm.readingMode.value == ReadingMode.Continues) vm.chapterShell.isEmpty() else true,
            animationSpec = tween(durationMillis = 300)
        ) { isLoading ->

            when (isLoading) {
                true -> {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = vm.backgroundColor.value
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(48.dp)
                                    .align(Alignment.Center),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp
                            )
                        }
                    }
                }
                else -> {
                    ModalBottomSheetLayout(
                        modifier = Modifier
                            .padding(if (vm.immersiveMode.value) PaddingValues() else paddingValues)
                            .fillMaxWidth(),
                        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        sheetBackgroundColor = if(vm.isSettingChanging) 
                            MaterialTheme.colorScheme.Transparent.copy(0f) 
                        else 
                            MaterialTheme.colorScheme.surface.copy(ContentAlpha.high),
                        sheetContentColor = if(vm.isSettingChanging) 
                            MaterialTheme.colorScheme.Transparent.copy(0f) 
                        else 
                            MaterialTheme.colorScheme.onSurface,
                        scrimColor = Color.Black.copy(alpha = 0.32f),
                        sheetElevation = 8.dp,
                        sheetState = modalBottomSheetState,
                        sheetContent = {
                            Column(
                                modifier = Modifier
                                    .height(140.dp)
                                    .padding(horizontal = 16.dp)
                            ) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                        .align(Alignment.CenterHorizontally)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                MainBottomSettingComposable(
                                    scope = scope,
                                    drawerState = drawerState,
                                    chapter = chapter,
                                    chapters = vm.stateChapters,
                                    currentChapterIndex = vm.currentChapterIndex,
                                    onSetting = onReaderBottomOnSetting,
                                    onNext = { onNext(true) },
                                    onPrev = { onPrev(true) },
                                    onSliderChange = onSliderChange,
                                    onSliderFinished = onSliderFinished,
                                    onPlay = onReaderPlay
                                )
                            }
                        },
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            ReaderText(
                                vm = readerScreenPreferencesState,
                                onNext = { onNext(false) },
                                swipeState = swipeState,
                                onPrev = { onPrev(false) },
                                scrollState = scrollState,
                                modalState = modalBottomSheetState,
                                toggleReaderMode = toggleReaderMode,
                                uiState = vm,
                                lazyListState = lazyListState,
                                onChapterShown = onChapterShown
                            )
                            
                            // Translation toggle button
                            TranslationToggleButton(
                                isTranslated = vm.translationState.isShowingTranslation,
                                hasTranslation = vm.translationState.hasTranslation,
                                onToggle = { vm.toggleTranslation() },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                            )
                            
                            // Translation badge
                            if (vm.translationState.isShowingTranslation) {
                                TranslationBadge(
                                    isTranslated = true,
                                    textColor = vm.textColor.value,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(16.dp)
                                )
                            }
                            
                            // Translation progress indicator
                            TranslationProgressIndicator(
                                isVisible = vm.translationState.isTranslating,
                                progress = vm.translationState.translationProgress,
                                completedItems = (vm.translationState.translationProgress * 100).toInt(),
                                totalItems = 100,
                                engine = vm.translationEnginesManager.get(),
                                textColor = vm.textColor.value,
                                onCancel = { 
                                    scope.launch {
                                        vm.translationState.isTranslating = false
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(horizontal = 16.dp)
                            )
                            
                            // Preload indicator
                            PreloadIndicator(
                                isVisible = vm.isPreloading,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 16.dp)
                            )
                        }
                        
                        // Glossary dialog
                        if (vm.translationState.showGlossaryDialog) {
                            GlossaryDialogWithFilePickers(
                                glossaryEntries = vm.translationState.glossaryEntries,
                                bookTitle = vm.book?.title,
                                onDismiss = { vm.translationState.showGlossaryDialog = false },
                                onAddEntry = { source, target, type, notes ->
                                    vm.addGlossaryEntry(source, target, type, notes)
                                },
                                onEditEntry = { entry ->
                                    vm.updateGlossaryEntry(entry)
                                },
                                onDeleteEntry = { id ->
                                    vm.deleteGlossaryEntry(id)
                                },
                                onExportGlossary = { onSuccess ->
                                    vm.exportGlossary(onSuccess)
                                },
                                onImportGlossary = { json ->
                                    vm.importGlossary(json)
                                },
                                onShowSnackBar = { message ->
                                    vm.showSnackBar(message)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
