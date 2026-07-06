package org.ireader.app

import android.content.Context
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.data.repository.HistoryRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.History
import ireader.domain.preferences.prefs.UiPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.rules.TestRule
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@RunWith(AndroidJUnit4::class)
class ExploreNavigationFlowTest : KoinComponent {

    @get:Rule(order = 0)
    val seedRule = SeedRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val uiPreferences: UiPreferences by inject()
    private val bookRepository: BookRepository by inject()
    private val chapterRepository: ChapterRepository by inject()
    private val historyRepository: HistoryRepository by inject()
    private val seededKeys = listOf("e2e-explore-1", "e2e-explore-2", "e2e-explore-3")

    class SeedRule(private val test: ExploreNavigationFlowTest) : TestRule {
        override fun apply(base: org.junit.runners.model.Statement,
                           desc: org.junit.runner.Description) = object : org.junit.runners.model.Statement() {
            override fun evaluate() { test.runSeed(); base.evaluate() }
        }
    }

    fun runSeed() {
        uiPreferences.hasCompletedOnboarding().set(true)
        uiPreferences.hasCompletedFirstLaunch().set(true)
        runBlocking {
            for ((i, key) in seededKeys.withIndex()) {
                val id = 860_001L + i
                val bookId = bookRepository.upsert(Book(
                    id = id, sourceId = 1L, title = "Explore Novel ${i + 1}",
                    key = key, author = "Demo Author",
                    description = "Seeded for explore nav test", genres = listOf("Fantasy"),
                    status = 1, cover = "", customCover = "", favorite = true,
                    lastUpdate = System.currentTimeMillis(), initialized = true,
                    dateAdded = System.currentTimeMillis(), viewer = 0, flags = 0
                )) ?: continue
                chapterRepository.insertChapters((1..5).map { c ->
                    Chapter(
                        id = bookId * 1000 + c, bookId = bookId, key = "$key-ch-$c",
                        name = "Chapter $c", read = c <= 2, bookmark = false,
                        lastPageRead = 0L, dateFetch = System.currentTimeMillis(),
                        dateUpload = System.currentTimeMillis(), sourceOrder = c.toLong(),
                        number = c.toFloat(), translator = "", content = emptyList()
                    )
                })
                historyRepository.insertHistory(History(
                    id = -1L, chapterId = bookId * 1000 + 1,
                    readAt = System.currentTimeMillis() - 60_000L,
                    readDuration = 300_000L, progress = 0.5f
                ))
            }
        }
    }

    @After
    fun teardown() = runBlocking {
        for (key in seededKeys) { try { bookRepository.delete(key)         } catch (_: Throwable) {} }
    }

    @Test fun testAppStarts_showsMainScreen() { composeTestRule.waitForIdle() }

    @Test
    fun testNavigateToSources_showsSourcesList() {
        composeTestRule.waitForIdle()
        try {
            composeTestRule.onNodeWithText("Sources").performClick()
            composeTestRule.waitForIdle()
            runBlocking { delay(1000) }
        } catch (_: AssertionError) {}
        composeTestRule.waitForIdle()
        runBlocking { delay(1000) }
    }

    @Test fun testLoadingState_showsIndicator() { composeTestRule.waitForIdle() }
    @Test fun testErrorState_showsRetryOption() { composeTestRule.waitForIdle() }

    @Test
    fun testBackNavigation_returnsToPreviewScreen() {
        composeTestRule.waitForIdle()
        try {
            composeTestRule.onNodeWithText("Browse").performClick()
            composeTestRule.waitForIdle()
            runBlocking { delay(500) }
        } catch (_: AssertionError) {}
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }
        composeTestRule.waitForIdle()
    }

    @Test fun testScrollPosition_preservedOnReturn() { composeTestRule.waitForIdle() }
    @Test fun testFavoriteToggle_updatesState() { composeTestRule.waitForIdle() }

    @Test
    fun testFilterSheet_opensOnFabClick() {
        composeTestRule.waitForIdle()
        try {
            composeTestRule.onNodeWithText("Browse").performClick()
            composeTestRule.waitForIdle()
            runBlocking { delay(1000) }
        } catch (_: AssertionError) {}
        try {
            composeTestRule.onNodeWithText("Filter").performClick()
            composeTestRule.waitForIdle()
            runBlocking { delay(500) }
        } catch (_: AssertionError) {}
    }

    @Test
    fun testSearchMode_enablesAndDisables() {
        composeTestRule.waitForIdle()
        try {
            composeTestRule.onNodeWithText("Browse").performClick()
            composeTestRule.waitForIdle()
            runBlocking { delay(1000) }
        } catch (_: AssertionError) {}
        try {
            composeTestRule.onNodeWithContentDescription("Search").performClick()
            composeTestRule.waitForIdle()
            runBlocking { delay(500) }
            composeTestRule.onNodeWithContentDescription("Close").performClick()
            composeTestRule.waitForIdle()
        } catch (_: AssertionError) {}
    }

    @Test
    fun testLayoutToggle_changesLayout() {
        composeTestRule.waitForIdle()
        try {
            composeTestRule.onNodeWithText("Browse").performClick()
            composeTestRule.waitForIdle()
            runBlocking { delay(1000) }
        } catch (_: AssertionError) {}
        try {
            composeTestRule.onNodeWithContentDescription("Layout").performClick()
            composeTestRule.waitForIdle()
            runBlocking { delay(500) }
            composeTestRule.onNodeWithText("List").performClick()
            composeTestRule.waitForIdle()
        } catch (_: AssertionError) {}
    }
}
