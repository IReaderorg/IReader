package org.ireader.presentation.feature_reader.presentation.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.ireader.common_models.entities.Chapter
import org.ireader.presentation.feature_reader.presentation.reader.viewmodel.ReaderScreenPreferencesState
import org.ireader.presentation.feature_reader.presentation.reader.viewmodel.ReaderScreenState
import org.ireader.presentation.presentation.Toolbar
import org.ireader.core_ui.ui_components.reusable_composable.AppIconButton
import org.ireader.core_ui.ui_components.reusable_composable.AppTextField
import org.ireader.core_ui.ui_components.reusable_composable.TopAppBarBackButton
import org.ireader.core_api.source.Source

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReaderScreenTopBar(
    isReaderModeEnable: Boolean,
    isLoaded: Boolean,
    vm: ReaderScreenPreferencesState,
    state: ReaderScreenState,
    modalBottomSheetValue: ModalBottomSheetValue,
    chapter: Chapter?,
    navController: NavController,
    onRefresh: () -> Unit,
    source: Source,
    onWebView: () -> Unit,
    onBookMark: () -> Unit,
    scrollState: LazyListState,
) {
    val scope = rememberCoroutineScope()
    if (!isReaderModeEnable && isLoaded) {
        AnimatedVisibility(
            visible = !isReaderModeEnable && isLoaded,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(700)),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(700))
        ) {
            Toolbar(
                modifier = Modifier.systemBarsPadding(),
                title = {
                    if (!vm.searchMode) {
                        Text(
                            text = chapter?.title ?: "",
                            color = MaterialTheme.colors.onBackground,
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    } else {
                        AppTextField(
                            query = vm.searchQuery,
                            onValueChange = { query ->
                                vm.searchQuery = query
                                vm.queriedTextIndex.clear()
                                state.stateContent?.value?.let { content ->
                                    content.filter { cont ->
                                        cont.contains(query,
                                            ignoreCase = true)
                                    }.forEach { str ->
                                        val index = content.indexOf(str)
                                        if (index != -1) {
                                            vm.queriedTextIndex.add(index)
                                        }

                                    }
                                }

                            },
                            onConfirm = {

                            },
                        )
                    }

                },
                backgroundColor = MaterialTheme.colors.background,
                contentColor = MaterialTheme.colors.onBackground,
                elevation = 0.dp,
                navigationIcon = {
                    if (!vm.searchMode) {
                        TopAppBarBackButton(navController = navController)
                    } else {
                        AppIconButton(imageVector = Icons.Default.ArrowBack,
                            title = "Exit search mode",
                            onClick = {
                                vm.searchQuery = ""
                                vm.searchMode = false
                                vm.queriedTextIndex.clear()
                            })
                    }
                },
                actions = {
                    when (vm.searchMode) {
                        true -> {
                            AppIconButton(
                                imageVector = Icons.Default.Close,
                                title = "Close",
                                onClick = {
                                    vm.searchQuery = ""
                                    vm.searchMode = false
                                    vm.queriedTextIndex.clear()
                                },
                            )
                            AppIconButton(
                                imageVector = Icons.Default.ExpandMore,
                                title = "previous result",
                                onClick = {
                                    vm.currentViewingSearchResultIndex.let { index ->
                                        chapter?.let {
                                            if (index < chapter.content.lastIndex) {
                                                scope.launch {
                                                    try {
                                                        vm.currentViewingSearchResultIndex += 1
                                                        scrollState.scrollToItem(vm.queriedTextIndex[index])
                                                    } catch (e: Throwable) {
                                                        vm.currentViewingSearchResultIndex = 0
                                                    }
                                                }

                                            }

                                        }
                                    }


                                },
                            )
                            AppIconButton(
                                imageVector = Icons.Default.ExpandLess,
                                title = "next result",
                                onClick = {
                                    vm.currentViewingSearchResultIndex.let { index ->
                                        if (index > 0) {

                                            scope.launch {
                                                try {
                                                    vm.currentViewingSearchResultIndex -= 1
                                                    scrollState.scrollToItem(vm.queriedTextIndex[index])
                                                } catch (e: Throwable) {
                                                    vm.currentViewingSearchResultIndex = 0
                                                }
                                            }
                                        }


                                    }
                                },
                            )
                        }
                        else -> {
                            if (chapter != null) {
                                AppIconButton(imageVector = if (vm.expandTopMenu) Icons.Default.ChevronRight else Icons.Default.ChevronLeft,
                                    title = "Expand Menu",
                                    onClick = {
                                        vm.expandTopMenu = !vm.expandTopMenu
                                    })
                                if (vm.expandTopMenu) {
                                    AppIconButton(imageVector = if (chapter.bookmark) Icons.Filled.Bookmark else Icons.Default.Bookmark,
                                        title = "Bookmark",
                                        tint = if (chapter.bookmark) MaterialTheme.colors.primary else MaterialTheme.colors.onBackground,
                                        onClick = {
                                            onBookMark()
                                        })
                                    AppIconButton(imageVector = Icons.Default.Search,
                                        title = "Search",
                                        onClick = {
                                            vm.searchMode = true
                                        })
                                    AppIconButton(imageVector = Icons.Default.Public,
                                        title = "WebView",
                                        onClick = {
                                            onWebView()
                                        })
                                }
                                AppIconButton(imageVector = Icons.Default.Autorenew,
                                    title = "Refresh",
                                    onClick = {
                                        onRefresh()
                                    })

                            }
                        }

                    }


                }
            )
        }
    } else if (!isLoaded) {
        Toolbar(
            modifier = Modifier.systemBarsPadding(),
            title = {},
            elevation = 0.dp,
            backgroundColor = Color.Transparent,
            actions = {
                if (chapter != null) {
                    AppIconButton(imageVector = Icons.Default.Autorenew,
                        title = "Refresh",
                        onClick = {
                            onRefresh()
                        })
                }
                AppIconButton(imageVector = Icons.Default.Public,
                    title = "WebView",
                    onClick = {
                        onWebView()

                    })
            },
            navigationIcon = {

                TopAppBarBackButton(navController = navController)
            },


            )
    }
}