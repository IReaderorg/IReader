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

class ScrollIndicatorUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    fun saveWidth(value: Int) {
        return appPreferences.scrollIndicatorWith().set(value)
    }

    fun readWidth(): Int {
        return appPreferences.scrollIndicatorWith().get()
    }

    fun savePadding(value: Int) {
        return appPreferences.scrollIndicatorPadding().set(value)
    }

    fun readPadding(): Int {
        return appPreferences.scrollIndicatorPadding().get()
    }

}

class AutoScrollMode @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    fun saveInterval(value: Long) {
        return appPreferences.autoScrollInterval().set(value)
    }

    fun readInterval(): Long {
        return appPreferences.autoScrollInterval().get()
    }

    fun saveOffset(value: Int) {
        return appPreferences.autoScrollOffset().set(value)
    }

    fun readOffset(): Int {
        return appPreferences.autoScrollOffset().get()
    }


}