package org.ireader.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import org.ireader.Controller
import org.ireader.common_extensions.async.viewModelIOCoroutine
import org.ireader.common_models.entities.UpdateWithInfo.Companion.toUpdate
import org.ireader.domain.ui.NavigationArgs
import org.ireader.presentation.R
import org.ireader.updates.UpdateScreen
import org.ireader.updates.component.UpdatesToolbar
import org.ireader.updates.viewmodel.UpdatesViewModel

object UpdateScreenSpec : BottomNavScreenSpec {
    override val icon: ImageVector = Icons.Filled.NewReleases

    override val label: Int = R.string.updates_screen_label
    override val navHostRoute: String = "updates"


    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.showBottomNav
    )
    @OptIn(ExperimentalMaterial3Api::class)
    @ExperimentalMaterialApi
    @Composable
    override fun TopBar(
        controller: Controller
    ) {
        val vm: UpdatesViewModel = hiltViewModel(controller.navBackStackEntry)
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
                    (
                        vm.updates.flatMap { update -> update.value.map { it.id } }
                            .filterNot { it in vm.selection }
                        ).distinct()
                vm.selection.clear()
                vm.selection.addAll(ids)
            },
            onClickRefresh = {
                vm.refreshUpdate()
            },
            onClickDelete = {
                vm.viewModelIOCoroutine {
                    vm.updateUseCases.deleteAllUpdates()
                }
            },
            scrollBehavior =  controller.scrollBehavior,
        )
    }
    @Composable
    override fun Content(
        controller: Controller
    ) {
        val vm: UpdatesViewModel = hiltViewModel(controller.navBackStackEntry)

        UpdateScreen(
            modifier = Modifier.padding(controller.scaffoldPadding).nestedScroll(controller.scrollBehavior.nestedScrollConnection),
            state = vm,
            onUpdate = { update ->
                if (vm.hasSelection) {
                    vm.addUpdate(update)
                } else {
                    controller.navController.navigate(
                        ReaderScreenSpec.buildRoute(
                            update.bookId,
                            update.sourceId,
                            update.chapterId
                        )
                    )
                }
            },
            onLongUpdate = {
                vm.addUpdate(it)
            },
            onCoverUpdate = { update ->
                controller.navController.navigate(
                    BookDetailScreenSpec.buildRoute(
                        update.sourceId,
                        update.bookId
                    )
                )
            },
            onDownloadUpdate = {
                vm.addUpdate(it)
                vm.downloadChapters()
                vm.selection.clear()
            },
            onBottomBarDelete = {
                vm.viewModelIOCoroutine {
                    vm.updateUseCases.deleteUpdates(
                        vm.updates.values.flatten()
                            .filter { it.id in vm.selection }.map { it.toUpdate() }
                    )
                }
                vm.selection.clear()
            },
            onBottomBarDownload = {
                vm.downloadChapters()
                vm.selection.clear()
            },
            onBottomBarMarkAsRead = {
                vm.updateChapters {
                    this.copy(read = !this.read)
                }
                vm.selection.clear()
            },
            onBottomBookMark = {
                vm.updateChapters {
                    this.copy(bookmark = !this.bookmark)
                }
                vm.selection.clear()
            }
        )
    }
}
