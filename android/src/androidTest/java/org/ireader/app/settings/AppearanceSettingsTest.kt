package org.ireader.app.settings

import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.filters.LargeTest
import org.ireader.app.BaseComposeTest
import org.junit.Before
import org.junit.Test

/**
 * E2E tests for the Appearance Settings screen.
 *
 * Screen: AppearanceSettingScreen.kt
 * Route: appearance
 * ViewModel: AppearanceViewModel
 *
 * UI elements tested:
 * - Dynamic color mode toggle (Material You)
 * - Use true black / AMOLED toggle
 * - Font customization section (app UI font ChoicePreference)
 * - Light themes section with LazyRow of ThemePreviewCards
 * - Dark themes section with LazyRow of ThemePreviewCards
 * - Color customization section (primary, secondary, bars ColorPreference)
 * - Theme mode selection (Light/Dark/System)
 */
@LargeTest
class AppearanceSettingsTest : BaseComposeTest() {

    @Before
    fun navigateToAppearanceSettings() {
        navigateToSettings()
        navigateToSubScreen("Appearance")
    }

    @Test
    fun appearanceSettingsScreenDisplaysTitle() {
        assertTextDisplayed("Appearance")
    }

    @Test
    fun dynamicColorsToggleIsVisible() {
        assertTextDisplayed("Material You Dynamic Colors")
    }

    @Test
    fun trueBlackToggleIsVisible() {
        assertTextDisplayed("Use True Black (AMOLED)")
    }

    @Test
    fun fontCustomizationSectionIsVisible() {
        assertTextDisplayed("Font Customization")
    }

    @Test
    fun appUiFontPreferenceIsVisible() {
        assertTextDisplayed("App UI Font")
    }

    @Test
    fun lightThemesSectionIsVisible() {
        assertTextDisplayed("Light Themes")
    }

    @Test
    fun darkThemesSectionIsVisible() {
        assertTextDisplayed("Dark Themes")
    }

    @Test
    fun colorCustomizationSectionIsVisible() {
        assertTextDisplayed("Color Customization")
    }

    @Test
    fun primaryColorPreferenceIsVisible() {
        assertTextDisplayed("Color Primary")
    }

    @Test
    fun secondaryColorPreferenceIsVisible() {
        assertTextDisplayed("Color Secondary")
    }

    @Test
    fun barsColorPreferenceIsVisible() {
        assertTextDisplayed("Color Toolbar")
    }

    @Test
    fun toggleDynamicColors() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Material You Dynamic Colors")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleTrueBlack() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Use True Black (AMOLED)")
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
