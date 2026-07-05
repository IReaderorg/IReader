package org.ireader.app.settings

import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.filters.LargeTest
import org.ireader.app.BaseComposeTest
import org.junit.Before
import org.junit.Test

/**
 * E2E tests for the Download Settings screen.
 *
 * Screen: SettingsDownloadScreen.kt
 * Route: downloader (NavigationRoutes.downloader)
 * ViewModel: SettingsDownloadViewModel
 *
 * UI elements tested:
 * - Download location (SettingsItemWithTrailing)
 * - Storage usage navigation
 * - Download only over WiFi toggle (SettingsSwitchItem)
 * - Download new chapters toggle (SettingsSwitchItem)
 * - Download categories navigation
 * - Auto delete chapters toggle (SettingsSwitchItem)
 * - Remove after reading toggle (SettingsSwitchItem)
 * - Remove after marked as read toggle (SettingsSwitchItem)
 * - Exclude categories navigation
 * - Save chapters as CBZ toggle (SettingsSwitchItem)
 * - Split tall images toggle (SettingsSwitchItem)
 * - Download queue navigation
 * - Clear download cache navigation
 * - Download location dialog (AlertDialog)
 * - Clear cache confirmation dialog (AlertDialog)
 */
@LargeTest
class DownloadSettingsTest : BaseComposeTest() {

    @Before
    fun navigateToDownloadSettings() {
        navigateToSettings()
        navigateToSubScreen("Downloads")
    }

    @Test
    fun downloadSettingsScreenDisplaysTitle() {
        assertTextDisplayed("Downloads")
    }

    @Test
    fun storageSectionIsVisible() {
        assertTextDisplayed("Storage")
    }

    @Test
    fun downloadLocationIsVisible() {
        assertTextDisplayed("Download Location")
    }

    @Test
    fun storageUsageIsVisible() {
        assertTextDisplayed("Storage Usage")
    }

    @Test
    fun downloadBehaviorSectionIsVisible() {
        assertTextDisplayed("Download Behavior")
    }

    @Test
    fun downloadOnlyOverWifiToggleIsVisible() {
        assertTextDisplayed("Download only over WiFi")
    }

    @Test
    fun automaticDownloadsSectionIsVisible() {
        assertTextDisplayed("Automatic Downloads")
    }

    @Test
    fun downloadNewChaptersToggleIsVisible() {
        assertTextDisplayed("Download new chapters")
    }

    @Test
    fun downloadCategoriesIsVisible() {
        assertTextDisplayed("Download Categories")
    }

    @Test
    fun autoDeleteSectionIsVisible() {
        assertTextDisplayed("Auto Delete")
    }

    @Test
    fun autoDeleteChaptersToggleIsVisible() {
        assertTextDisplayed("Auto Delete Chapters")
    }

    @Test
    fun removeAfterReadingToggleIsVisible() {
        assertTextDisplayed("Remove after reading")
    }

    @Test
    fun removeAfterMarkedAsReadToggleIsVisible() {
        assertTextDisplayed("Remove after marked as read")
    }

    @Test
    fun excludeCategoriesIsVisible() {
        assertTextDisplayed("Exclude Categories")
    }

    @Test
    fun fileFormatSectionIsVisible() {
        assertTextDisplayed("File Format")
    }

    @Test
    fun saveChaptersAsCbzToggleIsVisible() {
        assertTextDisplayed("Save Chapters as CBZ")
    }

    @Test
    fun splitTallImagesToggleIsVisible() {
        assertTextDisplayed("Split Tall Images")
    }

    @Test
    fun advancedSectionIsVisible() {
        assertTextDisplayed("Advanced")
    }

    @Test
    fun downloadQueueIsVisible() {
        assertTextDisplayed("Download Queue")
    }

    @Test
    fun clearDownloadCacheIsVisible() {
        assertTextDisplayed("Clear Download Cache")
    }

    @Test
    fun toggleDownloadOnlyOverWifi() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Download only over WiFi")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleDownloadNewChapters() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Download new chapters")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleAutoDeleteChapters() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Auto Delete Chapters")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleSaveChaptersAsCbz() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Save Chapters as CBZ")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleSplitTallImages() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Split Tall Images")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun openDownloadLocationDialog() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Download Location")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        assertTextDisplayed("Choose Folder")
    }

    @Test
    fun openClearCacheDialog() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Clear Download Cache")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        assertTextDisplayed("Clear")
    }

    @Test
    fun backNavigationReturnsToSettings() {
        navigateBack()
        assertTextDisplayed("Settings")
    }
}
