package org.ireader.presentation.ui

import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import org.ireader.core.extensions.viewModelIOCoroutine
import org.ireader.common_models.entities.UpdateWithInfo.Companion.toUpdate
import org.ireader.domain.ui.NavigationArgs
import org.ireader.presentation.R
import org.ireader.presentation.feature_updates.UpdateScreen
import org.ireader.presentation.feature_updates.viewmodel.UpdatesViewModel


object UpdateScreenSpec : BottomNavScreenSpec {
    override val icon: ImageVector = Icons.Default.NewReleases

    override val label: Int = R.string.updates_screen_label
    override val navHostRoute: String = "updates"


    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.showBottomNav
    )


    @OptIn(ExperimentalPagerApi::class, androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class)
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        scaffoldState: ScaffoldState,
    ) {
        val vm: UpdatesViewModel = hiltViewModel()
        UpdateScreen(
            state = vm,
            onAppbarCancelSelection = {
                vm.selection.clear()
            },
            onAppbarSelectAll = {
                val ids: List<Long> =
                    (vm.selection + vm.updates.values.flatMap { list -> list.map { it.id } }).distinct()
                vm.selection.clear()
                vm.selection.addAll(ids)
            },
            onAppbarFilipSelection = {
                val ids: List<Long> =
                    (vm.updates.flatMap { update -> update.value.map { it.id } }
                        .filterNot { it in vm.selection }).distinct()
                vm.selection.clear()
                vm.selection.addAll(ids)
            },
            onAppbarRefresh = {
                vm.refreshUpdate()
            },
            onAppbarDeleteAll = {
                vm.viewModelIOCoroutine {
                    vm.updatesRepository.deleteAllUpdates()
                }
            },
            onUpdate = { update ->
                if (vm.hasSelection) {
                    vm.addUpdate(update)
                } else {
                    navController.navigate(
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
                navController.navigate(BookDetailScreenSpec.buildRoute(
                    update.sourceId,
                    update.bookId
                ))
            },
            onDownloadUpdate = {
                vm.addUpdate(it)
                vm.downloadChapters()
                vm.selection.clear()
            },
            onBottomBarDelete = {
                vm.viewModelIOCoroutine {
                    vm.updatesRepository.deleteUpdates(vm.updates.values.flatten()
                        .filter { it.id in vm.selection }.map { it.toUpdate() })
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
