package ir.kazemcodes.infinity.feature_library.presentation

import ir.kazemcodes.infinity.core.presentation.layouts.DisplayMode
import ir.kazemcodes.infinity.feature_library.presentation.components.FilterType
import ir.kazemcodes.infinity.feature_library.presentation.components.SortType

interface LibraryScreenActions {
    fun getLibraryBooks()
    fun searchBook(query: String)
    fun updateSearchInput(query: String)
    fun toggleSearchMode(inSearchMode: Boolean? = null)
    fun updateLayoutType(layoutType: DisplayMode)
    fun readLayoutType()
    fun deleteNotInLibraryChapters()
    fun changeSortIndex(sortType: SortType)
    fun enableUnreadFilter(filterType: FilterType)
    fun getLibraryDataIfSearchModeIsOff(inSearchMode: Boolean)
    fun setExploreModeOffForInLibraryBooks()
}