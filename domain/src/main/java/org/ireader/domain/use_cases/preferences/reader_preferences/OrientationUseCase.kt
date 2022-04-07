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

class SortersDescUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    fun save(value: Boolean) {
        appPreferences.sortDescLibraryScreen().set(value)
    }

    fun read(): Boolean {
        return appPreferences.sortDescLibraryScreen().get()
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
            SortType.TotalChapters
        }
        4 -> {
            SortType.LatestChapter
        }
        5 -> {
            SortType.DateFetched
        }
        6 -> {
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


class TextReaderPrefUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    fun savePitch(value: Float) {
        appPreferences.speechRate().set(value)
    }

    fun readPitch(): Float {
        return appPreferences.speechRate().get()
    }

    fun saveRate(value: Float) {
        appPreferences.speechPitch().set(value)
    }

    fun readRate(): Float {
        return appPreferences.speechPitch().get()
    }

    fun saveLanguage(value: String) {
        appPreferences.speechLanguage().set(value)
    }

    fun readLanguage(): String {
        return appPreferences.speechLanguage().get()
    }

    fun saveVoice(value: String) {
        appPreferences.speechVoice().set(value)
    }

    fun readVoice(): String {
        return appPreferences.speechVoice().get()
    }

    fun saveAutoNext(value: Boolean) {
        appPreferences.readerAutoNext().set(value)
    }

    fun readAutoNext(): Boolean {
        return appPreferences.readerAutoNext().get()
    }

}