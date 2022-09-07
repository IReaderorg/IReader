package ireader.domain.use_cases.preferences.reader_preferences

import ireader.core.ui.preferences.ReaderPreferences

class BrightnessStateUseCase(
    private val prefs: ReaderPreferences,
) {
    fun saveBrightness(brightness: Float) {
        return prefs.brightness().set(brightness)
    }

    suspend fun readBrightness(): Float {
        return prefs.brightness().get()
    }

    fun saveAutoBrightness(brightness: Boolean) {
        return prefs.autoBrightness().set(brightness)
    }

    suspend fun readAutoBrightness(): Boolean {
        return prefs.autoBrightness().get()
    }
}

class ScrollModeUseCase(
    private val prefs: ReaderPreferences,
) {
    fun save(mode: Boolean) {
        return prefs.scrollMode().set(mode)
    }

    suspend fun read(): Boolean {
        return prefs.scrollMode().get()
    }
}

class ImmersiveModeUseCase(
    private val prefs: ReaderPreferences,
) {
    fun save(mode: Boolean) {
        return prefs.immersiveMode().set(mode)
    }

    suspend fun read(): Boolean {
        return prefs.immersiveMode().get()
    }
}

class ScrollIndicatorUseCase(
    private val prefs: ReaderPreferences,
) {
    fun saveWidth(value: Int) {
        if (value > 0) {
            return prefs.scrollIndicatorWith().set(value)
        }
    }

    suspend fun readWidth(): Int {
        return prefs.scrollIndicatorWith().get()
    }

    fun savePadding(value: Int) {
        if (value > 0) {
            return prefs.scrollIndicatorPadding().set(value)
        }
    }

    suspend fun readPadding(): Int {
        return prefs.scrollIndicatorPadding().get()
    }
    suspend fun isShow(): Boolean {
        return prefs.showScrollIndicator().get()
    }
    fun setIsShown(show: Boolean) {
        return prefs.showScrollIndicator().set(show)
    }
}

class AutoScrollMode(
    private val prefs: ReaderPreferences,
) {
    fun saveInterval(value: Long) {
        return prefs.autoScrollInterval().set(value)
    }

    suspend fun readInterval(): Long {
        return prefs.autoScrollInterval().get()
    }

    fun saveOffset(value: Int) {
        return prefs.autoScrollOffset().set(value)
    }

    suspend fun readOffset(): Int {
        return prefs.autoScrollOffset().get()
    }
}
