package ireader.ui.book.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneOutline
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import ireader.ui.book.viewmodel.BookDetailViewModel
import ireader.ui.component.reusable_composable.AppIconButton
import ireader.core.ui.theme.AppColors
import ireader.presentation.R
import kotlinx.coroutines.launch

@Composable
fun ChapterDetailBottomBar(
    vm: BookDetailViewModel,
    context: Context,
    onDownload: () -> Unit,
    onBookmark: () -> Unit,
    onMarkAsRead: () -> Unit,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 32.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = AppColors.current.bars,
            contentColor = AppColors.current.onBars,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIconButton(
                    imageVector = Icons.Default.GetApp,
                    contentDescription = stringResource(R.string.download),
                    onClick = {
                        vm.downloadChapters()
                        vm.selection.clear()
                    }
                )
                AppIconButton(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = stringResource(R.string.bookmark),
                    onClick = {
                        vm.viewModelScope.launch {
                            vm.insertUseCases.insertChapters(
                                vm.chapters.filter { it.id in vm.selection }
                                    .map { it.copy(bookmark = !it.bookmark) }
                            )
                            vm.selection.clear()
                        }
                    }
                )

                AppIconButton(
                    imageVector = if (vm.chapters.filter { it.read }
                            .map { it.id }
                            .containsAll(vm.selection)
                    ) Icons.Default.DoneOutline else Icons.Default.Done,
                    contentDescription = stringResource(R.string.mark_as_read),
                    onClick = {
                        vm.viewModelScope.launch {
                            vm.insertUseCases.insertChapters(
                                vm.chapters.filter { it.id in vm.selection }
                                    .map { it.copy(read = !it.read) }
                            )
                            vm.selection.clear()
                        }
                    }
                )
                AppIconButton(
                    imageVector = Icons.Default.PlaylistAddCheck,
                    contentDescription = stringResource(R.string.mark_previous_as_read),
                    onClick = {
                        vm.viewModelScope.launch {
                            vm.insertUseCases.insertChapters(
                                vm.chapters.filter { it.id <= (vm.selection.maxOrNull() ?: 0) }
                                    .map { it.copy(read = true) }
                            )
                            vm.selection.clear()
                        }
                    }
                )
                AppIconButton(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    onClick = {
                        vm.deleteChapters(vm.chapters.filter { it.id in vm.selection })
                        vm.selection.clear()
                    }
                )
            }
        }
    }
}