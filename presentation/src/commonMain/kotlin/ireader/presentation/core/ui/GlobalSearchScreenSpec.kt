package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.core.log.Log
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.home.sources.global_search.GlobalSearchScreen
import ireader.presentation.ui.home.sources.global_search.viewmodel.GlobalSearchViewModel
import kotlinx.coroutines.runBlocking


data class GlobalSearchScreenSpec(
    val query: String? = null
) : VoyagerScreen() {

    @OptIn(
        ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content(

    ) {
        val vm: GlobalSearchViewModel = getIViewModel(parameters =
                GlobalSearchViewModel.Param(query)
            )
        val navigator = LocalNavigator.currentOrThrow

        IScaffold(

        ) {
            GlobalSearchScreen(
                onPopBackStack = {
                    popBackStack(navigator)
                },
                onSearch = { query ->
                    vm.searchBooks(query = query)
                },
                vm = vm,
                onBook = { book ->
                    try {
                        runBlocking {
                            vm.insertUseCases.insertBook(book).let { bookId ->
                                navigator.push(
                                    BookDetailScreenSpec(
                                        bookId = bookId,
                                    )
                                )
                            }

                        }

                    } catch (e: Throwable) {
                        Log.error(e, "")
                    }
                },
                onGoToExplore = { item ->
                    try {
                        if (vm.query.isNotBlank()) {
                            navigator.push(
                                ExploreScreenSpec(
                                    item.source.id,
                                    query = vm.query
                                )
                            )
                        }
                    } catch (e: Throwable) {
                        Log.error(e, "")
                    }
                },
            )

        }
    }
}

