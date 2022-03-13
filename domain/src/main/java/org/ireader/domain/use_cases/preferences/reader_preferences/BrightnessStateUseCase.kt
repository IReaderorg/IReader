package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.theme.AppPreferences
import javax.inject.Inject

class BrightnessStateUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    fun save(brightness: Float) {
        return appPreferences.brightness().set(brightness)
    }

    fun read(): Float {
        return appPreferences.brightness().get()
    }

}
