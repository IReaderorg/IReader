package org.ireader.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.data.repository.HistoryRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.History
import ireader.domain.preferences.prefs.UiPreferences
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
class SmokeTest : KoinComponent {

    @get:Rule(order = 0)
    val seedRule = SmokeTest.SeedRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val uiPreferences: UiPreferences by inject()
    private val bookRepository: BookRepository by inject()
    private val chapterRepository: ChapterRepository by inject()
    private val historyRepository: HistoryRepository by inject()

    private data class SeedBook(val id: Long, val title: String, val key: String, val favorite: Boolean)
    private val seededBooks = listOf(
        SeedBook(800_001L, "Smoke Test Novel", "e2e-smoke-1", true),
        SeedBook(800_002L, "Second Novel", "e2e-smoke-2", true),
        SeedBook(800_003L, "Third Novel", "e2e-smoke-3", true),
    )

    class SeedRule(private val test: SmokeTest) : TestRule {
        override fun apply(base: org.junit.runners.model.Statement,
                           desc: org.junit.runner.Description) = object : org.junit.runners.model.Statement() {
            override fun evaluate() { test.runSeed(); base.evaluate() }
        }
    }

    fun runSeed() {
        uiPreferences.hasCompletedOnboarding().set(true)
        uiPreferences.hasCompletedFirstLaunch().set(true)
        seedDemoData()
    }

    @After
    fun cleanup() = runBlocking {
        for (b in seededBooks) { try { bookRepository.delete(b.key) } catch (_: Exception) {} }
    }

    @Test
    fun app_launches_successfully() {
        composeTestRule.waitForIdle()
    }

    private fun seedDemoData() = runBlocking {
        for (b in seededBooks) {
            val bookId = bookRepository.upsert(Book(
                id = b.id, sourceId = 1L, title = b.title, key = b.key,
                author = "Demo Author", description = "Seeded for smoke test",
                genres = listOf("Fantasy"), status = 1, cover = "", customCover = "",
                favorite = b.favorite, lastUpdate = System.currentTimeMillis(), initialized = true,
                dateAdded = System.currentTimeMillis(), viewer = 0, flags = 0
            )) ?: continue
            chapterRepository.insertChapters((1..5).map { i ->
                Chapter(
                    id = bookId * 1000 + i, bookId = bookId, key = "${b.key}-ch-$i",
                    name = "Chapter $i", read = i <= 2, bookmark = false,
                    lastPageRead = 0L, dateFetch = System.currentTimeMillis(),
                    dateUpload = System.currentTimeMillis(), sourceOrder = i.toLong(),
                    number = i.toFloat(), translator = "", content = emptyList()
                )
            })
            if (b.favorite) {
                historyRepository.insertHistory(History(
                    id = -1L, chapterId = bookId * 1000 + 1,
                    readAt = System.currentTimeMillis() - 60_000L,
                    readDuration = 300_000L, progress = 0.5f
                ))
            }
        }
    }
}
