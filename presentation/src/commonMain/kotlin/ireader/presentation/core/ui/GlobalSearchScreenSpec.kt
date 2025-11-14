package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import ireader.core.log.Log
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.navigateTo
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.home.sources.global_search.GlobalSearchScreen
import ireader.presentation.ui.home.sources.global_search.viewmodel.GlobalSearchViewModel
import kotlinx.coroutines.runBlocking
import org.koin.core.parameter.parametersOf


data class GlobalSearchScreenSpec(
    val query: String? = null
) {

    @OptIn(
        ExperimentalMaterial3Api::class
    )
    @Composable
    fun Content(

    ) {
        val vm: GlobalSearchViewModel = getIViewModel(parameters =
        { parametersOf(GlobalSearchViewModel.Param(query))}
            )
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }

        IScaffold(

        ) {
            GlobalSearchScreen(
                onPopBackStack = {
                    navController.popBackStack()
                },
                onSearch = { query ->
                    vm.searchBooks(query = query)
                },
                vm = vm,
                onBook = { book ->
                    try {
                        runBlocking {
                            vm.insertUseCases.insertBook(book).let { bookId ->
                                navController.navigateTo(
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
                            navController.navigateTo(
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

