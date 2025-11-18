package ireader.presentation.ui.home.library

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.AppTextField
import ireader.presentation.ui.component.reusable_composable.BigSizeTextComposable
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import ireader.presentation.ui.core.ui.DEFAULT_ELEVATION
import ireader.presentation.ui.home.library.viewmodel.LibraryState
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterialApi::class, 
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class
)
@Composable
fun LibraryScreenTopBar(
    state: LibraryState,
    onSearch: (() -> Unit)? = null,
    refreshUpdate: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickInvertSelection: () -> Unit,
    onClearSelection: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    showModalSheet:() -> Unit,
    hideModalSheet:() -> Unit,
    isModalVisible:Boolean,
    onUpdateLibrary: () -> Unit = {},
    onUpdateCategory: () -> Unit = {},
    onImportEpub: () -> Unit = {},
    onOpenRandom: () -> Unit = {},
    onSyncRemote: () -> Unit = {},
    onSearchLibrary: () -> Unit = {}
) {
    AnimatedContent(
        targetState = state.selectionMode,
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { if (targetState) it else -it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(150)) togetherWith
            slideOutHorizontally(
                targetOffsetX = { if (!targetState) it else -it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(150))
        }
    ) { selectionMode ->
        if (selectionMode) {
            EditModeTopAppBar(
                selectionSize = state.selectedBooks.size,
                onClickCancelSelection = onClearSelection,
                onClickSelectAll = onClickSelectAll,
                onClickInvertSelection = onClickInvertSelection,
                scrollBehavior = scrollBehavior
            )
        } else {
            RegularTopBar(
                state,
                refreshUpdate = refreshUpdate,
                onSearch = {
                    onSearch?.invoke()
                },
                scrollBehavior = scrollBehavior,
                hideModalSheet = hideModalSheet,
                isModalVisible = isModalVisible,
                showModalSheet = showModalSheet,
                onUpdateLibrary = onUpdateLibrary,
                onUpdateCategory = onUpdateCategory,
                onImportEpub = onImportEpub,
                onOpenRandom = onOpenRandom,
                onSyncRemote = onSyncRemote,
                onSearchLibrary = onSearchLibrary
            )
        }
    }
}

@OptIn(
    ExperimentalMaterialApi::class, 
    ExperimentalComposeUiApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
private fun RegularTopBar(
    vm: LibraryState,
    onSearch: () -> Unit,
    refreshUpdate: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    showModalSheet:() -> Unit,
    hideModalSheet:() -> Unit,
    isModalVisible:Boolean,
    onUpdateLibrary: () -> Unit = {},
    onUpdateCategory: () -> Unit = {},
    onImportEpub: () -> Unit = {},
    onOpenRandom: () -> Unit = {},
    onSyncRemote: () -> Unit = {},
    onSearchLibrary: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val (showMoreMenu, setShowMoreMenu) = remember { mutableStateOf(false) }
    
    Toolbar(
        title = {
            AnimatedContent(
                targetState = vm.inSearchMode,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith 
                    fadeOut(animationSpec = tween(300))
                }
            ) { inSearchMode ->
                if (!inSearchMode) {
                    BigSizeTextComposable(
                        text = localize(Res.string.library),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                } else {
                    AppTextField(
                        query = vm.searchQuery ?: "",
                        onValueChange = { query ->
                            vm.searchQuery = query
                            onSearch()
                        },
                        onConfirm = {
                            onSearch()
                            vm.inSearchMode = false
                            vm.searchQuery = null
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        },
                        modifier = Modifier.focusRequester(focusRequester)
                    )
                }
            }
        },
        actions = {
            if (vm.inSearchMode) {
                AppIconButton(
                    imageVector = Icons.Default.Close,
                    contentDescription = localize(Res.string.close),
                    onClick = {
                        vm.inSearchMode = false
                        vm.searchQuery = null
                        onSearch()
                    },
                )
            } else {
                // Sort/Filter button
                TopBarActionButton(
                    icon = Icons.Outlined.Sort,
                    contentDescription = localize(Res.string.filter),
                    onClick = {
                        scope.launch {
                            if (isModalVisible) {
                                hideModalSheet()
                            } else {
                                showModalSheet()
                            }
                        }
                    },
                    isActive = isModalVisible
                )
                
                // Search button
                TopBarActionButton(
                    icon = Icons.Outlined.Search,
                    contentDescription = localize(Res.string.search),
                    onClick = {
                        vm.inSearchMode = true
                    }
                )
                
                // More options menu
                Box {
                    IconButton(onClick = { setShowMoreMenu(true) }) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = localize(Res.string.more_options),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    ireader.presentation.ui.component.components.IDropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { setShowMoreMenu(false) }
                    ) {
                        ireader.presentation.ui.component.components.IDropdownMenuItem(
                            text = { Text(localize(Res.string.update_library)) },
                            onClick = {
                                onUpdateLibrary()
                                setShowMoreMenu(false)
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Update, contentDescription = null)
                            }
                        )
                        ireader.presentation.ui.component.components.IDropdownMenuItem(
                            text = { Text(localize(Res.string.update_category)) },
                            onClick = {
                                onUpdateCategory()
                                setShowMoreMenu(false)
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Category, contentDescription = null)
                            }
                        )
                        ireader.presentation.ui.component.components.IDropdownMenuItem(
                            text = { Text(localize(Res.string.import_epub)) },
                            onClick = {
                                onImportEpub()
                                setShowMoreMenu(false)
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.FileUpload, contentDescription = null)
                            }
                        )
                        ireader.presentation.ui.component.components.IDropdownMenuItem(
                            text = { Text(localize(Res.string.open_random)) },
                            onClick = {
                                onOpenRandom()
                                setShowMoreMenu(false)
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Shuffle, contentDescription = null)
                            }
                        )
                        ireader.presentation.ui.component.components.IDropdownMenuItem(
                            text = { Text(localize(Res.string.sync_remote)) },
                            onClick = {
                                onSyncRemote()
                                setShowMoreMenu(false)
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.CloudSync, contentDescription = null)
                            }
                        )
                        ireader.presentation.ui.component.components.IDropdownMenuItem(
                            text = { Text(localize(Res.string.refresh)) },
                            onClick = {
                                refreshUpdate()
                                setShowMoreMenu(false)
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Refresh, contentDescription = null)
                            }
                        )
                    }
                }
            }
        },
        navigationIcon = {
            if (vm.inSearchMode) {
                TopAppBarBackButton {
                    vm.inSearchMode = false
                    vm.searchQuery = null
                }
            } else null
        },
        scrollBehavior = scrollBehavior ?: TopAppBarDefaults.pinnedScrollBehavior(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditModeTopAppBar(
    selectionSize: Int,
    onClickCancelSelection: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickInvertSelection: () -> Unit,
    onClickBatchOperations: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    Toolbar(
        title = {
            BadgedBox(
                badge = {
                    Surface(
                        modifier = Modifier
                            .padding(12.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            ,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$selectionSize",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }
                }
            ) {
                Text(
                    text = localize(Res.string.selected),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onClickCancelSelection) {
                Icon(
                    Icons.Default.Close, 
                    contentDescription = localize(Res.string.close),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        elevation = DEFAULT_ELEVATION,
        actions = {
            IconButton(onClick = onClickSelectAll) {
                Icon(
                    Icons.Default.SelectAll, 
                    contentDescription = localize(Res.string.select_all),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onClickInvertSelection) {
                Icon(
                    Icons.Default.FlipToBack, 
                    contentDescription = localize(Res.string.invert_selection),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        scrollBehavior = scrollBehavior,
        backgroundColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun TopBarActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    isActive: Boolean = false
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
