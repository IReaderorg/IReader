//package ireader.domain.preferences
//
//import ireader.core.api.prefs.Preference
//import ireader.core.api.prefs.PreferenceStore
//import ireader.core.api.prefs.getEnum
//import ireader.domain.models.prefs.PreferenceValues
//
//class CommonPreferences constructor(
//    private val preferenceStore: PreferenceStore,
//
//)  {
//    fun scrollbarMode(): Preference<PreferenceValues.ScrollbarSelectionMode> {
//        return preferenceStore.getEnum(
//            "scroll_indicator_is_draggable",
//            ireader.domain.models.prefs.PreferenceValues.ScrollbarSelectionMode.Full
//        )
//    }
//
//
//    fun themeMode(): Preference<ireader.domain.models.prefs.PreferenceValues.ThemeMode> {
//        return preferenceStore.getEnum("theme_mode", ireader.domain.models.prefs.PreferenceValues.ThemeMode.System)
//    }
//    fun installerMode(): Preference<ireader.domain.models.prefs.PreferenceValues.Installer> {
//        return preferenceStore.getEnum("installer_mode", ireader.domain.models.prefs.PreferenceValues.Installer.AndroidPackageManager)
//    }
//    fun relativeTime(): Preference<ireader.domain.models.prefs.PreferenceValues.RelativeTime> {
//        return preferenceStore.getEnum("relative_time", ireader.domain.models.prefs.PreferenceValues.RelativeTime.Day)
//    }
//    fun secureScreen(): Preference<ireader.domain.models.prefs.PreferenceValues.SecureScreenMode> {
//        return preferenceStore.getEnum("secure_screen", ireader.domain.models.prefs.PreferenceValues.SecureScreenMode.NEVER)
//    }
//}