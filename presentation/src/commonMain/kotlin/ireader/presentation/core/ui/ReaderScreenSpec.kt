package ireader.presentation.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ireader.core.log.Log
import ireader.core.util.getBuildNumber
import ireader.domain.preferences.prefs.ReadingMode
import ireader.domain.services.processstate.ProcessStateManager
import ireader.domain.services.processstate.ReaderProcessState
import ireader.domain.utils.extensions.currentTimeToLong
import ireader.i18n.UiText
import ireader.i18n.resources.*
import ireader.i18n.resources.this_is_first_chapter
import ireader.i18n.resources.this_is_last_chapter
import ireader.presentation.core.IModalDrawer
import ireader.presentation.core.IModalSheets
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.NavigationRoutes
import ireader.presentation.core.ensureAbsoluteUrlForWebView
import ireader.presentation.core.navigateTo
import ireader.presentation.core.toComposeColor
import ireader.presentation.ui.component.getContextWrapper
import ireader.presentation.ui.core.theme.AppColors
import ireader.presentation.ui.core.theme.CustomSystemColor
import ireader.presentation.ui.core.ui.Colour.Transparent
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.plugins.integration.FeaturePluginIntegration
import ireader.presentation.ui.plugins.integration.IncompatiblePluginHandler
import ireader.presentation.ui.reader.ReaderScreenDrawer
import ireader.presentation.ui.reader.ReaderScreenTopBar
import ireader.presentation.ui.reader.ReadingScreen
import ireader.presentation.ui.reader.components.ReaderSettingMainLayout
import ireader.presentation.ui.reader.reverse_swip_refresh.rememberSwipeRefreshState
import ireader.presentation.ui.reader.viewmodel.PlatformReaderSettingReader
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel
import ireader.presentation.ui.reader.viewmodel.ReaderState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import ireader.presentation.core.safePopBackStack

@OptIn( ExperimentalMaterial3Api::class)
data class ReaderScreenSpec(
    val bookId: Long,
    val chapterId: Long
) {

    
    @OptIn(
        ExperimentalAnimationApi::class,
         ExperimentalMaterial3Api::class
    )
    @Composable
    fun Content() {
        val scope = rememberCoroutineScope()

        val vm: ReaderScreenViewModel =
            getIViewModel(parameters = {
                parametersOf(
                    ReaderScreenViewModel.Param(
                        chapterId,
                        bookId
                    )
                )
            })
        val platformReader: PlatformReaderSettingReader = koinInject()
        val processStateManager: ProcessStateManager = koinInject()
        val readerState by vm.state.collectAsState()
        
        // Plugin integration for reader menu items
        // Use getKoin().getOrNull() instead of try-catch around koinInject (composable)
        val koin = org.koin.compose.getKoin()
        val featurePluginIntegration: FeaturePluginIntegration? = remember {
            koin.getOrNull<FeaturePluginIntegration>()
        }
        val pluginMenuItems = remember(featurePluginIntegration) {
            featurePluginIntegration?.getPluginMenuItems() ?: emptyList()
        }
        
        // Get FeaturePlugin instances that implement PluginUIProvider for declarative UI rendering
        val featurePlugins = remember(featurePluginIntegration) {
            featurePluginIntegration?.getFeaturePlugins() ?: emptyList()
        }
        
        var showPluginMenu by rememberSaveable { mutableStateOf(false) }
        var selectedPluginId by rememberSaveable { mutableStateOf<String?>(null) }
        
        // Extract values from state
        val successState = readerState as? ReaderState.Success
        val currentIndex = successState?.currentChapterIndex ?: 0
        val chapters = successState?.chapters ?: emptyList()
        val chapter = successState?.currentChapter

        val context = getContextWrapper()
        val scrollState = rememberScrollState()
        val lazyListState = rememberLazyListState()
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        
        // Show dialog for incompatible plugins that need update
        IncompatiblePluginHandler(
            featurePluginIntegration = featurePluginIntegration,
            onNavigateToFeatureStore = {
                navController.navigate(NavigationRoutes.featureStore)
            }
        )

        val swipeState = rememberSwipeRefreshState(isRefreshing = false)
        
        // Restore scroll position from process death state
        val restoredState = remember { processStateManager.getReaderState() }
        LaunchedEffect(restoredState, successState) {
            if (restoredState != null && 
                restoredState.bookId == bookId && 
                restoredState.chapterId == chapterId &&
                successState != null) {
                // Restore scroll position
                try {
                    delay(100) // Wait for content to load
                    scrollState.scrollTo(restoredState.scrollPosition)
                } catch (e: Exception) {
                    Log.warn { "Failed to restore scroll position: ${e.message}" }
                }
                // Clear restored state after applying
                processStateManager.clearReaderState()
            }
        }
        
        // Save state periodically for process death recovery
        LaunchedEffect(scrollState.value, chapter?.id) {
            if (chapter != null) {
                // Debounce state saving to avoid excessive writes
                delay(500)
                processStateManager.saveReaderState(
                    ReaderProcessState(
                        bookId = bookId,
                        chapterId = chapter.id,
                        scrollPosition = scrollState.value,
                        scrollOffset = 0,
                        readingParagraph = 0,
                        isReaderModeEnabled = successState?.isReaderModeEnabled ?: true,
                        timestamp = currentTimeToLong()
                    )
                )
            }
        }
        
        DisposableEffect(key1 = true) {
            onDispose {
                platformReader.apply {
                    if (context != null) {
                        vm.restoreSetting(context, scrollState, lazyListState)
                    }
                }
                // Clear process state when user intentionally leaves the screen
                processStateManager.clearReaderState()
            }
        }
        

        // Track reading time - records time spent in reader screen
        // Save periodically to avoid losing data if app crashes or user reads for long periods
        DisposableEffect(key1 = Unit) {
            val startTime = currentTimeToLong()
            var lastSaveTime = startTime
            Log.info { "Reading time tracking started at: $startTime" }
            
            // Launch a coroutine to save reading time periodically (every 30 seconds)
            val saveJob = scope.launch {
                while (true) {
                    delay(30000) // 30 seconds
                    val currentTime = currentTimeToLong()
                    val durationSinceLastSave = currentTime - lastSaveTime
                    val durationMinutes = durationSinceLastSave / 60000
                    
                    Log.info { "Periodic save check: duration since last save = ${durationSinceLastSave}ms (${durationMinutes}min)" }
                    
                    // Save incremental time
                    if (durationSinceLastSave >= 5000) { // At least 5 seconds
                        try {
                            Log.info { "Saving reading time: ${durationMinutes} minutes" }
                            vm.trackReadingProgressUseCase.trackReadingTime(durationSinceLastSave)
                            vm.trackReadingProgressUseCase.updateReadingStreak(currentTime)
                            lastSaveTime = currentTime
                            Log.info { "Reading time saved successfully" }
                        } catch (e: Exception) {
                            Log.error { "Failed to track reading time periodically: ${e.message}" }
                            e.printStackTrace()
                        }
                    }
                }
            }
            
            onDispose {
                // Cancel periodic save job
                saveJob.cancel()
                
                // Save final reading time
                val endTime = currentTimeToLong()
                val finalDuration = endTime - lastSaveTime
                val totalDuration = endTime - startTime
                val finalMinutes = finalDuration / 60000
                val totalMinutes = totalDuration / 60000
                
                Log.info { "Reading session ended. Final duration: ${finalDuration}ms (${finalMinutes}min), Total: ${totalDuration}ms (${totalMinutes}min)" }
                
                // Only track if user spent at least 5 seconds reading since last save
                if (finalDuration >= 5000) {
                    scope.launch {
                        try {
                            Log.info { "Saving final reading time: ${finalMinutes} minutes" }
                            vm.trackReadingProgressUseCase.trackReadingTime(finalDuration)
                            vm.trackReadingProgressUseCase.updateReadingStreak(endTime)
                            Log.info { "Final reading time saved successfully" }
                        } catch (e: Exception) {
                            Log.error { "Failed to track final reading time: ${e.message}" }
                            e.printStackTrace()
                        }
                    }
                } else {
                    Log.info { "Skipping final save: duration (${finalDuration}ms) is less than 5 seconds" }
                }
                
                // Clear process state when user intentionally leaves the screen
                processStateManager.clearReaderState()
            }
        }

        val autoScrollMode = successState?.autoScrollMode ?: false
        LaunchedEffect(key1 = autoScrollMode) {
            while (vm.settingsViewModel.autoScrollInterval.value.toInt() != 0 && autoScrollMode) {
                scrollState.scrollBy(vm.settingsViewModel.autoScrollOffset.value.toFloat())
                delay(vm.settingsViewModel.autoScrollInterval.value.toLong())
            }
        }
        
        // Sync with TTS chapter when screen becomes active
        // Observe the back stack to detect when we return from TTS screen
        val backStackEntry by navController.currentBackStackEntryFlow.collectAsState(initial = null)
        LaunchedEffect(backStackEntry) {
            // When back stack changes and we're on this screen, sync with TTS
            val route = backStackEntry?.destination?.route
            if (route?.contains("reader") == true) {
                vm.syncWithTTSChapter()
            }
        }
        LaunchedEffect(key1 = vm.autoBrightnessMode.value) {
            platformReader.apply {
                if (context != null) {
                    vm.readBrightness(context)
                }
            }
        }
        LaunchedEffect(key1 = vm.orientation.value) {
            platformReader.apply {
                if (context != null) {
                    vm.readOrientation(context)
                }
            }
        }

        LaunchedEffect(key1 = vm.screenAlwaysOn.value) {
            vm.screenAlwaysOnUseCase(vm.screenAlwaysOn.value)
        }
        val bars = AppColors.current

        val customColor = remember {
            val barsColor = bars.bars.toComposeColor()
            if (getBuildNumber() < 25 && bars.isBarLight) {
                CustomSystemColor(Color.LightGray, barsColor)
            } else {
                CustomSystemColor(barsColor, barsColor)
            }

        }
        val hideSystemBar = remember { mutableStateOf(false) }
        val hideNavBar = remember { mutableStateOf(false) }

        val isInitialized = successState != null
        LaunchedEffect(key1 = isInitialized) {
            platformReader.apply {
                if (context != null && isInitialized) {
                    vm.prepareReaderSetting(
                        context = context,
                        scrollState,
                        onHideNav = {
                            hideNavBar.value = it
                        },
                        onHideStatus = {
                            hideSystemBar.value = it
                        }
                    )
                }
            }
        }

        LaunchedEffect(key1 = vm.immersiveMode.value) {
            platformReader.apply {
                if (context != null) {
                    vm.readImmersiveMode(
                        context = context,
                        onHideNav = {
                            hideNavBar.value = it
                        },
                        onHideStatus = {
                            hideSystemBar.value = it
                        }
                    )
                }
            }
        }
        val host = SnackBarListener(vm)

        val sheetState = rememberModalBottomSheetState()
        val drawerState =
            androidx.compose.material3.rememberDrawerState(androidx.compose.material3.DrawerValue.Closed)
        
        // Handle back button to close drawer instead of closing screen
        // BackHandler removed - Android-specific, implement in androidMain if needed
        
        IModalDrawer(
            state = drawerState,
            sheetContent = {
                val drawerScrollState = rememberLazyListState()
                // Scroll to current chapter when drawer opens
                LaunchedEffect(key1 = drawerState.targetValue) {
                    if (chapter != null && drawerState.targetValue == androidx.compose.material3.DrawerValue.Open) {
                        val drawerChapters = successState?.drawerChapters ?: emptyList()
                        if (drawerChapters.isNotEmpty()) {
                            val index = drawerChapters.indexOfFirst { it.id == chapter.id }
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
                }
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { -it }),
                    exit = slideOutVertically(targetOffsetY = { -it })
                ) {
                    ReaderScreenDrawer(
                        onReverseIcon = {
                            vm.toggleDrawerAsc()
                        },
                        onChapter = { ch ->
                            val index = chapters.indexOfFirst { it.id == ch.id }
                            if (index != -1) {
                                scope.launch {
                                    vm.clearChapterShell(scrollState)
                                    vm.getLocalChapter(ch.id)
                                }
                                scope.launch {
                                    scrollState.scrollTo(0)
                                }
                            }
                        },
                        chapter = chapter,
                        chapters = successState?.drawerChapters ?: emptyList(),
                        drawerScrollState = drawerScrollState,
                        onMap = { drawer ->
                            scope.launch {
                                try {
                                    val drawerChapters = successState?.drawerChapters ?: emptyList()
                                    val index = drawerChapters.indexOfFirst { it.id == chapter?.id }
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
            }
        ) {
            CustomSystemColor(
                enable = false,
                statusBar = customColor.status,
                navigationBar = customColor.navigation
            ) {
                // Use Box to overlay top bar without affecting content layout
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize()
                ) {
                    // Content first (below the top bar)
                    val padding = androidx.compose.foundation.layout.PaddingValues(0.dp)

                    ReadingScreen(
                        drawerState = drawerState,
                        vm = vm,
                        scrollState = scrollState,
                        onNext = { rest ->
                            if (currentIndex < chapters.lastIndex) {
                                try {
                                    // Dispatch navigation to ChapterController (Requirements: 10.1, 10.3)
                                    vm.dispatchNextChapter()
                                    scope.launch {
                                        if (rest) {
                                            vm.clearChapterShell(scrollState)
                                        }
                                    }
                                    when (vm.readingMode.value) {
                                        ReadingMode.Continues -> {}
                                        ReadingMode.Page -> {
                                            scope.launch {
                                                scrollState.scrollTo(0)
                                            }
                                        }
                                    }
                                } catch (e: Throwable) {
                                    Log.error(e, "Reader Spec failed to go next chapter")
                                }
                            } else {
                                scope.launch {
                                    vm.showSnackBar(
                                        UiText.MStringResource(
                                            Res.string.this_is_last_chapter
                                        )
                                    )
                                }
                            }
                        },
                        onPrev = { reset ->
                            try {
                                if (currentIndex > 0) {
                                    // Dispatch navigation to ChapterController (Requirements: 10.2, 10.3)
                                    vm.dispatchPrevChapter()

                                    scope.launch {
                                        if (reset) {
                                            vm.clearChapterShell(scrollState)
                                        }

                                        // For Page mode, also scroll to end
                                        if (vm.readingMode.value == ReadingMode.Page) {
                                            delay(100)
                                            scrollState.scrollTo(scrollState.maxValue)
                                        }
                                        // For Continues mode, scrolling is handled by LaunchedEffect in ReaderText
                                    }
                                } else {
                                    scope.launch {
                                        vm.showSnackBar(
                                            UiText.MStringResource(
                                                Res.string.this_is_first_chapter
                                            )
                                        )
                                    }
                                }
                            } catch (e: Throwable) {
                                Log.error(e, "Reader Spec failed to go previous chapter")
                            }
                        },
                        toggleReaderMode = {
                            // Use toggleReaderMode without parameter to let ViewModel toggle internally
                            // This avoids stale state capture issues
                            vm.toggleReaderMode()
                        },
                        readerScreenPreferencesState = vm,
                        onBackgroundColorAndTextColorApply = { bgColor, txtColor ->
                            try {
                                if (bgColor.isNotBlank()) {
                                    vm.settingsViewModel.setReaderBackgroundColor(vm.backgroundColor.value.toComposeColor())
                                }
                            } catch (e: Throwable) {
                            }

                            try {
                                if (txtColor.isNotBlank()) {
                                    vm.settingsViewModel.setReaderTextColor(vm.textColor.value.toComposeColor())
                                }
                            } catch (e: Throwable) {
                            }
                        },

                        snackBarHostState = host,
                        swipeState = swipeState,
                        onSliderFinished = {
                            scope.launch {
                                if (currentIndex in chapters.indices) {
                                    vm.showSnackBar(
                                        UiText.DynamicString(
                                            chapters[currentIndex].name
                                        )
                                    )
                                }
                            }
                            scope.launch {
                                if (currentIndex in chapters.indices) {
                                    vm.getLocalChapter(
                                        chapters[currentIndex].id,
                                    )
                                }
                            }

                            scope.launch {
                                scrollState.animateScrollTo(0)
                            }
                        },
                        onSliderChange = {
                            // Slider change is now handled via state
                        },
                        onReaderPlay = {
                            successState?.book?.let { book ->
                                chapter?.let { ch ->
                                    navController.navigateTo(
                                        TTSV2ScreenSpec(
                                            bookId = book.id,
                                            sourceId = book.sourceId,
                                            chapterId = ch.id,
                                            readingParagraph = 0
                                        )
                                    )
                                }
                            }
                        },
                        onReaderBottomOnSetting = {
                            scope.launch {
                                sheetState.show()
                            }
                        },
                        lazyListState = lazyListState,
                        onChapterShown = { shownChapter ->
                            if (shownChapter.id != chapter?.id) {
                                kotlin.runCatching {
                                    // Chapter shown is now handled via state
                                    val index =
                                        chapters.indexOfFirst { it.id == shownChapter.id }
                                    if (index != -1) {
                                        // Index update handled via navigateToChapter
                                    }
                                }
                            }
                        },
                        paddingValues = padding,
                        onNavigateToTranslationSettings = {
                            navController.navigate(NavigationRoutes.translationSettings)
                        },
                        onNavigateToCharacterArtUpload = { bookTitle, chapterTitle, prompt ->
                            navController.navigate(
                                NavigationRoutes.characterArtUploadWithData(bookTitle, chapterTitle, prompt)
                            )
                        },
                        onChangeBrightness = { brightness ->
                            platformReader.apply {
                                if (context != null) {
                                    vm.saveBrightness(context, brightness)
                                }
                            }
                        },
                        onToggleAutoBrightness = {
                            vm.autoBrightnessMode.value = !vm.autoBrightnessMode.value
                        },
                        onNavigateToQuoteCreation = { params ->
                            navController.navigate(NavigationRoutes.quoteCreation(params))
                        }
                    )
                    
                    // Top bar overlay (rendered on top of content)
                    val catalog = successState?.catalog
                    val book = successState?.book

                    ReaderScreenTopBar(
                        modifier = androidx.compose.ui.Modifier.align(androidx.compose.ui.Alignment.TopCenter),
                        isReaderModeEnable = successState?.isReaderModeEnabled ?: false,
                        isLoaded = successState?.isChapterLoaded ?: false,
                            onRefresh = {
                                scope.launch {
                                    vm.getLocalChapter(
                                        chapter?.id,
                                        force = true
                                    )
                                }
                            },
                            chapter = chapter,
                            onWebView = {
                                try {
                                    catalog?.let { catalog ->
                                        val absoluteUrl = chapter?.key?.let { url ->
                                            ensureAbsoluteUrlForWebView(url, catalog.source)
                                        }
                                        navController.navigateTo(
                                            WebViewScreenSpec(
                                                url = absoluteUrl,
                                                sourceId = catalog.sourceId,
                                                chapterId = chapter?.id,
                                                bookId = book?.id,
                                                enableChapterFetch = true,
                                                enableChaptersFetch = false,
                                                enableBookFetch = false
                                            )
                                        )
                                    }
                                } catch (e: Throwable) {
                                    scope.launch {
                                        vm.showSnackBar(
                                            UiText.ExceptionString(
                                                e
                                            )
                                        )
                                    }
                                }
                            },
                            vm = vm,
                            state = vm,
                            onBookMark = {
                                vm.bookmarkChapter()
                            },
                            onPopBackStack = {
                                navController.safePopBackStack()
                            },
                            onChapterArt = {
                                vm.showChapterArtDialog()
                            },
                            hasPluginMenuItems = featurePlugins.isNotEmpty(),
                            onPluginMenu = {
                                showPluginMenu = true
                            }
                        )
                    
                    // Plugin panel bottom sheet - uses ReaderPluginPanel for declarative UI
                    if (showPluginMenu && featurePlugins.isNotEmpty()) {
                        androidx.compose.material3.ModalBottomSheet(
                            onDismissRequest = { showPluginMenu = false },
                            sheetState = androidx.compose.material3.rememberModalBottomSheetState()
                        ) {
                            ireader.presentation.ui.reader.plugins.ReaderPluginPanel(
                                plugins = featurePlugins,
                                context = ireader.plugin.api.PluginScreenContext(
                                    bookId = bookId,
                                    chapterId = chapterId,
                                    bookTitle = successState?.book?.title,
                                    chapterTitle = chapter?.name,
                                    selectedText = null,
                                    chapterContent = null
                                ),
                                onDismiss = { showPluginMenu = false },
                                initialPluginId = selectedPluginId
                            )
                        }
                    }
                    
                    // Modal sheet rendered inside the Box to ensure it appears above content
                    val isSettingChanging = vm.settingsViewModel.isSettingChanging
                    if (sheetState.isVisible) {
                        IModalSheets(
                            bottomSheetState = sheetState,
                            backgroundColor = if (isSettingChanging) MaterialTheme.colorScheme.Transparent.copy(0f) else MaterialTheme.colorScheme.background,
                            contentColor = if (isSettingChanging) MaterialTheme.colorScheme.Transparent.copy(0f) else MaterialTheme.colorScheme.onBackground,
                            sheetContent = { sheetModifier ->
                                Column(sheetModifier) {
                                    HorizontalDivider(
                                        modifier = Modifier.fillMaxWidth(),
                                        thickness = 1.dp,
                                        color = if (isSettingChanging) MaterialTheme.colorScheme.Transparent.copy(0f) else MaterialTheme.colorScheme.onBackground.copy(.2f)
                                    )
                                    Spacer(modifier = Modifier.height(5.dp))
                                    ReaderSettingMainLayout(
                                        onFontSelected = { index ->
                                            vm.settingsViewModel.selectFont(index)
                                        },
                                        onChangeBrightness = { brightness ->
                                            platformReader.apply {
                                                if (context != null) {
                                                    vm.saveBrightness(context, brightness)
                                                }
                                            }
                                        },
                                        onBackgroundChange = { index ->
                                            vm.settingsViewModel.changeBackgroundColor(index, vm.readerColors)
                                        },
                                        vm = vm,
                                        onTextAlign = { alignment ->
                                            vm.textAlignment.value = alignment
                                            vm.settingsViewModel.saveTextAlignment(alignment)
                                        },
                                        onToggleAutoBrightness = {
                                            vm.autoBrightnessMode.value = !vm.autoBrightnessMode.value
                                        }
                                    )
                                }
                            }
                        ) {
                            // Empty content - the actual content is rendered above
                        }
                    }
                }
            }
        }
    }

    private fun LazyListState.getId(): Long? {
        return kotlin.runCatching {
            return@runCatching this.layoutInfo.visibleItemsInfo.firstOrNull()?.key.toString()
                .substringAfter("-").toLong()
        }.getOrNull()
    }

}

