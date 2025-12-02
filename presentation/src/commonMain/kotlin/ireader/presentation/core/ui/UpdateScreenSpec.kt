package ireader.presentation.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import ireader.i18n.localize
import ireader.i18n.resources.*
import ireader.i18n.resources.Res
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.navigateTo
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.home.updates.UpdateScreen
import ireader.presentation.ui.home.updates.component.UpdatesToolbar
import ireader.presentation.ui.home.updates.viewmodel.UpdatesViewModel
import kotlinx.coroutines.launch

/**
 * Updates screen specification - provides tab metadata and content
 */
object UpdateScreenSpec {

    @Composable
    fun getTitle(): String = localize(Res.string.updates_screen_label)

    @Composable
    fun getIcon(): Painter = rememberVectorPainter(Icons.Filled.NewReleases)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TabContent() {
        val vm: UpdatesViewModel = getIViewModel()
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        
        // Collect state reactively - this is the key fix!
        val state by vm.state.collectAsState()
        val updates = state.updates
        val selection = state.selectedChapterIds
        val hasSelection = state.hasSelection
        val categories = state.categories

        IScaffold(
            topBar = { scrollBehavior ->
                UpdatesToolbar(
                    state = vm,
                    onClickCancelSelection = {
                        vm.clearSelection()
                    },
                    onClickSelectAll = {
                        vm.selectAll()
                    },
                    onClickFlipSelection = {
                        // Flip selection - select unselected, deselect selected
                        val allIds = updates.values.flatMap { list -> list.map { it.chapterId } }
                        val currentSelection = selection
                        vm.clearSelection()
                        allIds.filterNot { it in currentSelection }.forEach { vm.toggleSelection(it) }
                    },
                    onClickRefresh = {
                        vm.refreshUpdate()
                    },
                    onClickDelete = {
                        vm.scope.launch {
                            vm.updateUseCases.deleteAllUpdates()
                        }
                    },
                    onClickUpdateAll = if (updates.isNotEmpty()) {
                        {
                            vm.selectAll()
                            vm.downloadChapters()
                        }
                    } else null,
                    onClickUpdateSelected = if (hasSelection) {
                        {
                            vm.downloadChapters()
                        }
                    } else null,
                    categories = categories,
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
                    if (hasSelection) {
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
                },
                onBottomBarDownload = {
                    vm.downloadChapters()
                },
                onBottomBarMarkAsRead = {
                    vm.updateChapters {
                        this.copy(read = !this.read)
                    }
                    vm.clearSelection()
                },
                onBottomBookMark = {
                    vm.updateChapters {
                        this.copy(bookmark = !this.bookmark)
                    }
                    vm.clearSelection()
                },
                onRefresh = {
                    vm.refreshUpdate()
                }
            )
        }
    }
}
