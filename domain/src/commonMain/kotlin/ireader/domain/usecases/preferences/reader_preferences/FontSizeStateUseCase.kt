package ireader.domain.usecases.preferences.reader_preferences

import ireader.domain.models.library.LibrarySort
import ireader.domain.models.library.deserialize
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.ReaderPreferences

class FontSizeStateUseCase(
    private val prefs: ReaderPreferences,
) {
    fun save(fontSize: Int) {
        prefs.fontSize().set(fontSize)
    }

    suspend fun read(): Int {
        return prefs.fontSize().get()
    }
}
class SortersUseCase(
        private val appPreferences: AppPreferences,
) {
    fun save(value: String) {
        appPreferences.sortLibraryScreen().set(value)
    }

    suspend fun read(): LibrarySort {
        return LibrarySort.deserialize(appPreferences.sortLibraryScreen().read())
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
