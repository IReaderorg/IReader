package ireader.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext

import androidx.navigation.NamedNavArgument
import ireader.common.extensions.async.viewModelIOCoroutine
import ireader.ui.component.Controller
import ireader.ui.component.reusable_composable.WarningAlert
import ireader.presentation.ui.util.NavigationArgs
import ireader.ui.home.history.HistoryTopAppBar
import ireader.ui.home.history.viewmodel.HistoryViewModel
import ireader.presentation.R
import ireader.ui.home.history.HistoryScreen
import org.koin.androidx.compose.getViewModel

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
        val vm: HistoryViewModel = getViewModel(owner = controller.navBackStackEntry)
        val context = LocalContext.current

        HistoryTopAppBar(
            vm = vm,
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
    @Composable
    override fun Content(
        controller: Controller
    ) {
        val vm: HistoryViewModel  = getViewModel(owner = controller.navBackStackEntry)
        val context = LocalContext.current

        WarningAlert(data = vm.warningAlert.value)
        HistoryScreen(
            modifier = Modifier
                .padding(controller.scaffoldPadding),
            onHistory = { history ->
                controller.navController.navigate(
                    ReaderScreenSpec.buildRoute(
                        history.bookId,
                        history.chapterId
                    )
                )
            },
            onHistoryPlay = { history ->
                controller.navController.navigate(
                    ReaderScreenSpec.buildRoute(
                        history.bookId,
                        history.chapterId
                    )
                )
            },
            vm = vm,
            onBookCover = { history ->
                controller.navController.navigate(
                    BookDetailScreenSpec.buildRoute(
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
