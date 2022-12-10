package ireader.domain.usecases.preferences.reader_preferences

import androidx.compose.ui.graphics.Color
import ireader.domain.preferences.prefs.AndroidUiPreferences
import ireader.domain.preferences.prefs.ReaderPreferences

class BackgroundColorUseCase(
    private val prefs: AndroidUiPreferences,
) {
    fun save(value: Color) {
        prefs.backgroundColorReader().set(value)
    }

    suspend fun read(): Color {
        return prefs.backgroundColorReader().get()
    }
}

class TextColorUseCase(
    private val prefs: AndroidUiPreferences,
) {
    fun save(value: Color) {
        prefs.textColorReader().set(value)
    }

    suspend fun read(): Color {
        return prefs.textColorReader().get()
    }
}
