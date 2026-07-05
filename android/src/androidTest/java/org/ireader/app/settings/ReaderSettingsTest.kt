package org.ireader.app.settings

import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.filters.LargeTest
import org.ireader.app.BaseComposeTest
import org.junit.Before
import org.junit.Test

/**
 * E2E tests for the Reader Settings screen.
 *
 * Screen: SettingsReaderScreen.kt
 * Route: readerSettings
 * ViewModel: SettingsReaderViewModel
 *
 * UI elements tested:
 * - Reading mode section (webtoon/continuous_vertical with AlertDialog)
 * - Page transitions section (slide/fade/none with AlertDialog)
 * - Display settings: double tap zoom, show page number, fullscreen,
 *   keep screen on, show status bar, show navigation bar
 * - Orientation & layout: cutout area behavior, landscape zoom
 * - Navigation controls: navigation mode (tap/swipe/both with AlertDialog),
 *   volume key navigation, invert tapping
 * - Visual effects: flash on page change
 * - Advanced: color filters, image scaling, tap zones
 *
 * All switches use SettingsSwitchItem composable.
 * Dialog-based selections use AlertDialog with RadioButtons.
 */
@LargeTest
class ReaderSettingsTest : BaseComposeTest() {

    @Before
    fun navigateToReaderSettings() {
        navigateToSettings()
        navigateToSubScreen("Reader")
    }

    @Test
    fun readerSettingsScreenDisplaysTitle() {
        assertTextDisplayed("Reader")
    }

    @Test
    fun readingModeSectionIsVisible() {
        assertTextDisplayed("Reading Mode")
    }

    @Test
    fun defaultReadingModeIsVisible() {
        assertTextDisplayed("Default Reading Mode")
    }

    @Test
    fun pageTransitionsIsVisible() {
        assertTextDisplayed("Page Transitions")
    }

    @Test
    fun displaySettingsSectionIsVisible() {
        assertTextDisplayed("Display Settings")
    }

    @Test
    fun doubleTapToZoomIsVisible() {
        assertTextDisplayed("Double Tap to Zoom")
    }

    @Test
    fun showPageNumberIsVisible() {
        assertTextDisplayed("Show Page Number")
    }

    @Test
    fun fullscreenIsVisible() {
        assertTextDisplayed("Fullscreen")
    }

    @Test
    fun keepScreenOnIsVisible() {
        assertTextDisplayed("Keep Screen On")
    }

    @Test
    fun showStatusBarIsVisible() {
        assertTextDisplayed("Show Status Bar")
    }

    @Test
    fun showNavigationBarIsVisible() {
        assertTextDisplayed("Show Navigation Bar")
    }

    @Test
    fun orientationLayoutSectionIsVisible() {
        assertTextDisplayed("Orientation & Layout")
    }

    @Test
    fun cutoutAreaBehaviorIsVisible() {
        assertTextDisplayed("Cutout Area Behavior")
    }

    @Test
    fun landscapeZoomIsVisible() {
        assertTextDisplayed("Landscape Zoom")
    }

    @Test
    fun navigationControlsSectionIsVisible() {
        assertTextDisplayed("Navigation Controls")
    }

    @Test
    fun navigationModeIsVisible() {
        assertTextDisplayed("Navigation Mode")
    }

    @Test
    fun volumeKeyNavigationIsVisible() {
        assertTextDisplayed("Volume Key Navigation")
    }

    @Test
    fun invertTappingIsVisible() {
        assertTextDisplayed("Invert Tapping")
    }

    @Test
    fun visualEffectsSectionIsVisible() {
        assertTextDisplayed("Visual Effects")
    }

    @Test
    fun flashOnPageChangeIsVisible() {
        assertTextDisplayed("Flash on Page Change")
    }

    @Test
    fun advancedSettingsSectionIsVisible() {
        assertTextDisplayed("Advanced Settings")
    }

    @Test
    fun colorFiltersIsVisible() {
        assertTextDisplayed("Color Filters")
    }

    @Test
    fun imageScalingIsVisible() {
        assertTextDisplayed("Image Scaling")
    }

    @Test
    fun tapZonesIsVisible() {
        assertTextDisplayed("Tap Zones")
    }

    @Test
    fun toggleDoubleTapZoom() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Double Tap to Zoom")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleShowPageNumber() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Show Page Number")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleFullscreen() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Fullscreen")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleKeepScreenOn() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Keep Screen On")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleVolumeKeyNavigation() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Volume Key Navigation")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleInvertTapping() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Invert Tapping")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleFlashOnPageChange() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Flash on Page Change")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun readingModeDialogOpensAndCloses() {
        // Click on reading mode item to open dialog
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Default Reading Mode")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        // Dialog should show reading mode options
        assertTextDisplayed("Webtoon")
        assertTextDisplayed("Continuous Vertical")
        // Dismiss dialog
        clickOnText("OK")
    }

    @Test
    fun pageTransitionsDialogOpensAndCloses() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Page Transitions")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        // Dialog should show transition options
        assertTextDisplayed("Slide")
        assertTextDisplayed("Fade")
        assertTextDisplayed("None")
        clickOnText("OK")
    }

    @Test
    fun navigationModeDialogOpensAndCloses() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Navigation Mode")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        // Dialog should show navigation mode options
        assertTextDisplayed("Tap Zones")
        assertTextDisplayed("Swipe")
        assertTextDisplayed("Both")
        clickOnText("OK")
    }

    @Test
    fun backNavigationReturnsToSettings() {
        navigateBack()
        assertTextDisplayed("Settings")
    }
}
