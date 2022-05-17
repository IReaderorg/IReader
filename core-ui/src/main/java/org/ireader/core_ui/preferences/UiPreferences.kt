package org.ireader.core_ui.preferences

import org.ireader.core_api.prefs.Preference
import org.ireader.core_api.prefs.PreferenceStore
import org.ireader.core_api.prefs.getEnum
import org.ireader.core_ui.theme.ThemeMode

class UiPreferences(private val preferenceStore: PreferenceStore) {

    fun themeMode(): Preference<ThemeMode> {
        return preferenceStore.getEnum("theme_mode", ThemeMode.System)
    }

    fun colorTheme(): Preference<Int> {
        return preferenceStore.getInt("color_theme", 0)
    }

    fun colorPrimaryLight(): Preference<Int> {
        return preferenceStore.getInt("color_primary_light", 0)
    }

    fun colorPrimaryDark(): Preference<Int> {
        return preferenceStore.getInt("color_primary_dark", 0)
    }

    fun colorSecondaryLight(): Preference<Int> {
        return preferenceStore.getInt("color_secondary_light", 0)
    }

    fun colorSecondaryDark(): Preference<Int> {
        return preferenceStore.getInt("color_secondary_dark", 0)
    }

    fun colorBarsLight(): Preference<Int> {
        return preferenceStore.getInt("color_bar_light", 0)
    }

    fun colorBarsDark(): Preference<Int> {
        return preferenceStore.getInt("color_bar_dark", 0)
    }

    fun confirmExit(): Preference<Boolean> {
        return preferenceStore.getBoolean("confirm_exit", false)
    }

    fun hideBottomBarOnScroll(): Preference<Boolean> {
        return preferenceStore.getBoolean("hide_bottom_bar_on_scroll", true)
    }

    fun language(): Preference<String> {
        return preferenceStore.getString("language", "")
    }

    fun dateFormat(): Preference<String> {
        return preferenceStore.getString("date_format", "")
    }

    fun downloadedOnly(): Preference<Boolean> {
        return preferenceStore.getBoolean("downloaded_only", false)
    }

    fun incognitoMode(): Preference<Boolean> {
        return preferenceStore.getBoolean("incognito_mode", false)
    }
    fun lastUsedSource(): Preference<Long> {
        return preferenceStore.getLong("last_used_source", -1L)
    }
}
