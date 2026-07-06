package org.ireader.app

import android.annotation.SuppressLint
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import ireader.domain.data.repository.BookCategoryRepository
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.CategoryRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.data.repository.HistoryRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.BookCategory
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.History
import ireader.domain.preferences.prefs.UiPreferences
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Base class for all Compose instrumented tests.
 *
 * Seeds DB & sets prefs BEFORE the Activity launches, so the library VM
 * sees the data on its first load.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
abstract class BaseComposeTest : KoinComponent {

    /**
     * SeedRule runs its @Before BEFORE the Activity launches.
     * createAndroidComposeRule uses lazy launch — Activity only starts
     * when the first test method accesses composeTestRule.
     */
    @get:Rule(order = 0)
    val seedRule = SeedRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val uiPreferences: UiPreferences by inject()
    private val bookRepository: BookRepository by inject()
    private val chapterRepository: ChapterRepository by inject()
    private val historyRepository: HistoryRepository by inject()
    private val categoryRepository: CategoryRepository by inject()
    private val bookCategoryRepository: BookCategoryRepository by inject()

    private val seededBookKeys = mutableListOf<String>()

    protected val waitTimeoutMs = 15_000L

    /** Runs seed + prefs BEFORE Activity launch */
    class SeedRule(private val test: BaseComposeTest) : TestRule {
        override fun apply(base: org.junit.runners.model.Statement,
                           description: org.junit.runner.Description): org.junit.runners.model.Statement {
            return object : org.junit.runners.model.Statement() {
                override fun evaluate() {
                    test.runSeed()
                    base.evaluate()
                }
            }
        }
    }

    fun runSeed() {
        // Bypass onboarding
        uiPreferences.hasCompletedOnboarding().set(true)
        uiPreferences.hasCompletedFirstLaunch().set(true)
        // Seed DB
        seedDemoData()
    }

    @Before
    fun waitForDataToLoad() {
        // SeedRule ran before Activity launch, so data is in DB.
        // But the Library VM may need time to pick it up via Flow.
        // Wait for the app to show the Library tab (indicates activity is ready).
        composeTestRule.waitUntil(timeoutMillis = 15_000L) {
            try { composeTestRule.onNodeWithText("Library").assertExists(); true }
            catch (_: Exception) { false }
        }
    }

    @After
    fun cleanupSeededData() = runBlocking {
        for (key in seededBookKeys) {
            try { bookRepository.delete(key) } catch (_: Exception) {}
        }
    }

    private fun seedDemoData() = runBlocking {
        // System categories (ALL_ID=0L must exist for books to show in Library)
        try { categoryRepository.insert(Category(id = Category.ALL_ID, name = "All", order = 0L, flags = 0L)) }
        catch (_: Exception) {}
        try { categoryRepository.insert(Category(id = Category.UNCATEGORIZED_ID, name = "Uncategorized", order = 0L, flags = 0L)) }
        catch (_: Exception) {}
        try { categoryRepository.insert(Category(id = 1L, name = "Reading", order = 0L, flags = 0L)) }
        catch (_: Exception) {}

        // 5 favorite + 2 non-favorite books with chapters + history
        data class DemoBook(val id: Long, val title: String, val key: String, val favorite: Boolean)
        val demoBooks = listOf(
            DemoBook(900_001L, "E2E Demo Book", "e2e-demo-1", true),
            DemoBook(900_002L, "Shadow Monarch", "e2e-demo-2", true),
            DemoBook(900_003L, "The Beginning After The End", "e2e-demo-3", true),
            DemoBook(900_004L, "Omniscient Reader", "e2e-demo-4", true),
            DemoBook(900_005L, "Solo Leveling", "e2e-demo-5", true),
            DemoBook(900_006L, "Martial Peak", "e2e-demo-6", false),
            DemoBook(900_007L, "Cultivation Chat Group", "e2e-demo-7", false),
        )
        seededBookKeys.addAll(demoBooks.map { it.key })

        for (demo in demoBooks) {
            val bookId = bookRepository.upsert(Book(
                id = demo.id, sourceId = 1L, title = demo.title, key = demo.key,
                author = "Demo Author", description = "Demo book: ${demo.title}",
                genres = listOf("Fantasy", "Action"), status = 1,
                cover = "", customCover = "", favorite = demo.favorite,
                lastUpdate = System.currentTimeMillis(), initialized = true,
                dateAdded = System.currentTimeMillis(), viewer = 0, flags = 0
            )) ?: continue

            // Assign book to categories so Library screen shows it
            try { bookCategoryRepository.insert(BookCategory(bookId = bookId, categoryId = Category.ALL_ID)) }
            catch (_: Exception) {}
            try { bookCategoryRepository.insert(BookCategory(bookId = bookId, categoryId = 1L)) }
            catch (_: Exception) {}

            val chapters = (1..5).map { i ->
                Chapter(
                    id = bookId * 1000 + i, bookId = bookId,
                    key = "${demo.key}-ch-$i", name = "Chapter $i",
                    read = i <= 2, bookmark = i == 1, lastPageRead = 0L,
                    dateFetch = System.currentTimeMillis(), dateUpload = System.currentTimeMillis(),
                    sourceOrder = i.toLong(), number = i.toFloat(), translator = "", content = emptyList()
                )
            }
            chapterRepository.insertChapters(chapters)

            if (demo.favorite) {
                historyRepository.insertHistory(History(
                    id = -1L, chapterId = bookId * 1000 + 1,
                    readAt = System.currentTimeMillis() - 60_000L,
                    readDuration = 300_000L, progress = 0.5f
                ))
            }
        }
    }

    protected fun navigateToSettings() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try { composeTestRule.onNodeWithText("Settings").performClick(); true }
            catch (_: AssertionError) { false }
        }
    }

    protected fun navigateToSubScreen(title: String) {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try { composeTestRule.onNodeWithText(title).performScrollTo().performClick(); true }
            catch (_: AssertionError) { false }
        }
    }

    protected fun navigateBack() {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try { composeTestRule.onNodeWithText("Navigate up").performClick(); true }
            catch (_: AssertionError) {
                try { composeTestRule.activity.onBackPressedDispatcher.onBackPressed(); true }
                catch (_: Exception) { false }
            }
        }
    }

    protected fun assertTextDisplayed(text: String) {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try { composeTestRule.onNodeWithText(text).assertExists(); true }
            catch (_: AssertionError) { false }
        }
    }

    protected fun clickOnText(text: String) {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try { composeTestRule.onNodeWithText(text).performScrollTo().performClick(); true }
            catch (_: AssertionError) { false }
        }
    }

    @SuppressLint("VisibleForTests")
    protected fun waitForText(text: String) {
        composeTestRule.waitUntil(waitTimeoutMs) {
            try { composeTestRule.onNodeWithText(text).assertExists(); true }
            catch (_: AssertionError) { false }
        }
    }
}
