package ireader.domain.usecases.preferences.reader_preferences

import ireader.domain.preferences.prefs.ReaderPreferences

class FontHeightUseCase(
    private val prefs: ReaderPreferences,
) {
    fun save(fontHeight: Int) {
        prefs.lineHeight().set(fontHeight)
    }

    suspend fun read(): Int {
        return prefs.lineHeight().get()
    }
}
