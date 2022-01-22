package ir.kazemcodes.infinity.feature_library.presentation.components
sealed class SortType(val name: String, val index: Int) {
    object DateAdded : SortType("Date Added", 0)
    object Alphabetically : SortType("Alphabetically", 1)
    object LastRead : SortType("Last Read", 2)
    object TotalChapter : SortType("TotalChapter", 3)
}

sealed class FilterType(val name: String, val index: Int) {
    object Disable : FilterType("Disable", 0)
    object Unread : FilterType("Unread", 1)
}