package ireader.domain.usecases.preferences.reader_preferences

import ireader.domain.models.common.DomainColor
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.ReaderPreferences

class BackgroundColorUseCase(
    private val prefs: AppPreferences,
) {
    fun save(value: DomainColor) {
        prefs.backgroundColorReader().set(value)
    }

    suspend fun read(): DomainColor {
        return prefs.backgroundColorReader().get()
    }
}
class TextColorUseCase(
    private val prefs: AppPreferences,
) {
    fun save(value: DomainColor) {
        prefs.textColorReader().set(value)
    }

    suspend fun read(): DomainColor {
        return prefs.textColorReader().get()
    }
}
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
