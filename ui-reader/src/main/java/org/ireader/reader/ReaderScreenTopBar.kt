package org.ireader.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.ContentAlpha
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
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.ireader.common_models.entities.Chapter
import org.ireader.components.components.Toolbar
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.AppTextField
import org.ireader.components.reusable_composable.TopAppBarBackButton
import org.ireader.reader.viewmodel.ReaderScreenState
import org.ireader.reader.viewmodel.ReaderScreenViewModel
import org.ireader.ui_reader.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReaderScreenTopBar(
    isReaderModeEnable: Boolean,
    isLoaded: Boolean,
    vm: ReaderScreenViewModel,
    state: ReaderScreenState,
    modalBottomSheetValue: ModalBottomSheetValue,
    chapter: Chapter?,
    onRefresh: () -> Unit,
    onWebView: () -> Unit,
    onBookMark: () -> Unit,
    onPopBackStack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    AnimatedVisibility(
        visible = !isReaderModeEnable && isLoaded,
        enter = slideInVertically(initialOffsetY = { -it }, animationSpec = tween(250)),
        exit = slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(250))
    ) {
        Toolbar(
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
            backgroundColor = MaterialTheme.colorScheme.surface.copy(ContentAlpha.disabled),
            contentColor = MaterialTheme.colorScheme.onSurface,
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
                                    contentDescription = stringResource(R.string.bookmark),
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
    if(!isLoaded) {
        Toolbar(
            title = {},
            elevation = 0.dp,
            backgroundColor = vm.backgroundColor.value,
            actions = {
                if (chapter != null) {
                    AppIconButton(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = stringResource(R.string.refresh),
                        onClick = {
                            onRefresh()
                        },
                        tint = vm.textColor.value
                    )
                    AppIconButton(
                        imageVector = Icons.Default.Public,
                        contentDescription = stringResource(R.string.webView),
                        onClick = {
                            onWebView()
                        },
                        tint = vm.textColor.value
                    )
                }
            },
            navigationIcon = {
                TopAppBarBackButton(onClick = {
                    onPopBackStack()
                }, tint = vm.textColor.value)
            })
    }
}
