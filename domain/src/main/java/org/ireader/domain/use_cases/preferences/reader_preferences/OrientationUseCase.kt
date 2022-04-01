package org.ireader.domain.use_cases.preferences.reader_preferences


import org.ireader.core_ui.theme.AppPreferences
import org.ireader.core_ui.theme.OrientationMode
import org.ireader.domain.models.FilterType
import org.ireader.domain.models.SortType
import javax.inject.Inject

class OrientationUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    fun save(orientation: OrientationMode) {
        appPreferences.orientation().set(orientation)
    }

    fun read(): OrientationMode {
        return appPreferences.orientation().get()
    }
}


class SortersUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    fun save(value: Int) {
        appPreferences.sortLibraryScreen().set(value)
    }

    fun read(): SortType {
        return mapSortType(appPreferences.sortLibraryScreen().get())
    }

}

fun mapSortType(input: Int): SortType {
    return when (input) {
        0 -> {
            SortType.Alphabetically
        }
        1 -> {
            SortType.LastRead
        }
        2 -> {
            SortType.LastChecked
        }
        3 -> {
            SortType.Unread
        }
        4 -> {
            SortType.TotalChapters
        }
        5 -> {
            SortType.LatestChapter
        }
        6 -> {
            SortType.DateFetched
        }
        7 -> {
            SortType.DateAdded
        }
        else -> {
            SortType.LastRead
        }
    }
}


fun mapFilterType(input: Int): FilterType {
    return when (input) {
        0 -> {
            FilterType.Disable
        }
        else -> {
            FilterType.Unread
        }
    }
}


