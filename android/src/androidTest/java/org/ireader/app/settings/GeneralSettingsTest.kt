package org.ireader.app.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.filters.LargeTest
import org.ireader.app.BaseComposeTest
import org.junit.Before
import org.junit.Test

/**
 * E2E tests for the General Settings screen.
 *
 * Screen: GeneralSettingScreen.kt
 * Route: generalSettings
 * ViewModel: GeneralSettingScreenViewModel
 *
 * UI elements tested:
 * - Language selector (ChoicePreference)
 * - Translation settings navigation
 * - App updater toggle (Switch)
 * - Show history toggle (Switch)
 * - Show update toggle (Switch)
 * - Confirm exit toggle (Switch)
 * - Smart categories toggle (Switch)
 * - Use FAB in library toggle (Switch)
 * - Default chapter sort (ChoicePreference)
 * - Download delay (Slider)
 * - Max performance mode (Switch)
 * - Thumbnail quality (ChoicePreference)
 * - Disable haptic feedback (Switch)
 * - Disable loading animations (Switch)
 * - Installer mode (ChoicePreference)
 * - Enable JS plugins (Switch)
 * - Auto installer (Switch)
 * - Show system/local catalogs (Switch)
 * - Prefer SAF storage (Switch)
 */
@LargeTest
class GeneralSettingsTest : BaseComposeTest() {

    @Before
    fun navigateToGeneralSettings() {
        navigateToSettings()
        navigateToSubScreen("General")
    }

    @Test
    fun generalSettingsScreenDisplaysTitle() {
        assertTextDisplayed("General")
    }

    @Test
    fun languageSectionIsVisible() {
        assertTextDisplayed("Language & Translation")
    }

    @Test
    fun languageSelectorIsVisible() {
        assertTextDisplayed("Languages")
    }

    @Test
    fun translationSettingsNavigationIsVisible() {
        assertTextDisplayed("Translation Settings")
    }

    @Test
    fun appUpdatesSectionIsVisible() {
        assertTextDisplayed("App Updates & Display")
    }

    @Test
    fun appUpdaterToggleIsVisible() {
        assertTextDisplayed("Updater is enable")
    }

    @Test
    fun showHistoryToggleIsVisible() {
        assertTextDisplayed("Show History")
    }

    @Test
    fun showUpdateToggleIsVisible() {
        assertTextDisplayed("Show Update")
    }

    @Test
    fun confirmExitToggleIsVisible() {
        assertTextDisplayed("Confirm Exit")
    }

    @Test
    fun librarySettingsSectionIsVisible() {
        assertTextDisplayed("Library Settings")
    }

    @Test
    fun smartCategoriesToggleIsVisible() {
        assertTextDisplayed("Show Smart Categories")
    }

    @Test
    fun useFabInLibraryToggleIsVisible() {
        assertTextDisplayed("Use FAB in Library")
    }

    @Test
    fun defaultChapterSortIsVisible() {
        assertTextDisplayed("Default Chapter Sort")
    }

    @Test
    fun autoDownloadSectionIsVisible() {
        assertTextDisplayed("Auto Download")
    }

    @Test
    fun downloadNewChaptersToggleIsVisible() {
        assertTextDisplayed("Download new chapters")
    }

    @Test
    fun userInterfaceSectionIsVisible() {
        assertTextDisplayed("User Interface")
    }

    @Test
    fun maxPerformanceModeToggleIsVisible() {
        assertTextDisplayed("Max Performance Mode")
    }

    @Test
    fun thumbnailQualityIsVisible() {
        assertTextDisplayed("Thumbnail Quality")
    }

    @Test
    fun disableHapticFeedbackToggleIsVisible() {
        assertTextDisplayed("Disable Haptic Feedback")
    }

    @Test
    fun disableLoadingAnimationsToggleIsVisible() {
        assertTextDisplayed("Disable Loading Animations")
    }

    @Test
    fun downloadDelaySliderIsVisible() {
        assertTextDisplayed("Download Delay")
    }

    @Test
    fun storageSettingsSectionIsVisible() {
        assertTextDisplayed("Storage Settings")
    }

    @Test
    fun preferSafStorageToggleIsVisible() {
        assertTextDisplayed("Prefer SAF Storage")
    }

    @Test
    fun catalogSettingsSectionIsVisible() {
        assertTextDisplayed("Catalog Settings")
    }

    @Test
    fun showSystemCatalogsToggleIsVisible() {
        assertTextDisplayed("Show System Catalogs")
    }

    @Test
    fun showLocalCatalogsToggleIsVisible() {
        assertTextDisplayed("Show Local Catalogs")
    }

    @Test
    fun jsPluginSettingsNavigationIsVisible() {
        assertTextDisplayed("JavaScript Plugin Settings")
    }

    @Test
    fun enableJsPluginsToggleIsVisible() {
        assertTextDisplayed("Enable JavaScript Plugins")
    }

    @Test
    fun autoInstallerToggleIsVisible() {
        assertTextDisplayed("Auto Installer")
    }

    @Test
    fun installerModeIsVisible() {
        assertTextDisplayed("Installer Mode")
    }

    @Test
    fun toggleAppUpdater() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Updater is enable")
                    .performScrollTo()
                    .assertIsDisplayed()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleShowHistory() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Show History")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleConfirmExit() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Confirm Exit")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleSmartCategories() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Show Smart Categories")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleMaxPerformanceMode() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Max Performance Mode")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleDisableHapticFeedback() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Disable Haptic Feedback")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleEnableJsPlugins() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Enable JavaScript Plugins")
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
