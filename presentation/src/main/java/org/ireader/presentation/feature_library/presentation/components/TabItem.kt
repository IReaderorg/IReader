package org.ireader.presentation.feature_library.presentation.components

import androidx.annotation.Keep
import org.ireader.domain.models.DisplayMode
import org.ireader.domain.models.FilterType
import org.ireader.domain.models.LayoutType
import org.ireader.domain.models.SortType

@Keep
sealed class TabItem(
    var title: String,
    var screen: ComposableFun,

) {
    @Keep
    data class Filter(val filters: List<FilterType>,
                      val addFilters: (FilterType) -> Unit,
                      val removeFilter: (FilterType)-> Unit) :
        TabItem("Filter", { FilterScreen(
            removeFilter = removeFilter,
            addFilters = addFilters,
            filters = filters
        ) })

    @Keep
    data class Sort(
        val sortType: SortType,
        val isSortDesc: Boolean,
        val onSortSelected:(SortType) -> Unit) :
        TabItem("Sort", { SortScreen(
            sortType, isSortDesc, onSortSelected
        ) })

    @Keep
    data class Display(
        val layoutType: LayoutType,
        val onLayoutSelected: (DisplayMode) -> Unit) :
        TabItem("Display", { DisplayScreen(
            layoutType, onLayoutSelected
        ) })

}