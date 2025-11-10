package ireader.presentation.ui.book

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.core.source.Source
import ireader.core.source.model.Command
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.ui.book.viewmodel.BookDetailViewModel
import ireader.presentation.ui.component.components.*
import ireader.presentation.ui.component.isTableUi
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.BigSizeTextComposable
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
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
    onShare: () -> Unit,
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
                    onShare = onShare,
                    onCommand = onCommand,
                    onRefresh = onRefresh,
                    source = source,
                    onDownload = onDownload,
                    onInfo = onInfo,
                    onArchive = onArchive,
                    onUnarchive = onUnarchive,
                    isArchived = isArchived
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
    onShare: () -> Unit,
    onInfo: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior?,
    onArchive: () -> Unit = {},
    onUnarchive: () -> Unit = {},
    isArchived: Boolean = false
) {
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    val (dropDownState, setDropDownState) = remember {
        mutableStateOf(false)
    }
    Toolbar(
        scrollBehavior = scrollBehavior,
        title = {},
        applyInsets = true,
        backgroundColor = Color.Transparent.copy(alpha = 0f),
        contentColor = MaterialTheme.colorScheme.onBackground,
        elevation = 0.dp,
        actions = {
            IconButton(onClick = {
                onRefresh()
            }) {
                Icon(
                    imageVector = Icons.Default.Autorenew,
                    contentDescription = localizeHelper.localize(MR.strings.refresh),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            IconButton(onClick = {
                onInfo()
            }) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = localizeHelper.localize(MR.strings.share),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            if (source is ireader.core.source.CatalogSource && source.getCommands().any { it !is Command.Fetchers }) {
                IconButton(onClick = {
                    onCommand()
                }) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = localizeHelper.localize(MR.strings.advance_commands),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
            IconButton(onClick = {
                onDownload()
            }) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Box {
                IconButton(onClick = { setDropDownState(true) }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = localize(MR.strings.export_book_as_epub),
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
                        text = { Text(text = localizeHelper.localize(MR.strings.export_book_as_epub)) }, 
                        onClick = {
                            onShare()
                            setDropDownState(false)
                        }
                    )
                    if (isArchived) {
                        IDropdownMenuItem(
                            text = { Text(text = localizeHelper.localize(MR.strings.unarchive)) }, 
                            onClick = {
                                onUnarchive()
                                setDropDownState(false)
                            }
                        )
                    } else {
                        IDropdownMenuItem(
                            text = { Text(text = localizeHelper.localize(MR.strings.archive)) }, 
                            onClick = {
                                onArchive()
                                setDropDownState(false)
                            }
                        )
                    }
                }
            }
        },
        navigationIcon = {
            TopAppBarBackButton(onClick = onPopBackStack)
        }
    )
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
                contentDescription = localize(MR.strings.close),
                onClick = onClickCancelSelection
            )
        },
        actions = {
            AppIconButton(
                imageVector = Icons.Default.SelectAll,
                contentDescription = localize(MR.strings.select_all),
                onClick = onClickSelectAll
            )
            AppIconButton(
                imageVector = Icons.Default.FlipToBack,
                contentDescription = localize(MR.strings.select_inverted),
                onClick = onClickInvertSelection
            )
            AppIconButton(
                imageVector = Icons.Default.SyncAlt,
                contentDescription = localize(MR.strings.select_between),
                onClick = onSelectBetween
            )
        }
    )
}
