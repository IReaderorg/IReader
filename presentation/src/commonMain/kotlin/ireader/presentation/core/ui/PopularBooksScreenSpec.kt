package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import ireader.presentation.core.LocalNavigator
import ireader.presentation.ui.community.PopularBooksScreen
import ireader.presentation.ui.community.PopularBooksViewModel

class PopularBooksScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel: PopularBooksViewModel = getIViewModel()
        
        PopularBooksScreen(
            vm = viewModel,
            onBackPressed = { navController.popBackStack() },
            onNavigateToBook = { bookId ->
                navController.navigate("bookDetail/$bookId")
            },
            onNavigateToGlobalSearch = { query ->
                navController.navigate("globalSearch?query=$query")
            },
            onOpenExternalUrl = { url ->
                // URL is handled by LocalUriHandler in the screen
            }
        )
    }
}
