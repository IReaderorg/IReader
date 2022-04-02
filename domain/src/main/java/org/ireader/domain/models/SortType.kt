package org.ireader.domain.models

sealed class SortType(val name: String, val index: Int) {
    object Alphabetically : SortType("Alphabetically", 0)
    object LastRead : SortType("Last Read", 1)
    object LastChecked : SortType("Last Checked", 2)
    object TotalChapters : SortType("Total Chapters", 3)
    object LatestChapter : SortType("Latest Chapter", 4)
    object DateFetched : SortType("Date Fetched", 5)
    object DateAdded : SortType("Date Added", 6)
}

sealed class FilterType(val name: String, val index: Int) {
    object Disable : FilterType("Disable", 0)
    object Unread : FilterType("Unread", 1)
    object Downloaded : FilterType("Downloaded", 2)
    object Completed : FilterType("Completed", 3)
}