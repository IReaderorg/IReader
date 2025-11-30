package ireader.presentation.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import ireader.i18n.localize
import ireader.i18n.resources.*
import ireader.i18n.resources.updates_screen_label
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.navigateTo
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.home.updates.UpdateScreen
import ireader.presentation.ui.home.updates.component.UpdatesToolbar
import ireader.presentation.ui.home.updates.viewmodel.UpdatesViewModel
import kotlinx.coroutines.launch


object UpdateScreenSpec : Tab {


    override val options: TabOptions
        @Composable
        get() {
            val title = localize(Res.string.updates_screen_label)
            val icon = rememberVectorPainter(Icons.Filled.NewReleases)
            return remember {
                TabOptions(
                    index = 1u,
                    title = title,
                    icon = icon,
                )
            }

        }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val vm: UpdatesViewModel = getIViewModel()
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }

        IScaffold(
            topBar = { scrollBehavior ->
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
                        vm.scope.launch {
                            vm.updateUseCases.deleteAllUpdates()
                            vm.updates = emptyMap()
                        }
                    },
                    onClickUpdateAll = if (vm.updates.isNotEmpty()) {
                        {
                            // Select all updates and download them
                            val allIds = vm.updates.values.flatMap { list -> list.map { it.chapterId } }
                            vm.selection.clear()
                            vm.selection.addAll(allIds)
                            vm.downloadChapters()
                            vm.selection.clear()
                        }
                    } else null,
                    onClickUpdateSelected = if (vm.hasSelection) {
                        {
                            vm.downloadChapters()
                            vm.selection.clear()
                        }
                    } else null,
                    categories = vm.categories,
                    onCategorySelected = { categoryId ->
                        vm.selectCategory(categoryId)
                    },
                    scrollBehavior = scrollBehavior,
                )
            }
        ) { scaffoldPadding ->
            UpdateScreen(
                modifier = Modifier.padding(scaffoldPadding),
                state = vm,
                onUpdate = { update ->
                    if (vm.hasSelection) {
                        vm.addUpdate(update)
                    } else {
                        navController.navigateTo(
                            ReaderScreenSpec(
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
                    navController.navigateTo(
                        BookDetailScreenSpec(
                            update.bookId,
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
                },
                onRefresh = {
                    vm.refreshUpdate()
                }
            )

        }
    }
}
