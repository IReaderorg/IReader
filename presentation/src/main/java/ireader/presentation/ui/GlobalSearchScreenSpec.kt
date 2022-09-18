package ireader.presentation.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import ireader.ui.component.Controller
import ireader.core.log.Log
import ireader.ui.book.viewmodel.BookDetailViewModel
import ireader.ui.home.sources.global_search.GlobalSearchScreen
import ireader.ui.home.sources.global_search.viewmodel.GlobalSearchViewModel
import kotlinx.coroutines.runBlocking
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel

object GlobalSearchScreenSpec : ScreenSpec {

    override val navHostRoute: String = "global?query={query}"

    override val arguments: List<NamedNavArgument> = listOf(
        navArgument("query") {
            type = NavType.StringType
            defaultValue = ""
        }
    )

    fun buildRoute(query: String): String {
        return "global?query=$query"
    }

    @OptIn(
        ExperimentalAnimationApi::class,
        ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content(
        controller: Controller
    ) {
        val vm: GlobalSearchViewModel= getViewModel(owner = controller.navBackStackEntry, parameters = {
            org.koin.core.parameter.parametersOf(
                GlobalSearchViewModel.createParam(controller)
            )
        })
        GlobalSearchScreen(
            scrollBehavior = controller.scrollBehavior,
            onPopBackStack = {
                controller.navController.popBackStack()
            },
            onSearch = { query ->
                vm.searchBooks(query = query)
            },
            vm = vm,
            onBook = { book ->
                try {
                    runBlocking {
                        vm.insertUseCases.insertBook(book).let { bookId ->
                            controller.navController.navigate(
                                BookDetailScreenSpec.buildRoute(
                                    bookId = bookId,
                                )
                            )
                        }

                    }

                } catch (e: Throwable) {
                    Log.error(e, "")
                }
            },
            onGoToExplore = { index ->
                try {
                    if (vm.query.isNotBlank()) {
                        controller.navController.navigate(
                            ExploreScreenSpec.buildRoute(
                                vm.searchItems[index].source.id,
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
