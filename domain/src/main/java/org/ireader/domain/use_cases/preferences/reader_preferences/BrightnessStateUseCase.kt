package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.theme.AppPreferences

class SaveBrightnessStateUseCase(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(brightness: Float) {
        return appPreferences.brightness().set(brightness)
    }
}

class ReadBrightnessStateUseCase(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(): Float {
        return appPreferences.brightness().get()
    }
}