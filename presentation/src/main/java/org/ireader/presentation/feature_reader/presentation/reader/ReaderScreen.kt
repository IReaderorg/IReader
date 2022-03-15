package org.ireader.presentation.feature_reader.presentation.reader

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.ireader.core.utils.UiEvent
import org.ireader.core.utils.UiText
import org.ireader.domain.FetchType
import org.ireader.domain.view_models.reader.ReaderScreenViewModel
import org.ireader.presentation.feature_reader.presentation.reader.components.MainBottomSettingComposable
import org.ireader.presentation.feature_reader.presentation.reader.components.ReaderSettingComposable
import org.ireader.presentation.feature_reader.presentation.reader.reverse_swip_refresh.SwipeRefreshState
import org.ireader.presentation.presentation.components.ISnackBarHost
import org.ireader.presentation.presentation.reusable_composable.AppTextField
import org.ireader.presentation.presentation.reusable_composable.ErrorTextWithEmojis
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import org.ireader.presentation.ui.WebViewScreenSpec
import org.ireader.presentation.utils.scroll.rememberCarouselScrollState
import org.ireader.presentation.utils.scroll.verticalScroll
import tachiyomi.source.Source


@ExperimentalAnimationApi
@OptIn(ExperimentalMaterialApi::class, com.google.accompanist.pager.ExperimentalPagerApi::class,
    dev.chrisbanes.snapper.ExperimentalSnapperApi::class)
@Composable
fun ReadingScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    vm: ReaderScreenViewModel,
    source: Source,
    scrollState: LazyListState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSliderFinished: () -> Unit,
    onSliderChange: (index: Float) -> Unit,
    swipeState: SwipeRefreshState,
) {

    val chapters = vm.chapters.collectAsLazyPagingItems()
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    val modalState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    val chapter = vm.stateChapter
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val drawerScrollState = rememberLazyListState()

    DisposableEffect(key1 = true) {
        onDispose {
            vm.restoreSetting(context)
        }
    }
    LaunchedEffect(key1 = scaffoldState.drawerState.targetValue) {
        if (scaffoldState.drawerState.targetValue == DrawerValue.Open && vm.stateChapters.isNotEmpty()) {
            drawerScrollState.scrollToItem(vm.currentChapterIndex)
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
                    if (chapter != null) {
                        vm.getLocalChaptersByPaging(chapter.bookId)
                    }
                    modalState.animateTo(ModalBottomSheetValue.Expanded)
                }
            }
            true -> {
                scope.launch {
                    modalState.animateTo(ModalBottomSheetValue.Hidden)
                }
            }
        }
    }

    LaunchedEffect(key1 = true) {
        vm.readOrientation(context)
        vm.readBrightness(context)

        vm.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    scaffoldState.snackbarHostState.showSnackbar(
                        event.uiText.asString(context)
                    )
                }
            }
        }
    }
    LaunchedEffect(key1 = chapter) {
        if (chapter != null) {
            vm.updateChapterSliderIndex(vm.getCurrentIndexOfChapter(chapter))
        }
    }

    Scaffold(
        topBar = {
            ReaderScreenTopBar(
                isReaderModeEnable = vm.isReaderModeEnable,
                isLoaded = vm.isLocalLoaded,
                modalBottomSheetValue = modalState.targetValue,
                onRefresh = {
                    if (chapter != null) {
                        vm.getReadingContentRemotely(chapter = chapter,
                            source = source)
                    }
                },
                source = source,
                chapter = chapter,
                navController = navController,
                onWebView = {
                    try {
                        if (chapter != null && !vm.isReaderModeEnable && vm.isLocalLoaded && modalState.targetValue == ModalBottomSheetValue.Expanded) {
                            navController.navigate(WebViewScreenSpec.buildRoute(
                                url = chapter.link,
                                sourceId = source.id,
                                fetchType = FetchType.ContentFetchType.index,
                            )
                            )
                        } else if (chapter != null && !vm.isLocalLoaded) {
                            navController.navigate(WebViewScreenSpec.buildRoute(
                                url = chapter.link,
                                sourceId = source.id,
                                fetchType = FetchType.ContentFetchType.index,
                                bookId = chapter.bookId,
                                chapterId = chapter.id
                            ))
                        }
                    } catch (e: Exception) {
                        scope.launch {
                            vm.showSnackBar(UiText.ExceptionString(e))
                        }
                    }
                }
            )
        },
        scaffoldState = scaffoldState,
        snackbarHost = { ISnackBarHost(snackBarHostState = it) },
        bottomBar = {
            if (!vm.isReaderModeEnable && chapter != null) {
                ModalBottomSheetLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max)
                        .height(if (vm.isMainBottomModeEnable) 130.dp else 320.dp),
                    sheetBackgroundColor = MaterialTheme.colors.background,
                    sheetElevation = 8.dp,
                    sheetState = modalState,
                    sheetContent = {
                        Column(modifier.fillMaxSize()) {
                            Divider(modifier = modifier.fillMaxWidth(),
                                color = MaterialTheme.colors.onBackground.copy(alpha = .2f),
                                thickness = 1.dp)
                            Spacer(modifier = modifier.height(15.dp))
                            if (vm.isMainBottomModeEnable) {
                                MainBottomSettingComposable(
                                    scope = scope,
                                    scaffoldState = scaffoldState,
                                    scrollState = scrollState,
                                    chapter = chapter,
                                    chapters = vm.stateChapters,
                                    currentChapterIndex = vm.currentChapterIndex,
                                    onSetting = {
                                        vm.toggleSettingMode(true)
                                    },
                                    source = source,
                                    onNext = {
                                        onNext()
                                    },
                                    onPrev = {
                                        onPrev()
                                    },
                                    onSliderChange = { onSliderChange(it) },
                                    onSliderFinished = { onSliderFinished() }
                                )
                            }
                            if (vm.isSettingModeEnable) {
                                ReaderSettingComposable(viewModel = vm)
                            }
                        }
                    },
                    content = {}
                )

            }
        },
        drawerGesturesEnabled = true,
        drawerBackgroundColor = MaterialTheme.colors.background,
        drawerContent = {
            if (chapter != null) {
                ReaderScreenDrawer(
                    modifier = Modifier.statusBarsPadding(),
                    onReverseIcon = {
                        vm.reverseChapters()
                        vm.getLocalChaptersByPaging(chapter.bookId)
                    },
                    onChapter = { ch ->
                        vm.getChapter(ch.id,
                            source = source)
                        coroutineScope.launch {
                            scrollState.animateScrollToItem(0, 0)
                        }
                        vm.updateChapterSliderIndex(vm.getCurrentIndexOfChapter(
                            ch))
                    },
                    chapter = chapter,
                    source = source,
                    chapters = chapters,
                    drawerScrollState = drawerScrollState
                )
            }
        }
    ) {
        ScrollIndicatorSetting(enable = vm.scrollIndicatorDialogShown, vm)
        if (chapter != null) {
            Box(modifier = modifier.fillMaxSize()) {
                if (chapter.isChapterNotEmpty() && !vm.isLoading) {
                    ReaderText(
                        vm = vm,
                        chapter = chapter,
                        onNext = { onNext() },
                        swipeState = swipeState,
                        onPrev = { onPrev() },
                        scrollState = scrollState,
                        modalState = modalState
                    )
                }


                if (vm.error.asString(context).isNotBlank()) {
                    ErrorTextWithEmojis(
                        error = vm.error.asString(context),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .wrapContentSize(Alignment.Center)
                            .align(Alignment.Center),
                    )
                }

                if (vm.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colors.primary
                    )
                }
            }
        }
    }

}

@Composable
fun ScrollIndicatorSetting(
    enable: Boolean = false, vm: ReaderScreenViewModel,
    onDismiss: () -> Unit = {
        vm.scrollIndicatorDialogShown = false
        vm.scrollIndicatorPadding = vm.readScrollIndicatorPadding()
        vm.scrollIndicatorWith = vm.readScrollIndicatorWidth()
        vm.readBackgroundColor()
        vm.readTextColor()
    },
) {
    val (pValue, setPaddingValue) = remember { mutableStateOf<String>("") }
    val (wValue, setWidthValue) = remember { mutableStateOf<String>("") }
    val (bgValue, setBGValue) = remember { mutableStateOf<String>("") }
    val (txtValue, setTxtValue) = remember { mutableStateOf<String>("") }
    val focusManager = LocalFocusManager.current

    if (enable) {
        AlertDialog(
            modifier = Modifier.fillMaxWidth(),
            onDismissRequest = {
                onDismiss()
            },
            title = null,
            text = {
                Column(modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .verticalScroll(
                        rememberCarouselScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    MidSizeTextComposable(text = "Advance Setting")
                    Spacer(modifier = Modifier.height(32.dp))
                    AppTextField(
                        query = pValue,
                        onValueChange = {
                            setPaddingValue(it)
                            try {
                                vm.scrollIndicatorPadding = it.toInt()
                            } catch (e: Exception) {
                            }

                        },
                        onConfirm = {
                            focusManager.clearFocus()
                        },
                        hint = "Scroll Indicator Padding Value",
                        isBasicTextField = false,
                        keyboardAction = KeyboardOptions(imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Number),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppTextField(
                        query = wValue,
                        onValueChange = {
                            setWidthValue(it)
                            try {
                                vm.scrollIndicatorWith = it.toInt()
                            } catch (e: Exception) {
                            }

                        },
                        onConfirm = {
                            focusManager.clearFocus()
                        },
                        hint = "Scroll Indicator  Width Value",
                        isBasicTextField = false,
                        keyboardAction = KeyboardOptions(imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppTextField(
                        query = bgValue,
                        onValueChange = {
                            setBGValue(it)
                            try {
                                vm.backgroundColor = Color(it.toColorInt())
                            } catch (e: Exception) {
                                vm.readBackgroundColor()
                            }

                        },
                        onConfirm = {
                            focusManager.clearFocus()
                        },
                        hint = "Background Color",
                        isBasicTextField = false,
                        keyboardAction = KeyboardOptions(imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Text)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppTextField(
                        query = txtValue,
                        onValueChange = {
                            setTxtValue(it)
                            try {
                                vm.textColor = Color(it.toColorInt())
                            } catch (e: Exception) {
                                vm.readTextColor()
                            }
                        },
                        onConfirm = {
                            focusManager.clearFocus()
                        },
                        hint = "TextColor Value",
                        isBasicTextField = false,
                        keyboardAction = KeyboardOptions(imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Text)
                    )
                }

            },
            contentColor = MaterialTheme.colors.onBackground,
            backgroundColor = MaterialTheme.colors.background,
            buttons = {
                Row(horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = {
                        vm.scrollIndicatorDialogShown = false
                        onDismiss()
                    },
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = MaterialTheme.colors.background,
                            contentColor = MaterialTheme.colors.onBackground
                        )) {

                        MidSizeTextComposable(text = "DISMISS")
                    }
                    Button(onClick = {
                        vm.scrollIndicatorDialogShown = false
                        kotlin.runCatching {
                            if (pValue.isNotBlank()) {
                                vm.saveScrollIndicatorPadding(pValue.toInt())
                            }
                        }.getOrNull()
                        kotlin.runCatching {
                            if (wValue.isNotBlank()) {
                                vm.saveScrollIndicatorWidth(wValue.toInt())
                            }
                        }.getOrNull()
                        kotlin.runCatching {
                            if (bgValue.isNotBlank()) {
                                vm.setReaderBackgroundColor(vm.backgroundColor)
                            }

                        }.getOrNull()
                        kotlin.runCatching {
                            if (txtValue.isNotBlank()) {
                                vm.setReaderTextColor(vm.textColor)
                            }
                        }.getOrNull()

                    },
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = MaterialTheme.colors.primary,
                            contentColor = MaterialTheme.colors.background
                        )) {

                        MidSizeTextComposable(text = "APPLY")
                    }
                }

            },
        )
    }
}


