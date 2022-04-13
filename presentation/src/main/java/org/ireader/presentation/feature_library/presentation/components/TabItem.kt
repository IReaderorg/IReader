package org.ireader.presentation.feature_library.presentation.components

import androidx.annotation.Keep
import androidx.navigation.NavController
import org.ireader.presentation.feature_library.presentation.viewmodel.LibraryViewModel

@Keep
sealed class TabItem(
    var title: String,
    var screen: ComposableFun,
) {
    @Keep
    data class Filter(val viewModel: LibraryViewModel) :
        TabItem("Filter", { FilterScreen(viewModel) })

    @Keep
    data class Sort(val viewModel: LibraryViewModel, val navController: NavController) :
        TabItem("Sort", { SortScreen(viewModel) })

    @Keep
    data class Display(val viewModel: LibraryViewModel) :
        TabItem("Display", { DisplayScreen(viewModel) })

}