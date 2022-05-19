package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.common_models.FilterType
import org.ireader.common_models.SortType
import org.ireader.core_ui.preferences.AppPreferences
import org.ireader.core_ui.preferences.ReaderPreferences
import org.ireader.core_ui.theme.OrientationMode
import org.ireader.core_ui.theme.prefs.IReaderVoice
import org.ireader.core_ui.ui.PreferenceAlignment
import javax.inject.Inject

class OrientationUseCase(
    private val prefs: ReaderPreferences,
) {
    fun save(orientation: OrientationMode) {
        prefs.orientation().set(orientation)
    }

    suspend fun read(): OrientationMode {
        return prefs.orientation().get()
    }
}

class TextAlignmentUseCase(
    private val prefs: ReaderPreferences,
) {
    fun save(textAlign: PreferenceAlignment) {
        prefs.textAlign().set(textAlign)
    }

    suspend fun read(): PreferenceAlignment {
        return prefs.textAlign().get()
    }
}

class SortersUseCase(
    private val appPreferences: AppPreferences,
) {
    fun save(value: Int) {
        appPreferences.sortLibraryScreen().set(value)
    }

    suspend fun read(): SortType {
        return mapSortType(appPreferences.sortLibraryScreen().read())
    }
}

class SortersDescUseCase(
    private val appPreferences: AppPreferences,
) {
    fun save(value: Boolean) {
        appPreferences.sortDescLibraryScreen().set(value)
    }

    suspend fun read(): Boolean {
        return appPreferences.sortDescLibraryScreen().read()
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
    private val prefs: ReaderPreferences,
) {
    fun savePitch(value: Float) {
        prefs.speechPitch().set(value)
    }

    suspend fun readPitch(): Float {
        return prefs.speechPitch().get()
    }

    fun saveRate(value: Float) {
        prefs.speechRate().set(value)
    }

    suspend fun readRate(): Float {
        return prefs.speechRate().get()
    }

    fun saveLanguage(value: String) {
        prefs.speechLanguage().set(value)
    }

    suspend fun readLanguage(): String {
        return prefs.speechLanguage().get()
    }

    fun saveVoice(value: IReaderVoice) {
        kotlin.runCatching {
            prefs.speechVoice().set(value)
        }
    }

    fun readVoice(): IReaderVoice? {
        /**
         * because the default value is "" which means the first value should return null
         */
        return kotlin.runCatching {
            return prefs.speechVoice().get()
        }.getOrNull()
    }

    fun saveAutoNext(value: Boolean) {
        prefs.readerAutoNext().set(value)
    }

    suspend fun readAutoNext(): Boolean {
        return prefs.readerAutoNext().get()
    }
}
