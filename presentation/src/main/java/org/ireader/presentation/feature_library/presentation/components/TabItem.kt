package org.ireader.presentation.feature_library.presentation.components

import androidx.navigation.NavController
import org.ireader.domain.view_models.library.LibraryViewModel

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

}