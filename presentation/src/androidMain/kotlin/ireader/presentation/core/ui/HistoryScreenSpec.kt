package ireader.presentation.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

import androidx.navigation.NamedNavArgument
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

import ireader.domain.utils.extensions.launchIO
import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.component.reusable_composable.WarningAlert
import ireader.presentation.core.ui.util.NavigationArgs
import ireader.presentation.ui.home.history.HistoryTopAppBar
import ireader.presentation.ui.home.history.viewmodel.HistoryViewModel
import ireader.presentation.R
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.home.history.HistoryScreen
import kotlinx.coroutines.launch


object HistoryScreenSpec : Tab {

    override val options: TabOptions
        @Composable
        get()  {
            val title = stringResource(R.string.history_screen_label)
            val icon = rememberVectorPainter(Icons.Filled.History)
            return remember {
                TabOptions(
                    index = 2u,
                    title = title,
                    icon = icon,
                )
            }

        }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val vm: HistoryViewModel  = getIViewModel()
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
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
                                vm.scope.launch {
                                    vm.historyUseCase.deleteAllHistories()
                                }
                            }
                        }
                    },
                    scrollBehavior =scrollBehavior,

                    )
            },
            snackbarHostState = host
        ) {scaffoldPadding ->
            HistoryScreen(
                modifier = Modifier
                    .padding(scaffoldPadding),
                onHistory = { history ->
                    navigator.push(
                        ReaderScreenSpec(
                            history.bookId,
                            history.chapterId
                        )
                    )
                },
                onHistoryPlay = { history ->
                    navigator.push(
                        ReaderScreenSpec(
                            history.bookId,
                            history.chapterId
                        )
                    )
                },
                vm = vm,
                onBookCover = { history ->
                    navigator.push(
                        BookDetailScreenSpec(
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
                            vm.scope.launchIO {
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
