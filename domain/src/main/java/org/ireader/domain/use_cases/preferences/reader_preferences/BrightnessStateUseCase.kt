package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.theme.AppPreferences

class BrightnessStateUseCase(
    private val appPreferences: AppPreferences,
) {
    fun saveBrightness(brightness: Float) {
        return appPreferences.brightness().set(brightness)
    }

    suspend fun readBrightness(): Float {
        return appPreferences.brightness().get()
    }

    fun saveAutoBrightness(brightness: Boolean) {
        return appPreferences.autoBrightness().set(brightness)
    }

    suspend fun readAutoBrightness(): Boolean {
        return appPreferences.autoBrightness().get()
    }
}

class ScrollModeUseCase(
    private val appPreferences: AppPreferences,
) {
    fun save(mode: Boolean) {
        return appPreferences.scrollMode().set(mode)
    }

    suspend fun read(): Boolean {
        return appPreferences.scrollMode().get()
    }
}

class ImmersiveModeUseCase(
    private val appPreferences: AppPreferences,
) {
    fun save(mode: Boolean) {
        return appPreferences.immersiveMode().set(mode)
    }

    suspend  fun read(): Boolean {
        return appPreferences.immersiveMode().get()
    }
}

class ScrollIndicatorUseCase(
    private val appPreferences: AppPreferences,
) {
    fun saveWidth(value: Int) {
        if (value > 0) {
            return appPreferences.scrollIndicatorWith().set(value)
        }
    }

    suspend fun readWidth(): Int {
        return appPreferences.scrollIndicatorWith().get()
    }

    fun savePadding(value: Int) {
        if (value > 0) {
            return appPreferences.scrollIndicatorPadding().set(value)
        }
    }

    suspend fun readPadding(): Int {
        return appPreferences.scrollIndicatorPadding().get()
    }
    suspend fun isShow(): Boolean {
        return appPreferences.showScrollIndicator().get()
    }
    fun setIsShown(show:Boolean) {
        return appPreferences.showScrollIndicator().set(show)
    }
}

class AutoScrollMode(
    private val appPreferences: AppPreferences,
) {
    fun saveInterval(value: Long) {
        return appPreferences.autoScrollInterval().set(value)
    }

    suspend fun readInterval(): Long {
        return appPreferences.autoScrollInterval().get()
    }

    fun saveOffset(value: Int) {
        return appPreferences.autoScrollOffset().set(value)
    }

    suspend fun readOffset(): Int {
        return appPreferences.autoScrollOffset().get()
    }
}
