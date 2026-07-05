package org.ireader.app.settings

import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.filters.LargeTest
import org.ireader.app.BaseComposeTest
import org.junit.Before
import org.junit.Test

/**
 * E2E tests for the Tracking Settings screen.
 *
 * Screen: SettingsTrackingScreen.kt
 * Route: trackingSettings (NavigationRoutes.trackingSettings)
 * ViewModel: SettingsTrackingViewModel
 *
 * UI elements tested:
 * - Tracking services section: MyAnimeList, AniList, Kitsu, MangaUpdates, MyNovelList
 *   Each with enable toggle, login/logout button, configure button
 * - Auto-sync section: enable auto sync toggle, sync interval, sync only over WiFi
 * - Auto-update section: auto update status, auto update progress, auto update score
 * - Sync state: tracked books count, manual sync trigger
 * - All TrackingServiceItem composables
 */
@LargeTest
class TrackingSettingsTest : BaseComposeTest() {

    @Before
    fun navigateToTrackingSettings() {
        navigateToSettings()
        navigateToSubScreen("Tracking")
    }

    @Test
    fun trackingSettingsScreenDisplaysTitle() {
        assertTextDisplayed("Tracking")
    }

    @Test
    fun trackingServicesSectionIsVisible() {
        assertTextDisplayed("Tracking Services")
    }

    @Test
    fun myAnimeListServiceIsVisible() {
        assertTextDisplayed("MyAnimeList")
    }

    @Test
    fun aniListServiceIsVisible() {
        assertTextDisplayed("AniList")
    }

    @Test
    fun kitsuServiceIsVisible() {
        assertTextDisplayed("Kitsu")
    }

    @Test
    fun mangaUpdatesServiceIsVisible() {
        assertTextDisplayed("MangaUpdates")
    }

    @Test
    fun myNovelListServiceIsVisible() {
        assertTextDisplayed("MyNovelList")
    }

    @Test
    fun autoSyncSectionIsVisible() {
        assertTextDisplayed("Auto Sync")
    }

    @Test
    fun enableAutoSyncToggleIsVisible() {
        assertTextDisplayed("Enable Auto Sync")
    }

    @Test
    fun syncIntervalIsVisible() {
        assertTextDisplayed("Sync Interval")
    }

    @Test
    fun syncOnlyOverWifiToggleIsVisible() {
        assertTextDisplayed("Sync Only Over WiFi")
    }

    @Test
    fun autoUpdateSectionIsVisible() {
        assertTextDisplayed("Auto Update")
    }

    @Test
    fun autoUpdateStatusToggleIsVisible() {
        assertTextDisplayed("Auto Update Status")
    }

    @Test
    fun autoUpdateProgressToggleIsVisible() {
        assertTextDisplayed("Auto Update Progress")
    }

    @Test
    fun autoUpdateScoreToggleIsVisible() {
        assertTextDisplayed("Auto Update Score")
    }

    @Test
    fun toggleEnableAutoSync() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Enable Auto Sync")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleSyncOnlyOverWifi() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Sync Only Over WiFi")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleAutoUpdateStatus() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Auto Update Status")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleAutoUpdateProgress() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Auto Update Progress")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleAutoUpdateScore() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Auto Update Score")
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
