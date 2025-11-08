package ireader.domain.preferences.prefs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import ireader.core.prefs.getEnum
import ireader.domain.models.prefs.PreferenceValues
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale


class UiPreferences(private val preferenceStore: PreferenceStore) {

    fun themeMode(): Preference<PreferenceValues.ThemeMode> {
        return preferenceStore.getEnum("theme_mode", PreferenceValues.ThemeMode.System)
    }

    fun dynamicColorMode(): Preference<Boolean> {
        return preferenceStore.getBoolean("dynamic_color_mode", false)
    }
        fun installerMode(): Preference<PreferenceValues.Installer> {
        return preferenceStore.getEnum("installer_mode", ireader.domain.models.prefs.PreferenceValues.Installer.AndroidPackageManager)
    }
    fun showSystemWideCatalogs(): Preference<Boolean> {
        return preferenceStore.getBoolean("show_system_catalogs", true)
    }
    fun showLocalCatalogs(): Preference<Boolean> {
        return preferenceStore.getBoolean("show_local_catalogs", true)
    }
    fun autoCatalogUpdater(): Preference<Boolean> {
        return preferenceStore.getBoolean("auto_catalog_updater_catalogs", false)
    }
    fun savedLocalCatalogLocation(): Preference<Boolean> {
        return preferenceStore.getBoolean("saved_local_catalog_location", false)
    }
    fun lastBackUpTime(): Preference<Long> {
        return preferenceStore.getLong("last_automatic_backup_time", 0)
    }
    fun automaticBackupTime(): Preference<PreferenceValues.AutomaticBackup> {
        return preferenceStore.getEnum("automatic_backup_time", PreferenceValues.AutomaticBackup.Off)
    }
    fun maxAutomaticBackupFiles(): Preference<Int> {
        return preferenceStore.getInt("max_automatic_backup_size", 5)
    }

    fun colorTheme(): Preference<Long> {
        return preferenceStore.getLong("color_theme", -1L)
    }
    fun showUpdatesAfter(): Preference<Long> {
        return preferenceStore.getLong("show_updates_after", 0)
    }
    fun defaultRepository() : Preference<Long> {
        return preferenceStore.getLong("default_repository", -1)
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

    fun relativeTime(): Preference<PreferenceValues.RelativeTime> {
        return preferenceStore.getEnum("relative_time", PreferenceValues.RelativeTime.Day)
    }

    fun getDateFormat(format: String = dateFormat().get()): DateFormat = when (format) {
        "" -> DateFormat.getDateInstance(DateFormat.SHORT)
        else -> SimpleDateFormat(format, Locale.getDefault())
    }

    fun downloadedOnly(): Preference<Boolean> {
        return preferenceStore.getBoolean("downloaded_only", false)
    }

    fun incognitoMode(): Preference<Boolean> {
        return preferenceStore.getBoolean("incognito_mode", false)
    }

    fun useAuthenticator(): Preference<Boolean> {
        return preferenceStore.getBoolean("use_authenticator", false)
    }

    fun lastAppUnlock(): Preference<Long> {
        return preferenceStore.getLong("last_app_unlock", 0)
    }

    fun lockAppAfter(): Preference<Long> {
        return preferenceStore.getLong("lock_app_after", 0)
    }

    var isAppLocked by mutableStateOf(true)

    fun secureScreen(): Preference<PreferenceValues.SecureScreenMode> {
        return preferenceStore.getEnum("secure_screen", PreferenceValues.SecureScreenMode.NEVER)
    }

    fun lastUsedSource(): Preference<Long> {
        return preferenceStore.getLong("last_used_source", -1L)
    }

    fun showUpdatesInButtonBar(): Preference<Boolean> {
        return preferenceStore.getBoolean("show_updates_in_bottom_bar", true)
    }

    fun showHistoryInButtonBar(): Preference<Boolean> {
        return preferenceStore.getBoolean("show_history_in_bottom_bar", true)
    }

    fun showLanguageFilter(): Preference<Boolean> {
        return preferenceStore.getBoolean("show_language_filter", true)
    }
}
