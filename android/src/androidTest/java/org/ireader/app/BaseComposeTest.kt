package org.ireader.app

import android.annotation.SuppressLint
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.runner.RunWith

/**
 * Base class for all Compose instrumented tests.
 * Provides the compose test rule, navigation helpers, and common assertions.
 *
 * Uses [MainActivity] as the entry point for E2E tests.
 * All settings screens are reached via navigation from the main screen.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
abstract class BaseComposeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    /**
     * Wait timeout for async UI operations (ms).
     * Increase if tests flake on slow devices.
     */
    protected val waitTimeoutMs = 30_000L

    /**
     * Navigate to the main Settings screen from the bottom navigation.
     * The Settings tab is the last tab in the bottom bar.
     */
    protected fun navigateToSettings() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Settings").performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    /**
     * Navigate from the main Settings screen to a sub-screen by clicking
     * the item with the given [title].
     */
    protected fun navigateToSubScreen(title: String) {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText(title).performScrollTo().performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    /**
     * Press the back button / navigate up from a sub-screen.
     * Uses the toolbar back arrow content description.
     */
    protected fun navigateBack() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText("Navigate up").performClick()
                true
            } catch (_: AssertionError) {
                try {
                    composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
                    true
                } catch (_: Exception) {
                    false
                }
            }
        }
    }

    /**
     * Assert that a node with the given [text] is displayed.
     */
    protected fun assertTextDisplayed(text: String) {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText(text).assertExists()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    /**
     * Click a node with the given [text].
     */
    protected fun clickOnText(text: String) {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText(text).performScrollTo().performClick()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @SuppressLint("VisibleForTests")
    protected fun waitForText(text: String) {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                composeTestRule.onNodeWithText(text).assertExists()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }
}
