package org.ireader.core.prefs

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


enum class AppTheme {
    MODE_AUTO,
    MODE_DAY,
    MODE_NIGHT;

    companion object {
        fun fromOrdinal(ordinal: Int) = values()[ordinal]
    }
}

interface ThemeSetting {
    val themeStream: StateFlow<AppTheme>
    var theme: AppTheme
}

class ThemeSettingPreference @Inject constructor(
    private val preferences: SharedPreferences,
) : ThemeSetting {

    override val themeStream: MutableStateFlow<AppTheme>
    override var theme: AppTheme by AppThemePreferenceDelegate("app_theme", AppTheme.MODE_AUTO)


    init {
        themeStream = MutableStateFlow(theme)
    }

    inner class AppThemePreferenceDelegate(
        private val name: String,
        private val default: AppTheme,
    ) : ReadWriteProperty<Any?, AppTheme> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): AppTheme =
            AppTheme.fromOrdinal(preferences.getInt(name, default.ordinal))

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: AppTheme) {
            themeStream.value = value
            preferences.edit {
                putInt(name, value.ordinal)
            }
        }

    }
}


