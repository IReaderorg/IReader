package org.ireader.chapterDetails

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ireader.chapterDetails.viewmodel.ChapterDetailState
import org.ireader.core_ui.ui_components.components.CenterTopAppBar
import org.ireader.core_ui.ui_components.reusable_composable.AppIconButton
import org.ireader.core_ui.ui_components.reusable_composable.BigSizeTextComposable
import org.ireader.presentation.presentation.Toolbar


@Composable
fun ChapterDetailTopAppBar(
    state: ChapterDetailState,
    onClickCancelSelection: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickFlipSelection: () -> Unit,
    onReverseClick: () -> Unit,
    onPopBackStack: () -> Unit,
    onMap: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        when {
            state.hasSelection -> {
                EditModeChapterDetailTopAppBar(
                    selectionSize = state.selection.size,
                    onClickCancelSelection = onClickCancelSelection,
                    onClickSelectAll = onClickSelectAll,
                    onClickInvertSelection = onClickFlipSelection
                )
            }
            else -> {
                RegularChapterDetailTopAppBar(
                    onReverseClick = onReverseClick,
                    onPopBackStack = onPopBackStack,
                    onMap = onMap
                )
            }
        }
    }

}


@Composable
fun RegularChapterDetailTopAppBar(
    onReverseClick: () -> Unit,
    onPopBackStack: () -> Unit,
    onMap: () -> Unit,
) {
    CenterTopAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp),
        title = {
            BigSizeTextComposable(text = "Content")
        },
        actions = {
                AppIconButton(imageVector = Icons.Filled.Place, title = "", onClick = onMap)
                IconButton(onClick = onReverseClick) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort Icon"
                    )
                }
        },
        navigationIcon = {
            IconButton(onClick = onPopBackStack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back Icon"
                )
            }

        },
        elevation = 0.dp
    )
}

@Composable
private fun EditModeChapterDetailTopAppBar(
    selectionSize: Int,
    onClickCancelSelection: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickInvertSelection: () -> Unit,
) {
    Toolbar(
        title = { BigSizeTextComposable(text = "$selectionSize") },
        navigationIcon = {
            IconButton(onClick = onClickCancelSelection) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
        },
        actions = {
            IconButton(onClick = onClickSelectAll) {
                Icon(Icons.Default.SelectAll, contentDescription = null)
            }
            IconButton(onClick = onClickInvertSelection) {
                Icon(Icons.Default.FlipToBack, contentDescription = null)
            }
        }
    )
}