package org.ireader.app.settings

import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.filters.LargeTest
import org.ireader.app.BaseComposeTest
import org.junit.Before
import org.junit.Test

/**
 * E2E tests for the Security & Privacy Settings screen.
 *
 * Screen: SettingsSecurityScreen.kt
 * Route: securitySettings
 * ViewModel: SettingsSecurityViewModel
 *
 * UI elements tested:
 * - App Lock section: enable app lock toggle, lock method (PIN/Password/Pattern/Biometric),
 *   biometric authentication toggle, lock after inactivity
 * - Screen Security section: secure screen mode (Always/Incognito Only/Never)
 * - Privacy section: hide notification content toggle, incognito mode toggle
 * - Content Restrictions section: adult content lock toggle
 * - Advanced Security section: clear authentication data, security audit navigation
 * - Various AlertDialogs for lock method, inactivity timeout, secure screen mode
 * - Clear auth data confirmation dialog
 */
@LargeTest
class SecuritySettingsTest : BaseComposeTest() {

    @Before
    fun navigateToSecuritySettings() {
        navigateToSettings()
        navigateToSubScreen("Security & Privacy")
    }

    @Test
    fun securitySettingsScreenDisplaysTitle() {
        assertTextDisplayed("Security & Privacy")
    }

    @Test
    fun appLockSectionIsVisible() {
        assertTextDisplayed("App Lock")
    }

    @Test
    fun enableAppLockToggleIsVisible() {
        assertTextDisplayed("Enable App Lock")
    }

    @Test
    fun lockMethodIsVisible() {
        assertTextDisplayed("Lock Method")
    }

    @Test
    fun biometricAuthenticationToggleIsVisible() {
        assertTextDisplayed("Biometric Authentication")
    }

    @Test
    fun lockAfterInactivityIsVisible() {
        assertTextDisplayed("Lock After Inactivity")
    }

    @Test
    fun screenSecuritySectionIsVisible() {
        assertTextDisplayed("Screen Security")
    }

    @Test
    fun secureScreenIsVisible() {
        assertTextDisplayed("Secure Screen")
    }

    @Test
    fun privacySectionIsVisible() {
        assertTextDisplayed("Privacy")
    }

    @Test
    fun hideNotificationContentToggleIsVisible() {
        assertTextDisplayed("Hide Notification Content")
    }

    @Test
    fun incognitoModeToggleIsVisible() {
        assertTextDisplayed("Incognito Mode")
    }

    @Test
    fun contentRestrictionsSectionIsVisible() {
        assertTextDisplayed("Content Restrictions")
    }

    @Test
    fun adultContentLockToggleIsVisible() {
        assertTextDisplayed("Adult Content Lock")
    }

    @Test
    fun advancedSecuritySectionIsVisible() {
        assertTextDisplayed("Advanced Security")
    }

    @Test
    fun clearAuthenticationDataIsVisible() {
        assertTextDisplayed("Clear Authentication Data")
    }

    @Test
    fun securityAuditIsVisible() {
        assertTextDisplayed("Security Audit")
    }

    @Test
    fun toggleAppLock() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Enable App Lock")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleHideNotificationContent() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Hide Notification Content")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleIncognitoMode() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Incognito Mode")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun toggleAdultContentLock() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Adult Content Lock")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test
    fun lockMethodDialogOpensAndCloses() {
        // First enable app lock
        toggleAppLock()
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Lock Method")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        // Dialog should show lock method options
        assertTextDisplayed("PIN")
        assertTextDisplayed("Password")
        assertTextDisplayed("Pattern")
        clickOnText("OK")
    }

    @Test
    fun secureScreenDialogOpensAndCloses() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Secure Screen")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        // Dialog should show secure screen mode options
        assertTextDisplayed("Always")
        assertTextDisplayed("Incognito Only")
        assertTextDisplayed("Never")
        clickOnText("OK")
    }

    @Test
    fun clearAuthDataDialogOpensAndCloses() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Clear Authentication Data")
                    .performScrollTo()
                    .performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
        // Confirmation dialog should appear
        assertTextDisplayed("Clear")
        assertTextDisplayed("Cancel")
    }

    @Test
    fun backNavigationReturnsToSettings() {
        navigateBack()
        assertTextDisplayed("Settings")
    }
}
