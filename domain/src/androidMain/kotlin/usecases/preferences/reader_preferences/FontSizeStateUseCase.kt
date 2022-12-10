package ireader.domain.usecases.preferences.reader_preferences

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
