package org.ireader.presentation.feature_updates

import androidx.compose.animation.Crossfade
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import org.ireader.core.utils.UiText
import org.ireader.core_ui.ui.EmptyScreen
import org.ireader.core_ui.ui.LoadingScreen
import org.ireader.presentation.feature_updates.component.UpdatesContent
import org.ireader.presentation.feature_updates.component.UpdatesToolbar
import org.ireader.presentation.feature_updates.viewmodel.UpdatesViewModel
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
                        (vm.selection + vm.updates.values.flatMap { it.map { it.chapterId } }).distinct()
                    vm.selection.clear()
                    vm.selection.addAll(ids)
                },
                onClickFlipSelection = {
                    val ids: List<Long> =
                        (vm.updates.flatMap { update -> update.value.map { it.chapterId } }
                            .filterNot { it in vm.selection }).distinct()
                    vm.selection.clear()
                    vm.selection.addAll(ids)
                },
                onClickRefresh = {
                    vm.refreshUpdate(context)
                },
            )
        }
    ) {
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
                    onClickDownload = { vm.downloadChapter(it) }
                )
            }
        }
    }
}