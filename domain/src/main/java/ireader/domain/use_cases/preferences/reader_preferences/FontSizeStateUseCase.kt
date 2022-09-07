package ireader.domain.use_cases.preferences.reader_preferences

import ireader.core.ui.preferences.ReaderPreferences

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
