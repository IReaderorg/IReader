package org.ireader.presentation.ui

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import com.google.accompanist.pager.ExperimentalPagerApi
import org.ireader.domain.ui.NavigationArgs
import org.ireader.domain.view_models.detail.book_detail.BookDetailViewModel
import org.ireader.presentation.feature_detail.presentation.book_detail.BookDetailScreen

object BookDetailScreenSpec : ScreenSpec {

    override val navHostRoute: String = "book_detail_route/{bookId}/{sourceId}"

    fun buildRoute(sourceId: Long, bookId: Int): String {
        return "book_detail_route/$bookId/$sourceId"
    }

    override val arguments: List<NamedNavArgument> =
        listOf(
            NavigationArgs.bookId,
            NavigationArgs.sourceId,
        )

    override val deepLinks: List<NavDeepLink> = listOf(
        navDeepLink {
            uriPattern = "https://www.ireader.com/book_detail_route/{bookId}/{sourceId}"
            NavigationArgs.bookId
            NavigationArgs.sourceId
        }
    )

    @OptIn(ExperimentalPagerApi::class, androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class)
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        scaffoldState: ScaffoldState,
    ) {
        val viewModel: BookDetailViewModel = hiltViewModel()
        BookDetailScreen(navController = navController, viewModel = viewModel)
    }

}