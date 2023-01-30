package ireader.presentation.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.navigation.NamedNavArgument
import ireader.presentation.ui.component.Controller
import ireader.domain.utils.extensions.async.viewModelIOCoroutine
import ireader.presentation.core.ui.util.NavigationArgs
import ireader.presentation.R
import ireader.presentation.ui.home.updates.UpdateScreen
import ireader.presentation.ui.home.updates.component.UpdatesToolbar
import ireader.presentation.ui.home.updates.viewmodel.UpdatesViewModel
import org.koin.androidx.compose.getViewModel

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
        val vm: UpdatesViewModel = getViewModel(viewModelStoreOwner = controller.navBackStackEntry)
        UpdatesToolbar(
            state = vm,
            onClickCancelSelection = {
                vm.selection.clear()
            },
            onClickSelectAll = {
                val ids: List<Long> =
                    (vm.selection + vm.updates.values.flatMap { list -> list.map { it.chapterId } }).distinct()
                vm.selection.clear()
                vm.selection.addAll(ids)
            },
            onClickFlipSelection = {
                val ids: List<Long> =
                    (
                        vm.updates.flatMap { update -> update.value.map { it.chapterId } }
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
                    vm.updates = emptyMap()
                }
            },
            scrollBehavior = controller.scrollBehavior,
        )
    }
    @Composable
    override fun Content(
        controller: Controller
    ) {
        val vm: UpdatesViewModel = getViewModel(viewModelStoreOwner = controller.navBackStackEntry)

        UpdateScreen(
            modifier = Modifier.padding(controller.scaffoldPadding),
            state = vm,
            onUpdate = { update ->
                if (vm.hasSelection) {
                    vm.addUpdate(update)
                } else {
                    controller.navController.navigate(
                        ReaderScreenSpec.buildRoute(
                            update.bookId,
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
                        update.bookId
                    )
                )
            },
            onDownloadUpdate = {
                vm.addUpdate(it)
                vm.downloadChapters()
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
