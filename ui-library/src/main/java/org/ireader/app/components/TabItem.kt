package org.ireader.app.components

import org.ireader.common_models.DisplayMode
import org.ireader.common_models.FilterType
import org.ireader.common_models.LayoutType
import org.ireader.common_models.SortType


sealed class TabItem(
    var title: String,
    var screen: ComposableFun,

) {

    data class Filter(val filters: List<FilterType>,
                      val addFilters: (FilterType) -> Unit,
                      val removeFilter: (FilterType)-> Unit) :
        TabItem("Filter", { FilterScreen(
            removeFilter = removeFilter,
            addFilters = addFilters,
            filters = filters
        ) })


    data class Sort(
        val sortType: SortType,
        val isSortDesc: Boolean,
        val onSortSelected:(SortType) -> Unit) :
        TabItem("Sort", { SortScreen(
            sortType, isSortDesc, onSortSelected
        ) })


    data class Display(
        val layoutType: LayoutType,
        val onLayoutSelected: (DisplayMode) -> Unit) :
        TabItem("Display", { DisplayScreen(
            layoutType, onLayoutSelected
        ) })

}