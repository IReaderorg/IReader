package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.theme.AppPreferences
import javax.inject.Inject

class SaveBrightnessStateUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(brightness: Float) {
        return appPreferences.brightness().set(brightness)
    }
}

class ReadBrightnessStateUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(): Float {
        return appPreferences.brightness().get()
    }
}