package ireader.presentation.ui.accessibility

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithRole
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.semantics.Role
import ireader.domain.models.entities.Book
import ireader.presentation.ui.component.enhanced.AccessibleBookListItem
import ireader.presentation.ui.component.list.PerformantBookList
import ireader.presentation.ui.accessibility.AccessibilityTestUtils.assertAccessibleButton
import ireader.presentation.ui.accessibility.AccessibilityTestUtils.assertMinimumTouchTargetSize
import ireader.presentation.ui.accessibility.AccessibilityTestUtils.runAccessibilityTests
import org.junit.Rule
import org.junit.Test

/**
 * Comprehensive accessibility tests for book list components
 * Tests following Mihon's accessibility patterns
 */
class BookListAccessibilityTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    private val testBooks = listOf(
        Book(
            id = 1,
            title = "Test Book 1",
            author = "Test Author 1",
            description = "Test description 1",
            favorite = true,
            status = 1L,
            cover = "",
            key = "test1",
            sourceId = 1L,
            dateAdded = 0L,
            lastUpdate = 0L,
            initialized = true,
            flags = 0L,
            viewer = 0L,
            genres = emptyList(),
            isPinned = false,
            pinnedOrder = 0,
            isArchived = false
        ),
        Book(
            id = 2,
            title = "Test Book 2",
            author = "Test Author 2",
            description = "Test description 2",
            favorite = false,
            status = 2L,
            cover = "",
            key = "test2",
            sourceId = 1L,
            dateAdded = 0L,
            lastUpdate = 0L,
            initialized = true,
            flags = 0L,
            viewer = 0L,
            genres = emptyList(),
            isPinned = false,
            pinnedOrder = 0,
            isArchived = false
        )
    )
    
    @Test
    fun testBookListItemAccessibility() {
        var clickedBook: Book? = null
        
        composeTestRule.setContent {
            AccessibleBookListItem(
                book = testBooks[0],
                onClick = { clickedBook = testBooks[0] },
                showAuthor = true,
                showDescription = true
            )
        }
        
        // Test content description
        composeTestRule
            .onNodeWithContentDescription("Book: Test Book 1, by Test Author 1, favorited, status: ongoing series")
            .assertExists()
            .assertAccessibleButton("Book: Test Book 1, by Test Author 1, favorited, status: ongoing series")
            .assertMinimumTouchTargetSize()
        
        // Test click functionality
        composeTestRule
            .onNodeWithContentDescription("Book: Test Book 1, by Test Author 1, favorited, status: ongoing series")
            .performClick()
        
        assert(clickedBook == testBooks[0])
    }
    
    @Test
    fun testBookListAccessibility() {
        val clickedBooks = mutableListOf<Book>()
        
        composeTestRule.setContent {
            PerformantBookList(
                books = testBooks,
                onBookClick = { book -> clickedBooks.add(book) },
                showAuthor = true,
                showDescription = false
            )
        }
        
        // Run comprehensive accessibility tests
        composeTestRule.runAccessibilityTests("BookList") { rule ->
            // Test that all books are accessible
            rule.onAllNodesWithRole(Role.Button)
                .fetchSemanticsNodes()
                .forEachIndexed { index, _ ->
                    rule.onAllNodesWithRole(Role.Button)[index]
                        .assertMinimumTouchTargetSize()
                }
            
            // Test that images have proper content descriptions
            rule.onAllNodesWithRole(Role.Image)
                .fetchSemanticsNodes()
                .forEach { node ->
                    // Each image should have a content description
                    assert(node.config.contains(androidx.compose.ui.semantics.SemanticsProperties.ContentDescription))
                }
        }
    }
    
    @Test
    fun testBookListItemWithoutAuthor() {
        val bookWithoutAuthor = testBooks[0].copy(author = "")
        
        composeTestRule.setContent {
            AccessibleBookListItem(
                book = bookWithoutAuthor,
                onClick = { },
                showAuthor = true
            )
        }
        
        // Should not include author in content description when author is empty
        composeTestRule
            .onNodeWithContentDescription("Book: Test Book 1, favorited, status: ongoing series")
            .assertExists()
    }
    
    @Test
    fun testBookListItemWithoutFavorite() {
        val bookNotFavorited = testBooks[0].copy(favorite = false)
        
        composeTestRule.setContent {
            AccessibleBookListItem(
                book = bookNotFavorited,
                onClick = { },
                showAuthor = true
            )
        }
        
        // Should not include "favorited" in content description
        composeTestRule
            .onNodeWithContentDescription("Book: Test Book 1, by Test Author 1, status: ongoing series")
            .assertExists()
    }
    
    @Test
    fun testBookListItemMinimumTouchTarget() {
        composeTestRule.setContent {
            AccessibleBookListItem(
                book = testBooks[0],
                onClick = { }
            )
        }
        
        // Test minimum touch target size (48dp)
        composeTestRule
            .onAllNodesWithRole(Role.Button)[0]
            .assertMinimumTouchTargetSize()
    }
    
    @Test
    fun testBookListScrollAccessibility() {
        // Test with a larger list to ensure scroll accessibility
        val largeBookList = (1..50).map { index ->
            testBooks[0].copy(
                id = index.toLong(),
                title = "Test Book $index"
            )
        }
        
        composeTestRule.setContent {
            PerformantBookList(
                books = largeBookList,
                onBookClick = { }
            )
        }
        
        // Verify that the list is accessible for screen readers
        composeTestRule.runAccessibilityTests("LargeBookList") { rule ->
            // Should have multiple accessible book items
            val buttonNodes = rule.onAllNodesWithRole(Role.Button).fetchSemanticsNodes()
            assert(buttonNodes.isNotEmpty()) { "Book list should contain accessible button nodes" }
            
            // Each button should have proper accessibility attributes
            buttonNodes.forEach { node ->
                assert(node.config.contains(androidx.compose.ui.semantics.SemanticsProperties.ContentDescription)) {
                    "Each book item should have a content description"
                }
                assert(node.config.contains(androidx.compose.ui.semantics.SemanticsProperties.Role)) {
                    "Each book item should have a semantic role"
                }
            }
        }
    }
}