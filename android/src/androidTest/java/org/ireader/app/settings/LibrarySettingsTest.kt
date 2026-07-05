package org.ireader.app.settings

import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.filters.LargeTest
import org.ireader.app.BaseComposeTest
import org.junit.Before
import org.junit.Test

/**
 * E2E tests for the Library Settings screen.
 *
 * Screen: SettingsLibraryScreen.kt
 * Route: library (via settings)
 * ViewModel: SettingsLibraryViewModel
 *
 * UI elements tested:
 * - Display section: default sort, sort direction, continue reading button, default category
 * - Badges section: unread badge, download badge, language badge, local badge
 * - Auto update section: auto update library toggle, update interval, update restrictions
 * - Update filters section: update only completed, update only non-completed,
 *   skip titles without chapters, refresh covers too
 * - Advanced section: category management
 *
 * Dialogs tested:
 * - Default sort dialog (RadioButton selection)
 * - Update interval dialog (RadioButton selection)
 * - Update restrictions dialog (Checkbox selection)
 */
@LargeTest
class LibrarySettingsTest : BaseComposeTest() {

    @Before
    fun navigateToLibrarySettings() {
        navigateToSettings()
        navigateToSubScreen("Library")
    }

    @Test
    fun librarySettingsScreenDisplaysTitle() {
        assertTextDisplayed("Library")
    }

    @Test
    fun displaySectionIsVisible() {
        assertTextDisplayed("Display")
    }

    @Test
    fun defaultSortIsVisible() {
        assertTextDisplayed("Default Sort")
    }

    @Test
    fun sortDirectionIsVisible() {
        assertTextDisplayed("Sort Direction")
    }

    @Test
    fun continueReadingButtonIsVisible() {
        assertTextDisplayed("Continue Reading Button")
    }

    @Test
    fun defaultCategoryIsVisible() {
        assertTextDisplayed("Default Category")
    }

    @Test
    fun badgesSectionIsVisible() {
        assertTextDisplayed("Badges")
    }

    @Test
    fun unreadBadgeIsVisible() {
        assertTextDisplayed("Unread Badge")
    }

    @Test
    fun downloadBadgeIsVisible() {
        assertTextDisplayed("Download Badge")
    }

    @Test
    fun languageBadgeIsVisible() {
        assertTextDisplayed("Language Badge")
    }

    @Test
    fun localBadgeIsVisible() {
        assertTextDisplayed("Local Badge")
    }

    @Test
    fun autoUpdateSectionIsVisible() {
        assertTextDisplayed("Auto Update")
    }

    @Test
    fun autoUpdateLibraryIsVisible() {
        assertTextDisplayed("Auto Update Library")
    }

    @Test
    fun updateIntervalIsVisible() {
        assertTextDisplayed("Update Interval")
    }

    @Test
    fun updateRestrictionsIsVisible() {
        assertTextDisplayed("Update Restrictions")
    }

    @Test
    fun updateFiltersSectionIsVisible() {
        assertTextDisplayed("Update Filters")
    }

    @Test
    fun updateOnlyCompletedIsVisible() {
        assertTextDisplayed("Update Only Completed")
    }

    @Test
    fun updateOnlyNonCompletedIsVisible() {
        assertTextDisplayed("Update Only Non-Completed")
    }

    @Test
    fun skipTitlesWithoutChaptersIsVisible() {
        assertTextDisplayed("Skip Titles Without Chapters")
    }

    @Test
    fun refreshCoversTooIsVisible() {
        assertTextDisplayed("Refresh Covers Too")
    }

    @Test
    fun advancedSectionIsVisible() {
        assertTextDisplayed("Advanced")
    }

    @Test
    fun categoryManagementIsVisible() {
        assertTextDisplayed("Category Management")
    }

    @Test
    fun toggleContinueReadingButton() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Continue Reading Button")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleUnreadBadge() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Unread Badge")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleDownloadBadge() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Download Badge")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleAutoUpdateLibrary() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Auto Update Library")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleLanguageBadge() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Language Badge")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleLocalBadge() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Local Badge")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun defaultSortDialogOpensAndCloses() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Default Sort")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        // Dialog should show sort options
        assertTextDisplayed("Alphabetical")
        assertTextDisplayed("Last Read")
        assertTextDisplayed("Last Updated")
        assertTextDisplayed("Unread Count")
        assertTextDisplayed("Total Chapters")
        assertTextDisplayed("Date Added")
        clickOnText("OK")
    }

    @Test
    fun updateIntervalDialogOpensAndCloses() {
        // First enable auto update
        toggleAutoUpdateLibrary()
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Update Interval")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        // Dialog should show interval options
        assertTextDisplayed("1 hour")
        assertTextDisplayed("Daily")
        assertTextDisplayed("Weekly")
        clickOnText("OK")
    }

    @Test
    fun backNavigationReturnsToSettings() {
        navigateBack()
        assertTextDisplayed("Settings")
    }
}
