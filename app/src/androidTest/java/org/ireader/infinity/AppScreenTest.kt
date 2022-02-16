package org.ireader.infinity

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.ireader.infinity.di.AppModule
import org.ireader.infinity.di.LocalModule
import org.ireader.infinity.di.NetworkModule
import org.ireader.infinity.presentation.MainActivity
import org.ireader.presentation.ui.ReaderScreenSpec
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@UninstallModules(AppModule::class, LocalModule::class, NetworkModule::class)
class AppScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
        composeRule.setContent {
            val navController = rememberNavController()
            navController.navigate(
                ReaderScreenSpec.buildRoute(
                    bookId = 0,
                    chapterId = 0,
                    sourceId = 0
                )
            )
        }
    }

    @Test
    fun clickOnLastBook() {
        composeRule.onNodeWithContentDescription("WebView").assertExists()
    }


}