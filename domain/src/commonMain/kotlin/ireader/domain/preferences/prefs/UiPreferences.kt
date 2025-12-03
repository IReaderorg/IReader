package ireader.domain.preferences.prefs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import ireader.core.prefs.getEnum
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.utils.extensions.formatDate


class UiPreferences(private val preferenceStore: PreferenceStore) {

    fun themeMode(): Preference<PreferenceValues.ThemeMode> {
        return preferenceStore.getEnum("theme_mode", PreferenceValues.ThemeMode.System)
    }

    fun dynamicColorMode(): Preference<Boolean> {
        return preferenceStore.getBoolean("dynamic_color_mode", false)
    }
    
    fun useTrueBlack(): Preference<Boolean> {
        return preferenceStore.getBoolean("use_true_black", false)
    }
    
    fun appUiFont(): Preference<String> {
        return preferenceStore.getString("app_ui_font", "default")
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
        return preferenceStore.getBoolean("saved_local_catalog_location", true)
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

    /**
     * Returns a function that formats timestamps according to user preferences.
     * Uses KMP-compatible date formatting.
     */
    fun getDateFormatter(format: String = dateFormat().get()): (Long) -> String = { timestamp ->
        timestamp.formatDate()
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

    fun groupHistoryByNovel(): Preference<Boolean> {
        return preferenceStore.getBoolean("group_history_by_novel", false)
    }
    
    // Security Preferences
    
    fun appLockEnabled(): Preference<Boolean> {
        return preferenceStore.getBoolean("app_lock_enabled", false)
    }
    
    fun appLockMethod(): Preference<String> {
        return preferenceStore.getString("app_lock_method", "none")
    }
    
    fun appLockPin(): Preference<String> {
        return preferenceStore.getString("app_lock_pin", "")
    }
    
    fun appLockPassword(): Preference<String> {
        return preferenceStore.getString("app_lock_password", "")
    }
    
    fun biometricEnabled(): Preference<Boolean> {
        return preferenceStore.getBoolean("biometric_enabled", false)
    }
    
    fun secureScreenEnabled(): Preference<Boolean> {
        return preferenceStore.getBoolean("secure_screen_enabled", false)
    }
    
    fun hideContentEnabled(): Preference<Boolean> {
        return preferenceStore.getBoolean("hide_content_enabled", false)
    }
    
    fun adultSourceLockEnabled(): Preference<Boolean> {
        return preferenceStore.getBoolean("adult_source_lock_enabled", false)
    }
    
    // TTS Preferences
    
    fun selectedVoiceId(): Preference<String> {
        return preferenceStore.getString("selected_voice_id", "en-us-amy-low")
    }
    
    fun ttsEnabled(): Preference<Boolean> {
        return preferenceStore.getBoolean("tts_enabled", false)
    }
    
    fun ttsSpeechRate(): Preference<Float> {
        return preferenceStore.getFloat("tts_speech_rate", 1.0f)
    }
    
    fun ttsAutoPlay(): Preference<Boolean> {
        return preferenceStore.getBoolean("tts_auto_play", false)
    }
    
    // ============================================================================
    // General Settings Enhancements
    // ============================================================================
    
    // Library Preferences
    
    /**
     * Whether to use a Floating Action Button instead of toolbar buttons in the Library screen.
     * Default: false (use toolbar buttons)
     */
    fun useFabInLibrary(): Preference<Boolean> {
        return preferenceStore.getBoolean("use_fab_in_library", false)
    }
    
    /**
     * Default chapter sort order for all novels.
     * Values: "SOURCE_ORDER", "CHAPTER_NUMBER", "UPLOAD_DATE_ASC", "UPLOAD_DATE_DESC"
     * Default: "SOURCE_ORDER"
     */
    fun defaultChapterSort(): Preference<String> {
        return preferenceStore.getString("default_chapter_sort", "SOURCE_ORDER")
    }
    
    // Global Search Preferences
    
    /**
     * Whether to only add novels to library when they are found in search results.
     * When true: Only add novels when search returns results
     * When false: Add novels immediately when user clicks "Add to Library"
     * Default: false
     */
    fun onlyUpdateOnFinding(): Preference<Boolean> {
        return preferenceStore.getBoolean("only_update_on_finding", false)
    }
    
    /**
     * Whether to display the last update time for each novel in the library.
     * Shows "Last updated: X hours ago" below novel title when enabled.
     * Default: true
     */
    fun showLastUpdateTime(): Preference<Boolean> {
        return preferenceStore.getBoolean("show_last_update_time", true)
    }
    
    // Auto Download Preferences
    
    /**
     * Whether to automatically download newly detected chapters in the background.
     * When enabled, new chapters are automatically added to the download queue.
     * Default: false
     */
    fun autoDownloadNewChapters(): Preference<Boolean> {
        return preferenceStore.getBoolean("auto_download_new_chapters", false)
    }
    
    // User Interface Preferences
    
    /**
     * Whether to disable haptic feedback (vibration) for interactions.
     * When enabled, all vibration feedback is suppressed throughout the app.
     * Default: false (haptic feedback enabled)
     */
    fun disableHapticFeedback(): Preference<Boolean> {
        return preferenceStore.getBoolean("disable_haptic_feedback", false)
    }
    
    /**
     * Whether to disable loading animations.
     * When enabled, static progress indicators are used instead of animated ones.
     * Default: false (animations enabled)
     */
    fun disableLoadingAnimations(): Preference<Boolean> {
        return preferenceStore.getBoolean("disable_loading_animations", false)
    }
    
    // ============================================================================
    // Appearance Settings for Novel Info
    // ============================================================================
    
    /**
     * Whether to hide backdrop images on novel detail screens.
     * When enabled, backdrop images are hidden and replaced with solid background.
     * This improves performance and provides a cleaner UI.
     * Default: false (show backdrop)
     */
    fun hideNovelBackdrop(): Preference<Boolean> {
        return preferenceStore.getBoolean("hide_novel_backdrop", false)
    }
    
    /**
     * Whether to use a Floating Action Button instead of standard buttons on novel detail screens.
     * When enabled, action buttons are replaced with a FAB in the bottom-right corner.
     * Default: false (use standard buttons)
     */
    fun useFabInNovelInfo(): Preference<Boolean> {
        return preferenceStore.getBoolean("use_fab_in_novel_info", false)
    }
    
    // ============================================================================
    // JavaScript Plugin Settings
    // ============================================================================
    
    /**
     * Whether to enable JavaScript plugin support.
     * When enabled, LNReader-compatible JavaScript plugins can be loaded and used.
     * Default: false (disabled for security)
     */
    fun enableJSPlugins(): Preference<Boolean> {
        return preferenceStore.getBoolean("enable_js_plugins", true)
    }
    
    /**
     * Whether to automatically check for and install plugin updates.
     * When enabled, the app will check for updates every 24 hours.
     * Default: true
     */
    fun autoUpdateJSPlugins(): Preference<Boolean> {
        return preferenceStore.getBoolean("auto_update_js_plugins", true)
    }
    
    /**
     * Whether to enable debug mode for JavaScript plugins.
     * When enabled, detailed execution logs are generated for troubleshooting.
     * Default: false
     */
    fun jsPluginDebugMode(): Preference<Boolean> {
        return preferenceStore.getBoolean("js_plugin_debug_mode", false)
    }


    /**
     * Timeout for plugin method execution in seconds.
     * Range: 10-60 seconds
     * Default: 30 seconds
     */
    fun jsPluginTimeout(): Preference<Int> {
        return preferenceStore.getInt("js_plugin_timeout", 30)
    }
    
    /**
     * Memory limit per plugin in megabytes.
     * Default: 64 MB
     */
    fun jsPluginMemoryLimit(): Preference<Int> {
        return preferenceStore.getInt("js_plugin_memory_limit", 64)
    }
    
    // ============================================================================
    // Plugin Theme Settings
    // ============================================================================
    
    /**
     * Selected plugin theme ID.
     * Format: "{pluginId}_{light|dark}" for plugin themes, or theme ID string for built-in themes
     * Default: "" (no plugin theme selected)
     */
    fun selectedPluginTheme(): Preference<String> {
        return preferenceStore.getString("selected_plugin_theme", "")
    }
    
    // ============================================================================
    // Leaderboard Settings
    // ============================================================================
    
    /**
     * Whether to enable realtime updates for the leaderboard.
     * When enabled, leaderboard updates automatically via websocket connection.
     * When disabled, leaderboard only updates on manual refresh.
     * Default: false (manual refresh only)
     */
    fun leaderboardRealtimeEnabled(): Preference<Boolean> {
        return preferenceStore.getBoolean("leaderboard_realtime_enabled", false)
    }
    
    /**
     * Whether to enable realtime updates for the donation leaderboard.
     * When enabled, donation leaderboard updates automatically via websocket connection.
     * When disabled, donation leaderboard only updates on manual refresh.
     * Default: false (manual refresh only)
     */
    fun donationLeaderboardRealtimeEnabled(): Preference<Boolean> {
        return preferenceStore.getBoolean("donation_leaderboard_realtime_enabled", false)
    }
    
    // ============================================================================
    // Performance Settings
    // ============================================================================
    
    /**
     * Whether to enable maximum performance mode.
     * When enabled, all animations and visual effects are disabled for fastest possible rendering.
     * This includes: crossfade animations, loading placeholders, shadows, blur effects.
     * Recommended for older devices or users who prefer instant response over visual polish.
     * Default: false (normal mode with animations)
     */
    fun maxPerformanceMode(): Preference<Boolean> {
        return preferenceStore.getBoolean("max_performance_mode", false)
    }
    
    // ============================================================================
    // First Launch Settings
    // ============================================================================
    
    /**
     * Whether the user has seen and completed the first launch dialog.
     * This dialog shows important setup options like Supabase/cloud features toggle.
     * Default: false (show dialog on first launch)
     */
    fun hasCompletedFirstLaunch(): Preference<Boolean> {
        return preferenceStore.getBoolean("has_completed_first_launch", false)
    }
}
