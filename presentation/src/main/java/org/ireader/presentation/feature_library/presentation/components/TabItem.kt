package org.ireader.presentation.feature_library.presentation.components

import androidx.navigation.NavController
import org.ireader.domain.view_models.library.LibraryViewModel
import org.ireader.presentation.feature_sources.presentation.extension.ExtensionViewModel
import org.ireader.presentation.feature_sources.presentation.extension.composables.CommunitySourceScreen
import org.ireader.presentation.feature_sources.presentation.extension.composables.UserSourcesScreen

sealed class TabItem(
    var title: String,
    var screen: ComposableFun,
) {
    data class Filter(val viewModel: LibraryViewModel) :
        TabItem("Filter", { FilterScreen(viewModel) })

    data class Sort(val viewModel: LibraryViewModel, val navController: NavController) :
        TabItem("Sort", { SortScreen(viewModel) })

    data class Display(val viewModel: LibraryViewModel) :
        TabItem("Display", { DisplayScreen(viewModel) })

    data class Sources(val viewModel: ExtensionViewModel, val navController: NavController) :
        TabItem("Sources",
            { UserSourcesScreen(viewModel = viewModel, navController = navController) })

    data class CommunitySources(
        val viewModel: ExtensionViewModel,
        val navController: NavController,
    ) :
        TabItem("Community Sources",
            { CommunitySourceScreen(viewModel, navController = navController) })
}