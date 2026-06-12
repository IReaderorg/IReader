package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.navigateTo
import ireader.presentation.ui.community.PopularBooksScreen
import ireader.presentation.ui.community.PopularBooksViewModel
import ireader.presentation.core.safePopBackStack
class PopularBooksScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel: PopularBooksViewModel = getIViewModel()
        
        PopularBooksScreen(
            vm = viewModel,
            onBackPressed = { navController.safePopBackStack() },
            onNavigateToBook = { bookId ->
                navController.navigate("bookDetail/$bookId")
            },
            onNavigateToGlobalSearch = { query ->
                navController.navigateTo(GlobalSearchScreenSpec(query = query))
            },
            onOpenExternalUrl = { url ->
                // URL is handled by LocalUriHandler in the screen
            },
            onAddSources = {
                // Source for this book isn't installed — take the user to global search
                // where they can pick an installed source or be prompted to add more.
                navController.navigateTo(GlobalSearchScreenSpec(query = null))
            },
            onNavigateToExtensions = {
                // Navigate to extensions tab (index 3 in bottom nav)
                navController.navigate("main") {
                    popUpTo("main") { inclusive = true }
                }
            }
        )
    }
}
