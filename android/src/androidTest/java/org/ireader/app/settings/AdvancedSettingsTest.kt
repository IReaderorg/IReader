package org.ireader.app.settings

import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.filters.LargeTest
import org.ireader.app.BaseComposeTest
import org.junit.Before
import org.junit.Test

/**
 * E2E tests for the Advanced Settings screen.
 *
 * Screen: AdvanceSettings.kt
 * Route: advanceSettings (NavigationRoutes.advanceSettings)
 * ViewModel: AdvanceSettingViewModel
 *
 * UI elements tested:
 * - Cache Management section: clear all cache, clear all cover cache
 * - EPUB section: import epub
 * - PDF section: import pdf
 * - Database Maintenance section: repair database, repair categories
 * - Danger Zone section: clear not in library books, clear all chapters,
 *   clear all database, delete all database, reset reader settings,
 *   reset themes, reset categories
 * - Various confirmation AlertDialogs with text input verification
 *
 * Note: Danger zone operations require typing "CONFIRM" to proceed.
 * These tests only verify dialog opening, not actual destructive operations.
 */
@LargeTest
class AdvancedSettingsTest : BaseComposeTest() {

    @Before
    fun navigateToAdvancedSettings() {
        navigateToSettings()
        navigateToSubScreen("Advanced")
    }

    @Test
    fun advancedSettingsScreenDisplaysTitle() {
        assertTextDisplayed("Advanced")
    }

    @Test
    fun cacheManagementSectionIsVisible() {
        assertTextDisplayed("Cache Management")
    }

    @Test
    fun clearAllCacheIsVisible() {
        assertTextDisplayed("Clear All Cache")
    }

    @Test
    fun clearAllCoverCacheIsVisible() {
        assertTextDisplayed("Clear All Cover Cache")
    }

    @Test
    fun epubSectionIsVisible() {
        assertTextDisplayed("EPUB")
    }

    @Test
    fun importEpubIsVisible() {
        assertTextDisplayed("Import EPUB")
    }

    @Test
    fun databaseMaintenanceSectionIsVisible() {
        assertTextDisplayed("Database Maintenance")
    }

    @Test
    fun repairDatabaseIsVisible() {
        assertTextDisplayed("Repair Database")
    }

    @Test
    fun repairCategoriesIsVisible() {
        assertTextDisplayed("Repair Categories")
    }

    @Test
    fun dangerZoneSectionIsVisible() {
        assertTextDisplayed("Danger Zone")
    }

    @Test
    fun clearNotInLibraryBooksIsVisible() {
        assertTextDisplayed("Clear not in library books")
    }

    @Test
    fun clearAllChaptersIsVisible() {
        assertTextDisplayed("Clear All Chapters")
    }

    @Test
    fun clearAllDatabaseIsVisible() {
        assertTextDisplayed("Clear All Database")
    }

    @Test
    fun deleteAllDatabaseIsVisible() {
        assertTextDisplayed("Delete All Database")
    }

    @Test
    fun resetReaderSettingsIsVisible() {
        assertTextDisplayed("Reset Reader Settings")
    }

    @Test
    fun resetThemesIsVisible() {
        assertTextDisplayed("Reset Themes")
    }

    @Test
    fun resetCategoriesIsVisible() {
        assertTextDisplayed("Reset Categories")
    }

    @Test
    fun openClearCacheDialog() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Clear All Cache", substring = true)
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        // Confirmation dialog should appear
        assertTextDisplayed("Confirm")
    }

    @Test
    fun openClearCoverCacheDialog() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Clear All Cover Cache", substring = true)
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        // Confirmation dialog should appear
        assertTextDisplayed("Confirm")
    }

    @Test
    fun openClearNotInLibraryDialog() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Clear not in library books")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        // Confirmation dialog should appear
        assertTextDisplayed("Confirm")
    }

    @Test
    fun openResetReaderSettingsDialog() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Reset Reader Settings")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        // Confirmation dialog should appear
        assertTextDisplayed("Confirm")
    }

    @Test
    fun backNavigationReturnsToSettings() {
        navigateBack()
        assertTextDisplayed("Settings")
    }
}
