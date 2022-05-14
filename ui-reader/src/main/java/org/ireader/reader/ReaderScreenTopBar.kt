package org.ireader.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.ireader.common_models.entities.Chapter
import org.ireader.components.components.Toolbar
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.AppTextField
import org.ireader.components.reusable_composable.TopAppBarBackButton
import org.ireader.reader.viewmodel.ReaderScreenPreferencesState
import org.ireader.reader.viewmodel.ReaderScreenState
import org.ireader.ui_reader.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReaderScreenTopBar(
    isReaderModeEnable: Boolean,
    isLoaded: Boolean,
    vm: ReaderScreenPreferencesState,
    state: ReaderScreenState,
    modalBottomSheetValue: ModalBottomSheetValue,
    chapter: Chapter?,
    onRefresh: () -> Unit,
    onWebView: () -> Unit,
    onBookMark: () -> Unit,
    scrollState: LazyListState,
    onPopBackStack: () -> Unit,
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
                            text = chapter?.name ?: "",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyLarge,
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
                                state.stateContent.let { content ->
                                    content.filter { cont ->
                                        cont.contains(
                                            query,
                                            ignoreCase = true
                                        )
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
                backgroundColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                elevation = 0.dp,
                navigationIcon = {
                    if (!vm.searchMode) {
                        TopAppBarBackButton(
                            onClick = { onPopBackStack() }
                        )
                    } else {
                        AppIconButton(
                            imageVector = Icons.Default.ArrowBack,
                           contentDescription = stringResource(R.string.exit_search_mode),
                            onClick = {
                                vm.searchQuery = ""
                                vm.searchMode = false
                                vm.queriedTextIndex.clear()
                            }
                        )
                    }
                },
                actions = {
                    when (vm.searchMode) {
                        true -> {
                            AppIconButton(
                                imageVector = Icons.Default.Close,
                               contentDescription = stringResource(R.string.close),
                                onClick = {
                                    vm.searchQuery = ""
                                    vm.searchMode = false
                                    vm.queriedTextIndex.clear()
                                },
                            )
                            AppIconButton(
                                imageVector = Icons.Default.ExpandMore,
                               contentDescription = stringResource(R.string.previous_result),
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
                                contentDescription =  stringResource(R.string.next_result),
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
                                AppIconButton(
                                    imageVector = if (vm.expandTopMenu) Icons.Default.ChevronRight else Icons.Default.ChevronLeft,
                                   contentDescription = stringResource(R.string.expand_menu),
                                    onClick = {
                                        vm.expandTopMenu = !vm.expandTopMenu
                                    }
                                )
                                if (vm.expandTopMenu) {
                                    AppIconButton(
                                        imageVector = if (chapter.bookmark) Icons.Filled.Bookmark else Icons.Default.Bookmark,
                                       contentDescription = stringResource( R.string.bookmark),
                                        tint = if (chapter.bookmark) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                        onClick = {
                                            onBookMark()
                                        }
                                    )
                                    AppIconButton(
                                        imageVector = Icons.Default.Search,
                                       contentDescription = stringResource(R.string.search),
                                        onClick = {
                                            vm.searchMode = true
                                        }
                                    )
                                    AppIconButton(
                                        imageVector = Icons.Default.Public,
                                       contentDescription = stringResource(R.string.webView),
                                        onClick = {
                                            onWebView()
                                        }
                                    )
                                }
                                AppIconButton(
                                    imageVector = Icons.Default.Autorenew,
                                   contentDescription = stringResource(R.string.refresh),
                                    onClick = {
                                        onRefresh()
                                    }
                                )
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
                    AppIconButton(
                        imageVector = Icons.Default.Autorenew,
                       contentDescription = stringResource(R.string.refresh),
                        onClick = {
                            onRefresh()
                        }
                    )
                }
                AppIconButton(
                    imageVector = Icons.Default.Public,
                    contentDescription = stringResource(R.string.webView),
                    onClick = {
                        onWebView()
                    }
                )
            },
            navigationIcon = {

                TopAppBarBackButton(onClick = {
                    onPopBackStack()
                })
            },

        )
    }
}
