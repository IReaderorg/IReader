package org.ireader.presentation.feature_detail.presentation.chapter_detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ireader.core.utils.Constants
import org.ireader.presentation.feature_detail.presentation.chapter_detail.viewmodel.ChapterDetailState
import org.ireader.presentation.presentation.Toolbar
import org.ireader.presentation.presentation.components.CenterTopAppBar
import org.ireader.presentation.presentation.reusable_composable.TopAppBarTitle


@Composable
fun ChapterDetailTopAppBar(
    state: ChapterDetailState,
    onClickCancelSelection: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickFlipSelection: () -> Unit,
    onReverseClick: () -> Unit,
    onPopBackStack: () -> Unit,
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
                    onPopBackStack = onPopBackStack
                )
            }
        }
    }

}


@Composable
fun RegularChapterDetailTopAppBar(
    onReverseClick: () -> Unit,
    onPopBackStack: () -> Unit,
) {
    CenterTopAppBar(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxWidth()
            .height(45.dp), title = {
            TopAppBarTitle(title = "Content")
        },
        backgroundColor = MaterialTheme.colors.background,
        contentColor = MaterialTheme.colors.onBackground,
        elevation = Constants.DEFAULT_ELEVATION,
        actions = {
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
        }
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
        title = { TopAppBarTitle(title = "$selectionSize") },
        navigationIcon = {
            IconButton(onClick = onClickCancelSelection) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
        },
        elevation = Constants.DEFAULT_ELEVATION,
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