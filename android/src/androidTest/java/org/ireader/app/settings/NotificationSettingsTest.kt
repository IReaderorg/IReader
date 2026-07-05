package org.ireader.app.settings

import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.filters.LargeTest
import org.ireader.app.BaseComposeTest
import org.junit.Before
import org.junit.Test

/**
 * E2E tests for the Notification Settings screen.
 *
 * Screen: SettingsNotificationScreen.kt
 * Route: (accessed via settings main screen "Notifications" item)
 * ViewModel: SettingsNotificationViewModel
 *
 * UI elements tested:
 * - Library notifications: library update progress, new chapter notifications
 * - Download notifications: download progress, download complete
 * - System notifications: backup/restore, app updates, extension updates, error notifications
 * - Notification behavior: sound, vibration, LED, quiet hours, group notifications
 * - All toggles use SettingsSwitchItem composable
 */
@LargeTest
class NotificationSettingsTest : BaseComposeTest() {

    @Before
    fun navigateToNotificationSettings() {
        navigateToSettings()
        navigateToSubScreen("Notifications")
    }

    @Test
    fun notificationSettingsScreenDisplaysTitle() {
        assertTextDisplayed("Notifications")
    }

    @Test
    fun libraryNotificationsSectionIsVisible() {
        assertTextDisplayed("Library Notifications")
    }

    @Test
    fun libraryUpdateProgressToggleIsVisible() {
        assertTextDisplayed("Library Update Progress")
    }

    @Test
    fun newChapterNotificationsToggleIsVisible() {
        assertTextDisplayed("New Chapter Notifications")
    }

    @Test
    fun downloadNotificationsSectionIsVisible() {
        assertTextDisplayed("Download Notifications")
    }

    @Test
    fun downloadProgressToggleIsVisible() {
        assertTextDisplayed("Download Progress")
    }

    @Test
    fun downloadCompleteToggleIsVisible() {
        assertTextDisplayed("Download Complete")
    }

    @Test
    fun systemNotificationsSectionIsVisible() {
        assertTextDisplayed("System Notifications")
    }

    @Test
    fun backupRestoreNotificationsToggleIsVisible() {
        assertTextDisplayed("Backup & Restore")
    }

    @Test
    fun appUpdatesNotificationsToggleIsVisible() {
        assertTextDisplayed("App Updates")
    }

    @Test
    fun extensionUpdatesNotificationsToggleIsVisible() {
        assertTextDisplayed("Extension Updates")
    }

    @Test
    fun errorNotificationsToggleIsVisible() {
        assertTextDisplayed("Error Notifications")
    }

    @Test
    fun notificationBehaviorSectionIsVisible() {
        assertTextDisplayed("Notification Behavior")
    }

    @Test
    fun soundToggleIsVisible() {
        assertTextDisplayed("Sound")
    }

    @Test
    fun vibrationToggleIsVisible() {
        assertTextDisplayed("Vibration")
    }

    @Test
    fun toggleLibraryUpdateProgress() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Library Update Progress")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleNewChapterNotifications() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("New Chapter Notifications")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleDownloadProgress() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Download Progress")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleDownloadComplete() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Download Complete")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleAppUpdates() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("App Updates")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleExtensionUpdates() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Extension Updates")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleErrorNotifications() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Error Notifications")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleSound() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Sound")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleVibration() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Vibration")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun backNavigationReturnsToSettings() {
        navigateBack()
        assertTextDisplayed("Settings")
    }
}
