package ireader.presentation.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import ireader.domain.utils.extensions.launchIO
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.navigateTo
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.home.history.HistoryScreen
import ireader.presentation.ui.home.history.HistoryTopAppBar
import ireader.presentation.ui.home.history.viewmodel.HistoryViewModel
import kotlinx.coroutines.launch

/**
 * History screen specification - provides tab metadata and content
 */
object HistoryScreenSpec {

    @Composable
    fun getTitle(): String = localize(Res.string.history_screen_label)

    @Composable
    fun getIcon(): Painter = rememberVectorPainter(Icons.Filled.History)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TabContent() {
        val vm: HistoryViewModel = getIViewModel()
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }

        val state by vm.state.collectAsState()
        val searchFocusRequester = remember { FocusRequester() }
        val host = SnackBarListener(vm)
        
        IScaffold(
            topBar = { scrollBehavior ->
                HistoryTopAppBar(
                    searchMode = state.isSearchMode,
                    searchQuery = state.searchQuery,
                    onSearchModeChange = { vm.toggleSearchMode() },
                    onSearchQueryChange = { vm.onSearchQueryChange(it) },
                    focusRequester = searchFocusRequester,
                    onClearClick = { vm.onSearchQueryChange("") },
                    groupByNovel = state.groupByNovel,
                    onToggleGroupByNovel = { vm.toggleGroupByNovel() },
                    dateFilter = state.dateFilter,
                    onDateFilterChange = { vm.setDateFilterHistory(it) },
                    onClearAll = { vm.deleteAllHistories(localizeHelper) },
                    hasHistory = state.histories.values.isNotEmpty(),
                    scrollBehavior = scrollBehavior
                )
            },
            snackbarHostState = host
        ) { scaffoldPadding ->
            HistoryScreen(
                modifier = Modifier.padding(scaffoldPadding),
                onHistory = { history ->
                    navController.navigateTo(
                        ReaderScreenSpec(
                            history.bookId,
                            history.chapterId
                        )
                    )
                },
                onHistoryPlay = { history ->
                    navController.navigateTo(
                        ReaderScreenSpec(
                            history.bookId,
                            history.chapterId
                        )
                    )
                },
                vm = vm,
                onBookCover = { history ->
                    navController.navigateTo(
                        BookDetailScreenSpec(
                            history.bookId
                        )
                    )
                },
                onHistoryDelete = { history ->
                    vm.warningAlert.apply {
                        enable = true
                        this.title.value = localizeHelper.localize(Res.string.remove)
                        this.title.value =
                            localizeHelper.localize(Res.string.dialog_remove_chapter_history_description)
                        this.onDismiss.value = {
                            this.enable = false
                        }
                        this.onConfirm.value = {
                            this.enable = false
                            vm.scope.launchIO {
                                vm.historyUseCase.deleteHistory(history.chapterId)
                            }
                        }
                    }
                },
                onLongClickDelete = { history ->
                    vm.warningAlert.apply {
                        enable = true
                        this.title.value = localizeHelper.localize(Res.string.remove)
                        this.title.value =
                            localizeHelper.localize(Res.string.dialog_remove_chapter_book_description)
                        this.onDismiss.value = {
                            this.enable = false
                        }
                        this.onConfirm.value = {
                            this.enable = false
                            vm.scope.launchIO {
                                vm.historyUseCase.deleteHistoryByBookId(history.bookId)
                            }
                        }
                    }
                }
            )
        }
    }
}
