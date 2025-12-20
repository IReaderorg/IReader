package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import io.ktor.http.decodeURLPart
import ireader.core.log.Log
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.navigateTo
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.home.sources.global_search.GlobalSearchScreen
import ireader.presentation.ui.home.sources.global_search.viewmodel.GlobalSearchViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.parameter.parametersOf
import ireader.presentation.core.safePopBackStack

data class GlobalSearchScreenSpec(
    val query: String? = null
) {

    @OptIn(
        ExperimentalMaterial3Api::class
    )
    @Composable
    fun Content(

    ) {
        // Decode the URL-encoded query parameter
        val decodedQuery = query?.let { 
            try {
                it.decodeURLPart()
            } catch (e: Exception) {
                it // fallback to original if decode fails
            }
        }
        
        val vm: GlobalSearchViewModel = getIViewModel(parameters =
        { parametersOf(GlobalSearchViewModel.Param(decodedQuery))}
            )
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }

        IScaffold(

        ) {
            GlobalSearchScreen(
                onPopBackStack = {
                    navController.safePopBackStack()
                },
                onSearch = { query ->
                    vm.searchBooks(query = query)
                },
                vm = vm,
                onBook = { book ->
                    try {
                        val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main)
                        scope.launch {
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

