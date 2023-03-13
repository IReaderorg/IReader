package ireader.presentation.ui.book.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.ui.book.viewmodel.BookDetailViewModel
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.core.theme.AppColors
import kotlinx.coroutines.launch

@Composable
fun ChapterDetailBottomBar(
    vm: BookDetailViewModel,
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
                    contentDescription = localize(MR.strings.download),
                    onClick = {
                        vm.downloadChapters()
                        vm.selection.clear()
                    }
                )
                AppIconButton(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = localize(MR.strings.bookmark),
                    onClick = {
                        vm.scope.launch {
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
                    contentDescription = localize(MR.strings.mark_as_read),
                    onClick = {
                        vm.scope.launch {
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
                    contentDescription = localize(MR.strings.mark_previous_as_read),
                    onClick = {
                        vm.scope.launch {
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
                    contentDescription = localize(MR.strings.delete),
                    onClick = {
                        vm.deleteChapters(vm.chapters.filter { it.id in vm.selection })
                        vm.selection.clear()
                    }
                )
            }
        }
    }
}