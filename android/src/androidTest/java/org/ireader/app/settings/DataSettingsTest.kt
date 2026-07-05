package org.ireader.app.settings

import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.filters.LargeTest
import org.ireader.app.BaseComposeTest
import org.junit.Before
import org.junit.Test

/**
 * E2E tests for the Data & Storage Settings screen.
 *
 * Screen: SettingsDataScreen.kt
 * Route: (accessed via settings main screen "Data & Storage" item)
 * ViewModel: SettingsDataViewModel
 *
 * UI elements tested:
 * - Storage usage section: total cache size, image/chapter/network cache
 * - Cache management: auto cleanup toggle, cleanup interval, max cache size,
 *   clear cache on low storage toggle
 * - Image settings: compress images toggle, image quality
 * - Preloading: preload next/previous chapter toggles
 * - Data usage: data usage statistics, network settings navigation
 * - Maintenance: clear all cache, optimize database, reset data settings
 * - Various AlertDialogs for selection and confirmation
 */
@LargeTest
class DataSettingsTest : BaseComposeTest() {

    @Before
    fun navigateToDataSettings() {
        navigateToSettings()
        navigateToSubScreen("Data & Storage")
    }

    @Test
    fun dataSettingsScreenDisplaysTitle() {
        assertTextDisplayed("Data & Storage")
    }

    @Test
    fun storageUsageSectionIsVisible() {
        assertTextDisplayed("Storage Usage")
    }

    @Test
    fun totalCacheSizeIsVisible() {
        assertTextDisplayed("Total Cache Size")
    }

    @Test
    fun imageCacheIsVisible() {
        assertTextDisplayed("Image Cache")
    }

    @Test
    fun chapterCacheIsVisible() {
        assertTextDisplayed("Chapter Cache")
    }

    @Test
    fun networkCacheIsVisible() {
        assertTextDisplayed("Network Cache")
    }

    @Test
    fun cacheManagementSectionIsVisible() {
        assertTextDisplayed("Cache Management")
    }

    @Test
    fun autoCleanupToggleIsVisible() {
        assertTextDisplayed("Auto Cleanup")
    }

    @Test
    fun cleanupIntervalIsVisible() {
        assertTextDisplayed("Cleanup Interval")
    }

    @Test
    fun maxCacheSizeIsVisible() {
        assertTextDisplayed("Max Cache Size")
    }

    @Test
    fun clearCacheOnLowStorageToggleIsVisible() {
        assertTextDisplayed("Clear Cache on Low Storage")
    }

    @Test
    fun imageSettingsSectionIsVisible() {
        assertTextDisplayed("Image Settings")
    }

    @Test
    fun compressImagesToggleIsVisible() {
        assertTextDisplayed("Compress Images")
    }

    @Test
    fun imageQualityIsVisible() {
        assertTextDisplayed("Image Quality")
    }

    @Test
    fun preloadingSectionIsVisible() {
        assertTextDisplayed("Preloading")
    }

    @Test
    fun preloadNextChapterToggleIsVisible() {
        assertTextDisplayed("Preload Next Chapter")
    }

    @Test
    fun preloadPreviousChapterToggleIsVisible() {
        assertTextDisplayed("Preload Previous Chapter")
    }

    @Test
    fun dataUsageSectionIsVisible() {
        assertTextDisplayed("Data Usage")
    }

    @Test
    fun dataUsageStatisticsIsVisible() {
        assertTextDisplayed("Data Usage Statistics")
    }

    @Test
    fun networkSettingsIsVisible() {
        assertTextDisplayed("Network Settings")
    }

    @Test
    fun maintenanceSectionIsVisible() {
        assertTextDisplayed("Maintenance")
    }

    @Test
    fun clearAllCacheIsVisible() {
        assertTextDisplayed("Clear All Cache")
    }

    @Test
    fun optimizeDatabaseIsVisible() {
        assertTextDisplayed("Optimize Database")
    }

    @Test
    fun resetDataSettingsIsVisible() {
        assertTextDisplayed("Reset Data Settings")
    }

    @Test
    fun toggleAutoCleanup() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Auto Cleanup")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleClearCacheOnLowStorage() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Clear Cache on Low Storage")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleCompressImages() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Compress Images")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun togglePreloadNextChapter() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Preload Next Chapter")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun togglePreloadPreviousChapter() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Preload Previous Chapter")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun cleanupIntervalDialogOpensAndCloses() {
        // First enable auto cleanup
        toggleAutoCleanup()
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Cleanup Interval")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        assertTextDisplayed("Daily")
        assertTextDisplayed("Weekly")
        assertTextDisplayed("Monthly")
        clickOnText("OK")
    }

    @Test
    fun maxCacheSizeDialogOpensAndCloses() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Max Cache Size")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        assertTextDisplayed("100 MB")
        assertTextDisplayed("1 GB")
        assertTextDisplayed("Unlimited")
        clickOnText("OK")
    }

    @Test
    fun backNavigationReturnsToSettings() {
        navigateBack()
        assertTextDisplayed("Settings")
    }
}
