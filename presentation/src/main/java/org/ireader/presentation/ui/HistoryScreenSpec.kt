package org.ireader.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import org.ireader.common_extensions.async.viewModelIOCoroutine
import org.ireader.components.Controller
import org.ireader.components.reusable_composable.WarningAlert
import org.ireader.domain.ui.NavigationArgs
import org.ireader.history.HistoryScreen
import org.ireader.history.HistoryTopAppBar
import org.ireader.history.viewmodel.HistoryViewModel
import org.ireader.presentation.R

object HistoryScreenSpec : BottomNavScreenSpec {
    override val icon: ImageVector = Icons.Filled.History
    override val label: Int = R.string.history_screen_label
    override val navHostRoute: String = "history"

    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.showBottomNav
    )

    @ExperimentalMaterial3Api
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun TopBar(
        controller: Controller
    ) {
        val vm: HistoryViewModel = hiltViewModel(controller.navBackStackEntry)
        val context = LocalContext.current

        HistoryTopAppBar(
            vm = vm,
            getHistories = {
                vm.getHistoryBooks()
            },
            onDeleteAll = {
                vm.warningAlert.value.apply {
                    enable.value = true
                    this.title.value = context.getString(R.string.remove)
                    this.title.value =
                        context.getString(R.string.dialog_remove_chapter_books_description)
                    this.onDismiss.value = {
                        this.enable.value = false
                    }
                    this.onConfirm.value = {
                        this.enable.value = false
                        vm.viewModelIOCoroutine {
                            vm.historyUseCase.deleteAllHistories()
                        }
                    }
                }
            },
            scrollBehavior = controller.scrollBehavior,

        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(
        controller: Controller
    ) {
        val vm: HistoryViewModel = hiltViewModel(controller.navBackStackEntry)
        val context = LocalContext.current

        WarningAlert(data = vm.warningAlert.value)
        HistoryScreen(
            modifier = Modifier
                .padding(controller.scaffoldPadding),
            onHistory = { history ->
                controller.navController.navigate(
                    ReaderScreenSpec.buildRoute(
                        history.bookId,
                        history.sourceId,
                        history.chapterId
                    )
                )
            },
            onHistoryPlay = { history ->
                controller.navController.navigate(
                    ReaderScreenSpec.buildRoute(
                        history.bookId,
                        history.sourceId,
                        history.chapterId
                    )
                )
            },
            state = vm,
            onBookCover = { history ->
                controller.navController.navigate(
                    BookDetailScreenSpec.buildRoute(
                        history.sourceId,
                        history.bookId
                    )
                )
            },
            onHistoryDelete = { history ->
                vm.warningAlert.value.apply {
                    enable.value = true
                    this.title.value = context.getString(R.string.remove)
                    this.title.value =
                        context.getString(R.string.dialog_remove_chapter_history_description)
                    this.onDismiss.value = {
                        this.enable.value = false
                    }
                    this.onConfirm.value = {
                        this.enable.value = false
                        vm.viewModelIOCoroutine {
                            vm.historyUseCase.deleteHistory(history.chapterId)
                        }
                    }
                }
            },
            onLongClickDelete = { history ->
                vm.warningAlert.value.apply {
                    enable.value = true
                    this.title.value = context.getString(R.string.remove)
                    this.title.value =
                        context.getString(R.string.dialog_remove_chapter_book_description)
                    this.onDismiss.value = {
                        this.enable.value = false
                    }
                    this.onConfirm.value = {
                        this.enable.value = false
                        vm.viewModelIOCoroutine {
                            vm.historyUseCase.deleteHistoryByBookId(history.bookId)
                        }
                    }
                }
            }
        )
    }
}
