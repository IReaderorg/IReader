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

class ScrollModeUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    fun save(mode: Boolean) {
        return appPreferences.scrollMode().set(mode)
    }

    fun read(): Boolean {
        return appPreferences.scrollMode().get()
    }

}