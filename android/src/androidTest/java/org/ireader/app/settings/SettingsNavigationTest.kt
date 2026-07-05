package org.ireader.app.settings

import androidx.test.filters.LargeTest
import org.ireader.app.BaseComposeTest
import org.junit.Test

/**
 * E2E tests for navigating between the main Settings screen and all sub-screens.
 * Verifies that each settings category is reachable and that back navigation works.
 *
 * Routes (from NavigationRoutes.kt):
 * - settings → main settings screen
 * - appearance → Appearance settings
 * - readerSettings → Reader settings
 * - generalSettings → General settings
 * - securitySettings → Security settings
 * - advanceSettings → Advanced settings
 * - trackingSettings → Tracking settings
 * - cloudBackup → Cloud Backup settings
 * - downloader → Download settings
 * - about → About screen
 * - networkSettings → Network settings
 */
@LargeTest
class SettingsNavigationTest : BaseComposeTest() {

    @Test
    fun settingsScreenIsReachableFromMainScreen() {
        navigateToSettings()
        // Verify the main settings screen title is displayed
        assertTextDisplayed("Settings")
    }

    @Test
    fun navigateToAppearanceSettingsAndBack() {
        navigateToSettings()
        navigateToSubScreen("Appearance")
        assertTextDisplayed("Appearance")
        navigateBack()
        assertTextDisplayed("Settings")
    }

    @Test
    fun navigateToReaderSettingsAndBack() {
        navigateToSettings()
        navigateToSubScreen("Reader")
        assertTextDisplayed("Reader")
        navigateBack()
        assertTextDisplayed("Settings")
    }

    @Test
    fun navigateToLibrarySettingsAndBack() {
        navigateToSettings()
        navigateToSubScreen("Library")
        assertTextDisplayed("Library")
        navigateBack()
        assertTextDisplayed("Settings")
    }

    @Test
    fun navigateToDownloadSettingsAndBack() {
        navigateToSettings()
        navigateToSubScreen("Downloads")
        assertTextDisplayed("Downloads")
        navigateBack()
        assertTextDisplayed("Settings")
    }

    @Test
    fun navigateToTrackingSettingsAndBack() {
        navigateToSettings()
        navigateToSubScreen("Tracking")
        assertTextDisplayed("Tracking")
        navigateBack()
        assertTextDisplayed("Settings")
    }

    @Test
    fun navigateToBackupSettingsAndBack() {
        navigateToSettings()
        navigateToSubScreen("Backup & Restore")
        assertTextDisplayed("Cloud Backup")
        navigateBack()
        assertTextDisplayed("Settings")
    }

    @Test
    fun navigateToDataSettingsAndBack() {
        navigateToSettings()
        navigateToSubScreen("Data & Storage")
        assertTextDisplayed("Data & Storage")
        navigateBack()
        assertTextDisplayed("Settings")
    }

    @Test
    fun navigateToSecuritySettingsAndBack() {
        navigateToSettings()
        navigateToSubScreen("Security & Privacy")
        assertTextDisplayed("Security & Privacy")
        navigateBack()
        assertTextDisplayed("Settings")
    }

    @Test
    fun navigateToNotificationSettingsAndBack() {
        navigateToSettings()
        navigateToSubScreen("Notifications")
        assertTextDisplayed("Notifications")
        navigateBack()
        assertTextDisplayed("Settings")
    }

    @Test
    fun navigateToAdvancedSettingsAndBack() {
        navigateToSettings()
        navigateToSubScreen("Advanced")
        assertTextDisplayed("Advanced")
        navigateBack()
        assertTextDisplayed("Settings")
    }

    @Test
    fun allSettingsSectionsAreVisibleOnMainScreen() {
        navigateToSettings()
        // Verify all section headers and items from SettingsMainScreen.kt
        assertTextDisplayed("Appearance & Theme")
        assertTextDisplayed("Reading Experience")
        assertTextDisplayed("Library Management")
        assertTextDisplayed("Data & Backup")
        assertTextDisplayed("Extensions & Sources")
        assertTextDisplayed("Security & Privacy")
        assertTextDisplayed("System & Notifications")
    }

    @Test
    fun allSettingsItemsAreVisibleOnMainScreen() {
        navigateToSettings()
        // Verify all clickable settings items from SettingsMainScreen.kt
        assertTextDisplayed("Appearance")
        assertTextDisplayed("Reader")
        assertTextDisplayed("Library")
        assertTextDisplayed("Downloads")
        assertTextDisplayed("Tracking")
        assertTextDisplayed("Backup & Restore")
        assertTextDisplayed("Data & Storage")
        assertTextDisplayed("Security & Privacy")
        assertTextDisplayed("Notifications")
        assertTextDisplayed("Advanced")
    }
}
