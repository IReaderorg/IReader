package ireader.presentation.ui.book

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ireader.core.source.Source
import ireader.core.source.model.Command
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.book.viewmodel.BookDetailViewModel
import ireader.presentation.ui.component.components.IDropdownMenu
import ireader.presentation.ui.component.components.IDropdownMenuItem
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.BigSizeTextComposable
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailTopAppBar(
    modifier: Modifier = Modifier,
    source: Source?,
    onDownload: () -> Unit,
    onRefresh: () -> Unit,
    onPopBackStack: () -> Unit,
    onCommand: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior?,
    state: BookDetailViewModel,
    onClickCancelSelection: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickInvertSelection: () -> Unit,
    onSelectBetween: () -> Unit,
    paddingValues: PaddingValues,
    onInfo: () -> Unit,
    onArchive: () -> Unit = {},
    onUnarchive: () -> Unit = {},
    isArchived: Boolean = false,
    onShareBook: () -> Unit = {},
    onExportEpub: () -> Unit = {},
) {

        when {
            state.hasSelection -> {
                EditModeChapterDetailTopAppBar(
                    modifier = modifier.padding(paddingValues),
                    selectionSize = state.selection.size,
                    onClickCancelSelection = onClickCancelSelection,
                    onClickSelectAll = onClickSelectAll,
                    onClickInvertSelection = onClickInvertSelection,
                    onSelectBetween = onSelectBetween,
                    scrollBehavior = scrollBehavior,
                    paddingValues = paddingValues
                )
            }
            else -> {
                RegularChapterDetailTopAppBar(
                    onPopBackStack = onPopBackStack,
                    scrollBehavior = scrollBehavior,
                    onCommand = onCommand,
                    onRefresh = onRefresh,
                    source = source,
                    onDownload = onDownload,
                    onInfo = onInfo,
                    onArchive = onArchive,
                    onUnarchive = onUnarchive,
                    isArchived = isArchived,
                    onExportEpub = onExportEpub,
                    onShareBook = onShareBook
                )
            }
        }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegularChapterDetailTopAppBar(
    modifier: Modifier = Modifier,
    source: Source?,
    onDownload: () -> Unit,
    onRefresh: () -> Unit,
    onPopBackStack: () -> Unit,
    onCommand: () -> Unit,
    onInfo: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior?,
    onArchive: () -> Unit = {},
    onUnarchive: () -> Unit = {},
    isArchived: Boolean = false,
    onShareBook: () -> Unit = {},
    onExportEpub: () -> Unit = {}
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val (dropDownState, setDropDownState) = remember {
        mutableStateOf(false)
    }
    
    Box {
        Toolbar(
            scrollBehavior = scrollBehavior,
            title = {},
            applyInsets = true,
            backgroundColor = Color.Transparent.copy(alpha = 0f),
            contentColor = MaterialTheme.colorScheme.onBackground,
            elevation = 0.dp,
            actions = {
                // Refresh button
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = localizeHelper.localize(Res.string.refresh),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
                
                // More options menu
                Box {
                    IconButton(onClick = { setDropDownState(true) }) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = localize(Res.string.more_options),
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    IDropdownMenu(
                        modifier = Modifier,
                        expanded = dropDownState,
                        onDismissRequest = {
                            setDropDownState(false)
                        },
                    ) {
                        IDropdownMenuItem(
                            text = { Text(text = localizeHelper.localize(Res.string.share)) },
                            onClick = {
                                onShareBook()
                                setDropDownState(false)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Share, contentDescription = null)
                            }
                        )
                        IDropdownMenuItem(
                            text = { Text(text = localizeHelper.localize(Res.string.export_as_epub)) },
                            onClick = {
                                onExportEpub()
                                setDropDownState(false)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Book, contentDescription = null)
                            }
                        )
                        IDropdownMenuItem(
                            text = { Text(text = localizeHelper.localize(Res.string.info)) },
                            onClick = {
                                onInfo()
                                setDropDownState(false)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Info, contentDescription = null)
                            }
                        )
                        if (source is ireader.core.source.CatalogSource && source.getCommands().any { it !is Command.Fetchers }) {
                            IDropdownMenuItem(
                                text = { Text(text = localizeHelper.localize(Res.string.advance_commands)) },
                                onClick = {
                                    onCommand()
                                    setDropDownState(false)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Tune, contentDescription = null)
                                }
                            )
                        }
                        IDropdownMenuItem(
                            text = { Text(text = "Download") },
                            onClick = {
                                onDownload()
                                setDropDownState(false)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Download, contentDescription = null)
                            }
                        )
                        if (isArchived) {
                            IDropdownMenuItem(
                                text = { Text(text = localizeHelper.localize(Res.string.unarchive)) },
                                onClick = {
                                    onUnarchive()
                                    setDropDownState(false)
                                }
                            )
                        } else {
                            IDropdownMenuItem(
                                text = { Text(text = localizeHelper.localize(Res.string.archive)) },
                                onClick = {
                                    onArchive()
                                    setDropDownState(false)
                                }
                            )
                        }
                    }
                }
            },
            navigationIcon = {}
        )
        
        // Always visible back button with elevated surface for better contrast
        Surface(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 8.dp, top = 8.dp)
                .size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shadowElevation = 4.dp,
            tonalElevation = 2.dp
        ) {
            IconButton(
                onClick = onPopBackStack,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = localizeHelper.localize(Res.string.go_back),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditModeChapterDetailTopAppBar(
    modifier: Modifier = Modifier,
    selectionSize: Int,
    onClickCancelSelection: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickInvertSelection: () -> Unit,
    onSelectBetween: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    paddingValues: PaddingValues
) {

    Toolbar(
        modifier = modifier,
        title = { BigSizeTextComposable(text = "$selectionSize") },
        navigationIcon = {
            AppIconButton(
                imageVector = Icons.Default.Close,
                contentDescription = localize(Res.string.close),
                onClick = onClickCancelSelection
            )
        },
        actions = {
            AppIconButton(
                imageVector = Icons.Default.SelectAll,
                contentDescription = localize(Res.string.select_all),
                onClick = onClickSelectAll
            )
            AppIconButton(
                imageVector = Icons.Default.FlipToBack,
                contentDescription = localize(Res.string.select_inverted),
                onClick = onClickInvertSelection
            )
            AppIconButton(
                imageVector = Icons.Default.SyncAlt,
                contentDescription = localize(Res.string.select_between),
                onClick = onSelectBetween
            )
        }
    )
}
