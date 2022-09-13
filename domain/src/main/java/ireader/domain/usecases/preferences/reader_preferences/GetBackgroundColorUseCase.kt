package ireader.domain.usecases.preferences.reader_preferences

import androidx.compose.ui.graphics.Color
import ireader.core.ui.preferences.ReaderPreferences

class BackgroundColorUseCase(
    private val prefs: ReaderPreferences,
) {
    fun save(value: Color) {
        prefs.backgroundColorReader().set(value)
    }

    suspend fun read(): Color {
        return prefs.backgroundColorReader().get()
    }
}

class TextColorUseCase(
    private val prefs: ReaderPreferences,
) {
    fun save(value: Color) {
        prefs.textColorReader().set(value)
    }

    suspend fun read(): Color {
        return prefs.textColorReader().get()
    }
}
