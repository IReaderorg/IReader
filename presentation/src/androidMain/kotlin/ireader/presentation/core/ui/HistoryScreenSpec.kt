package ireader.presentation.core.ui

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
import ireader.domain.utils.extensions.async.viewModelIOCoroutine
import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.component.reusable_composable.WarningAlert
import ireader.presentation.core.ui.util.NavigationArgs
import ireader.presentation.ui.home.history.HistoryTopAppBar
import ireader.presentation.ui.home.history.viewmodel.HistoryViewModel
import ireader.presentation.R
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.home.history.HistoryScreen
import org.koin.androidx.compose.getViewModel

object HistoryScreenSpec : BottomNavScreenSpec {
    override val icon: ImageVector = Icons.Filled.History
    override val label: Int = R.string.history_screen_label
    override val navHostRoute: String = "history"

    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.showBottomNav
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(
        controller: Controller
    ) {
        val vm: HistoryViewModel  = getViewModel(viewModelStoreOwner = controller.navBackStackEntry)
        val context = LocalContext.current

        WarningAlert(data = vm.warningAlert.value)
        val host = SnackBarListener(vm)
        IScaffold(
            topBar = { scrollBehavior->
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
                    scrollBehavior =scrollBehavior,

                    )
            }
        ) {scaffoldPadding ->
            HistoryScreen(
                modifier = Modifier
                    .padding(scaffoldPadding),
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
}
