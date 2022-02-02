package ir.kazemcodes.infinity.core.ui

import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import ir.kazemcodes.infinity.R
import ir.kazemcodes.infinity.feature_library.presentation.LibraryScreen
import ir.kazemcodes.infinity.feature_library.presentation.LibraryViewModel

object LibraryScreenSpec : BottomNavScreenSpec {
    override val icon: ImageVector = Icons.Default.Book
    override val label: Int = R.string.library_screen_label
    override val navHostRoute: String = "library"


    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.showBottomNav
    )


    @OptIn(ExperimentalPagerApi::class, androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class)
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        scaffoldState: ScaffoldState
    ) {
        val viewModel: LibraryViewModel = hiltViewModel(navBackStackEntry)
        LibraryScreen(navController = navController, viewModel = viewModel)
    }

}
