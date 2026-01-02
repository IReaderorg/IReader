package ireader.presentation.ui.book.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.book.viewmodel.BookDetailViewModel
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.core.theme.AppColors
import ireader.presentation.core.toComposeColor
import kotlinx.coroutines.launch

/**
 * Bottom bar shown when chapters are selected in the detail screen.
 * Contains actions: Download, Translate, Bookmark, Mark as Read, Mark Previous, Delete
 * 
 * @param onTranslate Called on single tap - starts quick translation with default settings
 * @param onTranslateLongPress Called on long press - opens translation options dialog
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChapterDetailBottomBar(
    vm: BookDetailViewModel,
    onDownload: () -> Unit,
    onBookmark: () -> Unit,
    onMarkAsRead: () -> Unit,
    onTranslate: () -> Unit = {},
    onTranslateLongPress: () -> Unit = {},
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
            color = AppColors.current.bars.toComposeColor(),
            contentColor = AppColors.current.onBars.toComposeColor(),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIconButton(
                    imageVector = Icons.Default.GetApp,
                    contentDescription = localize(Res.string.download),
                    onClick = {
                        vm.downloadChapters()
                        vm.selection.clear()
                    }
                )
                
                // Translate button with long press support
                IconButton(
                    onClick = onTranslate,
                    modifier = Modifier.combinedClickable(
                        onClick = onTranslate,
                        onLongClick = onTranslateLongPress
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = localize(Res.string.translate_action),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                AppIconButton(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = localize(Res.string.bookmark),
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
                    contentDescription = localize(Res.string.mark_as_read),
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
                    contentDescription = localize(Res.string.mark_previous_as_read),
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
                // Delete chapter content only (keeps chapter record in DB)
                AppIconButton(
                    imageVector = Icons.Default.Delete,
                    contentDescription = localize(Res.string.delete_content),
                    onClick = {
                        vm.deleteChapterContent(vm.chapters.filter { it.id in vm.selection })
                    }
                )
                // Delete chapter from DB entirely
                AppIconButton(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = localize(Res.string.delete_chapter),
                    onClick = {
                        vm.deleteChapters(vm.chapters.filter { it.id in vm.selection })
                    }
                )
            }
        }
    }
}
