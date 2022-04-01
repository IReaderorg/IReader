package org.ireader.domain.models

sealed class SortType(val name: String, val index: Int) {
    object Alphabetically : SortType("Alphabetically", 0)
    object LastRead : SortType("Last Read", 1)
    object LastChecked : SortType("Last Checked", 2)
    object Unread : SortType("Unread", 3)
    object TotalChapters : SortType("Total Chapters", 4)
    object LatestChapter : SortType("Latest Chapter", 5)
    object DateFetched : SortType("Date Fetched", 6)
    object DateAdded : SortType("Date Added", 7)
}

sealed class FilterType(val name: String, val index: Int) {
    object Disable : FilterType("Disable", 0)
    object Unread : FilterType("Unread", 1)
    object Downloaded : FilterType("Downloaded", 2)
    object Completed : FilterType("Completed", 3)
}