package org.ireader.presentation.feature_updates

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import org.ireader.core.utils.UiText
import org.ireader.core_ui.ui.EmptyScreen
import org.ireader.core_ui.ui.LoadingScreen
import org.ireader.presentation.feature_updates.component.UpdatesContent
import org.ireader.presentation.feature_updates.component.UpdatesToolbar
import org.ireader.presentation.feature_updates.viewmodel.UpdatesViewModel
import org.ireader.presentation.presentation.reusable_composable.AppIconButton
import org.ireader.presentation.ui.BookDetailScreenSpec

@Composable
fun UpdateScreen(
    navController: NavController,
    vm: UpdatesViewModel = hiltViewModel(),
) {

    val context = LocalContext.current
    Scaffold(
        topBar = {
            UpdatesToolbar(
                state = vm,
                onClickCancelSelection = {
                    vm.selection.clear()
                },
                onClickSelectAll = {
                    val ids: List<Long> =
                        (vm.selection + vm.updates.values.flatMap { list -> list.map { it.id } }).distinct()
                    vm.selection.clear()
                    vm.selection.addAll(ids)
                },
                onClickFlipSelection = {
                    val ids: List<Long> =
                        (vm.updates.flatMap { update -> update.value.map { it.id } }
                            .filterNot { it in vm.selection }).distinct()
                    vm.selection.clear()
                    vm.selection.addAll(ids)
                },
                onClickRefresh = {
                    vm.refreshUpdate(context)
                },
                onClickDelete = {
                    vm.deleteAllUpdates()
                }
            )
        }
    ) {
        Box(Modifier.fillMaxSize()) {
            Crossfade(targetState = Pair(vm.isLoading, vm.isEmpty)) { (isLoading, isEmpty) ->
                when {
                    isLoading -> LoadingScreen()
                    isEmpty -> EmptyScreen(UiText.DynamicString("No New Updates is Available."))
                    else -> UpdatesContent(
                        state = vm,
                        onClickItem = { update ->
                            if (vm.hasSelection) {
                                vm.addUpdate(update)
                            } else {
                                navController.navigate(
                                    BookDetailScreenSpec.buildRoute(
                                        update.sourceId,
                                        update.bookId
                                    )
                                )
                            }
                        },
                        onLongClickItem = { vm.addUpdate(it) },
                        onClickCover = { update ->
                            navController.navigate(BookDetailScreenSpec.buildRoute(
                                update.sourceId,
                                update.bookId
                            ))
                        },
                        onClickDownload = {
                            vm.addUpdate(it)
                            vm.downloadChapters(context)
                            vm.selection.clear()
                        }
                    )
                }
                when {
                    vm.hasSelection -> {
                        UpdateEditBar(vm, context)
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.UpdateEditBar(vm: UpdatesViewModel, context: Context) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .align(Alignment.BottomCenter)
            .padding(8.dp)
            .background(MaterialTheme.colors.background)
            .border(width = 1.dp,
                color = MaterialTheme.colors.onBackground.copy(.1f))
            .clickable(enabled = false) {},
    ) {
        Row(modifier = Modifier
            .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (vm.selection.any {
                    it in vm.updates.values.flatten().filter { !it.downloaded }.map { it.id }
                }) {
                AppIconButton(imageVector = Icons.Default.GetApp,
                    title = "Download",
                    onClick = {
                        vm.downloadChapters(context = context)
                        vm.selection.clear()
                    })
            }
            AppIconButton(imageVector = Icons.Default.BookmarkBorder,
                title = "Bookmark",
                onClick = {

                    val updateIds =
                        vm.updates.values.flatten().map { it.id }.filter { it in vm.selection }
                    val chapterIds = vm.updates.values.flatten().filter { it.id in updateIds }
                        .map { it.chapterId }

                    // vm.updates.filter { it.value.map { it.id }.filter { it in ids }}
                    vm.insertChapters(chapterIds) {
                        this.copy(bookmark = !this.bookmark)
                    }
                    vm.selection.clear()
                })

            AppIconButton(imageVector = Icons.Default.Done,
                title = "Mark as read",
                onClick = {
                    val updateIds =
                        vm.updates.values.flatten().map { it.id }.filter { it in vm.selection }
                    val chapterIds = vm.updates.values.flatten().filter { it.id in updateIds }
                        .map { it.chapterId }
                    vm.insertChapters(chapterIds) {
                        this.copy(read = !this.read)
                    }
                    vm.selection.clear()
                })

            AppIconButton(imageVector = Icons.Default.Delete,
                title = "Delete Update",
                onClick = {
                    vm.deleteUpdates(vm.updates.values.flatten().filter { it.id in vm.selection })
                    vm.selection.clear()
                })
        }
    }
}