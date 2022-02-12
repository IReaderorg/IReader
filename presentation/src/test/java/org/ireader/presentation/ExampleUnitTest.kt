package org.ireader.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.accompanist.pager.ExperimentalPagerApi
import org.ireader.presentation.feature_library.presentation.LibraryScreen
import org.junit.Rule
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

class MyComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @OptIn(ExperimentalPagerApi::class, androidx.compose.animation.ExperimentalAnimationApi::class)
    @Test
    fun MyTest() {
        composeTestRule.setContent {
            LibraryScreen()

        }

        composeTestRule.onNodeWithText("Library").performClick()

        composeTestRule.onNodeWithText("Library").assertIsDisplayed()
    }
}