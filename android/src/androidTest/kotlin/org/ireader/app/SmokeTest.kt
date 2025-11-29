package org.ireader.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmokeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun app_launches_successfully() {
        // Wait for the app to settle
        composeTestRule.waitForIdle()
        
        // This test verifies that the app launches successfully and reaches an idle state.
        // You can extend this test to interact with the UI, for example:
        // composeTestRule.onNodeWithText("Library").assertExists()
    }
}
