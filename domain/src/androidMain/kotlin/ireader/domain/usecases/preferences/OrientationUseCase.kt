package ireader.domain.usecases.preferences

import ireader.domain.models.library.LibrarySort
import ireader.domain.models.library.deserialize
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.models.prefs.IReaderVoice
import ireader.domain.preferences.prefs.AndroidUiPreferences


class TextAlignmentUseCase(
    private val prefs: ReaderPreferences,
) {
    fun save(textAlign: PreferenceValues.PreferenceTextAlignment) {
        prefs.textAlign().set(textAlign)
    }

    suspend fun read(): PreferenceValues.PreferenceTextAlignment {
        return prefs.textAlign().get()
    }
}


