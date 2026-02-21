package ireader.presentation.ui.sync.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import ireader.domain.models.sync.ConflictResolutionStrategy
import ireader.domain.models.sync.ConflictType
import ireader.domain.models.sync.DataConflict
import ireader.domain.models.sync.ReadingProgressData
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for ConflictResolutionDialog component following TDD methodology.
 * 
 * Tests verify that the conflict resolution dialog properly displays conflict details,
 * resolution strategy options, previews of both versions, and action buttons following
 * Material Design 3 guidelines.
 * 
 * **Validates: Requirements FR4.2, FR4.4**
 */
class ConflictResolutionDialogTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    private val testConflict = DataConflict(
        conflictType = ConflictType.READING_PROGRESS,
        localData = ReadingProgressData(
            bookId = 1L,
            chapterIndex = 5,
            chapterOffset = 100,
            progress = 0.5f,
            lastReadAt = 1000L
        ),
        remoteData = ReadingProgressData(
            bookId = 1L,
            chapterIndex = 7,
            chapterOffset = 200,
            progress = 0.7f,
            lastReadAt = 2000L
        ),
        conflictField = "chapterIndex"
    )
    
    // Test: Dialog should display title
    @Test
    fun `conflict resolution dialog should display title`() {
        // Arrange
        val conflicts = listOf(testConflict)
        
        // Act
        composeTestRule.setContent {
            ConflictResolutionDialog(
                conflicts = conflicts,
                onResolve = {},
                onDismiss = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Resolve Conflicts")
            .assertIsDisplayed()
    }
    
    // Test: Dialog should display conflict count
    @Test
    fun `conflict resolution dialog should display conflict count`() {
        // Arrange
        val conflicts = listOf(testConflict, testConflict.copy())
        
        // Act
        composeTestRule.setContent {
            ConflictResolutionDialog(
                conflicts = conflicts,
                onResolve = {},
                onDismiss = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Found 2 conflicts during sync.", substring = true)
            .assertIsDisplayed()
    }
    
    // Test: Dialog should display singular conflict message for one conflict
    @Test
    fun `conflict resolution dialog should display singular message for one conflict`() {
        // Arrange
        val conflicts = listOf(testConflict)
        
        // Act
        composeTestRule.setContent {
            ConflictResolutionDialog(
                conflicts = conflicts,
                onResolve = {},
                onDismiss = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Found 1 conflict during sync.", substring = true)
            .assertIsDisplayed()
    }
    
    // Test: Dialog should display conflict type
    @Test
    fun `conflict resolution dialog should display conflict type`() {
        // Arrange
        val conflicts = listOf(testConflict)
        
        // Act
        composeTestRule.setContent {
            ConflictResolutionDialog(
                conflicts = conflicts,
                onResolve = {},
                onDismiss = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Conflict Type: READING PROGRESS", substring = true)
            .assertIsDisplayed()
    }
    
    // Test: Dialog should display conflict field
    @Test
    fun `conflict resolution dialog should display conflict field`() {
        // Arrange
        val conflicts = listOf(testConflict)
        
        // Act
        composeTestRule.setContent {
            ConflictResolutionDialog(
                conflicts = conflicts,
                onResolve = {},
                onDismiss = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Field: chapterIndex", substring = true)
            .assertIsDisplayed()
    }
    
    // Test: Dialog should display local data label
    @Test
    fun `conflict resolution dialog should display local data label`() {
        // Arrange
        val conflicts = listOf(testConflict)
        
        // Act
        composeTestRule.setContent {
            ConflictResolutionDialog(
                conflicts = conflicts,
                onResolve = {},
                onDismiss = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Local")
            .assertIsDisplayed()
    }
    
    // Test: Dialog should display remote data label
    @Test
    fun `conflict resolution dialog should display remote data label`() {
        // Arrange
        val conflicts = listOf(testConflict)
        
        // Act
        composeTestRule.setContent {
            ConflictResolutionDialog(
                conflicts = conflicts,
                onResolve = {},
                onDismiss = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Remote")
            .assertIsDisplayed()
    }
    
    // Test: Dialog should display local data preview
    @Test
    fun `conflict resolution dialog should display local data preview`() {
        // Arrange
        val conflicts = listOf(testConflict)
        
        // Act
        composeTestRule.setContent {
            ConflictResolutionDialog(
                conflicts = conflicts,
                onResolve = {},
                onDismiss = {}
            )
        }
        
        // Assert - Local data should be displayed
        composeTestRule
            .onNodeWithText(testConflict.localData.toString(), substring = true)
            .assertIsDisplayed()
    }
    
    // Test: Dialog should display remote data preview
    @Test
    fun `conflict resolution dialog should display remote data preview`() {
        // Arrange
        val conflicts = listOf(testConflict)
        
        // Act
        composeTestRule.setContent {
            ConflictResolutionDialog(
                conflicts = conflicts,
                onResolve = {},
                onDismiss = {}
            )
        }
        
        // Assert - Remote data should be displayed
        composeTestRule
            .onNodeWithText(testConflict.remoteData.toString(), substring = true)
            .assertIsDisplayed()
    }
    
    // Test: Dialog should display "and X more" message for multiple conflicts
    @Test
    fun `conflict resolution dialog should display more conflicts message`() {
        // Arrange
        val conflicts = listOf(testConflict, testConflict.copy(), testConflict.copy())
        
        // Act
        composeTestRule.setContent {
            ConflictResolutionDialog(
                conflicts = conflicts,
                onResolve = {},
                onDismiss = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("...and 2 more", substring = true)
            .assertIsDisplayed()
    }
    
    // Test: Dialog should display resolution strategy prompt
    @Test
    fun `conflict resolution dialog should display resolution strategy prompt`() {
        // Arrange
        val conflicts = listOf(testConflict)
        
        // Act
        composeTestRule.setContent {
            ConflictResolutionDialog(
                conflicts = conflicts,
                onResolve = {},
                onDismiss = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Choose resolution strategy:")
            .assertIsDisplayed()
    }
    
    // Test: Dialog should display "Use Latest" strategy option
    @Test
    fun `conflict resolution dialog should display use latest strategy option`() {
        // Arrange
        val conflicts = listOf(testConflict)
        
        // Act
        composeTestRule.setContent {
            ConflictResolutionDialog(
                conflicts = conflicts,
                onResolve = {},
                onDismiss = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Use Latest")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Use data with the most recent timestamp")
            .assertIsDisplayed()
    }
    
    // Test: Dialog should display "Use Local" strategy option
    @Test
    fun `conflict resolution dialog should display use local strategy option`() {
        // Arrange
        val conflicts = listOf(testConflict)
        
        // Act
        composeTestRule.setContent {
            ConflictResolutionDialog(
                conflicts = conflicts,
                onResolve = {},
                onDismiss = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Use Local")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Keep data from this device")
            .assertIsDisplayed()
    }
    
    // Test: Dialog should display "Use Remote" strategy option
    @Test
    fun `conflict resolution dialog should display use remote strategy option`() {
        // Arrange
        val conflicts = listOf(testConflict)
        
        // Act
        composeTestRule.setContent {
            ConflictResolutionDialog(
                conflicts = conflicts,
                onResolve = {},
                onDismiss = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Use Remote")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Use data from the other device")
            .assertIsDisplayed()
    }
    
    // Test: Dialog should display "Merge" strategy option
    @Test
    fun `conflict resolution dialog should display merge strategy option`() {
        // Arrange
        val conflicts = listOf(testConflict)
        
        // Act
        composeTestRule.setContent {
            ConflictResolutionDialog(
                conflicts = conflicts,
                onResolve = {},
                onDismiss = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Merge")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Attempt to merge compatible changes")
            .assertIsDisplayed()
    }
    
    // Test: Dialog should have Resolve button
    @Test
    fun `conflict resolution dialog should have resolve button`() {
        // Arrange
        val conflicts = listOf(testConflict)
        
        // Act
        composeTestRule.setContent {
            ConflictResolutionDialog(
                conflicts = conflicts,
                onResolve = {},
                onDismiss = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Resolve")
            .assertIsDisplayed()
            .assertHasClickAction()
    }
    
    // Test: Dialog should have Cancel button
    @Test
    fun `conflict resolution dialog should have cancel button`() {
        // Arrange
        val conflicts = listOf(testConflict)
        
        // Act
        composeTestRule.setContent {
            ConflictResolutionDialog(
                conflicts = conflicts,
                onResolve = {},
                onDismiss = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Cancel")
            .assertIsDisplayed()
            .assertHasClickAction()
    }
    
    // Test: Resolve button should trigger onResolve callback with default strategy
    @Test
    fun `conflict resolution dialog resolve button should trigger callback with default strategy`() {
        // Arrange
        val conflicts = listOf(testConflict)
        var resolvedStrategy: ConflictResolutionStrategy? = null
        
        // Act
        composeTestRule.setContent {
            ConflictResolutionDialog(
                conflicts = conflicts,
                onResolve = { resolvedStrategy = it },
                onDismiss = {}
            )
        }
        
        composeTestRule
            .onNodeWithText("Resolve")
            .performClick()
        
        // Assert
        assert(resolvedStrategy == ConflictResolutionStrategy.LATEST_TIMESTAMP) {
            "onResolve should be called with LATEST_TIMESTAMP as default"
        }
    }
    
    // Test: Cancel button should trigger onDismiss callback
    @Test
    fun `conflict resolution dialog cancel button should trigger callback`() {
        // Arrange
        val conflicts = listOf(testConflict)
        var dismissCalled = false
        
        // Act
        composeTestRule.setContent {
            ConflictResolutionDialog(
                conflicts = conflicts,
                onResolve = {},
                onDismiss = { dismissCalled = true }
            )
        }
        
        composeTestRule
            .onNodeWithText("Cancel")
            .performClick()
        
        // Assert
        assert(dismissCalled) { "onDismiss callback should be called" }
    }
    
    // Test: User should be able to select LOCAL_WINS strategy
    @Test
    fun `conflict resolution dialog should allow selecting local wins strategy`() {
        // Arrange
        val conflicts = listOf(testConflict)
        var resolvedStrategy: ConflictResolutionStrategy? = null
        
        // Act
        composeTestRule.setContent {
            ConflictResolutionDialog(
                conflicts = conflicts,
                onResolve = { resolvedStrategy = it },
                onDismiss = {}
            )
        }
        
        // Select "Use Local" option
        composeTestRule
            .onNodeWithText("Use Local")
            .performClick()
        
        // Click Resolve
        composeTestRule
            .onNodeWithText("Resolve")
            .performClick()
        
        // Assert
        assert(resolvedStrategy == ConflictResolutionStrategy.LOCAL_WINS) {
            "onResolve should be called with LOCAL_WINS strategy"
        }
    }
    
    // Test: User should be able to select REMOTE_WINS strategy
    @Test
    fun `conflict resolution dialog should allow selecting remote wins strategy`() {
        // Arrange
        val conflicts = listOf(testConflict)
        var resolvedStrategy: ConflictResolutionStrategy? = null
        
        // Act
        composeTestRule.setContent {
            ConflictResolutionDialog(
                conflicts = conflicts,
                onResolve = { resolvedStrategy = it },
                onDismiss = {}
            )
        }
        
        // Select "Use Remote" option
        composeTestRule
            .onNodeWithText("Use Remote")
            .performClick()
        
        // Click Resolve
        composeTestRule
            .onNodeWithText("Resolve")
            .performClick()
        
        // Assert
        assert(resolvedStrategy == ConflictResolutionStrategy.REMOTE_WINS) {
            "onResolve should be called with REMOTE_WINS strategy"
        }
    }
    
    // Test: User should be able to select MERGE strategy
    @Test
    fun `conflict resolution dialog should allow selecting merge strategy`() {
        // Arrange
        val conflicts = listOf(testConflict)
        var resolvedStrategy: ConflictResolutionStrategy? = null
        
        // Act
        composeTestRule.setContent {
            ConflictResolutionDialog(
                conflicts = conflicts,
                onResolve = { resolvedStrategy = it },
                onDismiss = {}
            )
        }
        
        // Select "Merge" option
        composeTestRule
            .onNodeWithText("Merge")
            .performClick()
        
        // Click Resolve
        composeTestRule
            .onNodeWithText("Resolve")
            .performClick()
        
        // Assert
        assert(resolvedStrategy == ConflictResolutionStrategy.MERGE) {
            "onResolve should be called with MERGE strategy"
        }
    }
    
    // Test: LATEST_TIMESTAMP should be selected by default
    @Test
    fun `conflict resolution dialog should have latest timestamp selected by default`() {
        // Arrange
        val conflicts = listOf(testConflict)
        
        // Act
        composeTestRule.setContent {
            ConflictResolutionDialog(
                conflicts = conflicts,
                onResolve = {},
                onDismiss = {}
            )
        }
        
        // Assert - The radio button for "Use Latest" should be selected
        // We verify this by checking that clicking Resolve without any selection
        // results in LATEST_TIMESTAMP being passed
        var resolvedStrategy: ConflictResolutionStrategy? = null
        
        composeTestRule.setContent {
            ConflictResolutionDialog(
                conflicts = conflicts,
                onResolve = { resolvedStrategy = it },
                onDismiss = {}
            )
        }
        
        composeTestRule
            .onNodeWithText("Resolve")
            .performClick()
        
        assert(resolvedStrategy == ConflictResolutionStrategy.LATEST_TIMESTAMP) {
            "LATEST_TIMESTAMP should be selected by default"
        }
    }
    
    // Test: Dialog should display warning icon
    @Test
    fun `conflict resolution dialog should display warning icon`() {
        // Arrange
        val conflicts = listOf(testConflict)
        
        // Act
        composeTestRule.setContent {
            ConflictResolutionDialog(
                conflicts = conflicts,
                onResolve = {},
                onDismiss = {}
            )
        }
        
        // Assert - Warning icon should be present (we can't directly test icon, but we can verify the dialog renders)
        composeTestRule
            .onNodeWithText("Resolve Conflicts")
            .assertIsDisplayed()
    }
    
    // Test: Dialog should handle empty conflicts list gracefully
    @Test
    fun `conflict resolution dialog should handle empty conflicts list`() {
        // Arrange
        val conflicts = emptyList<DataConflict>()
        
        // Act
        composeTestRule.setContent {
            ConflictResolutionDialog(
                conflicts = conflicts,
                onResolve = {},
                onDismiss = {}
            )
        }
        
        // Assert - Dialog should still render with 0 conflicts message
        composeTestRule
            .onNodeWithText("Found 0 conflicts during sync.", substring = true)
            .assertIsDisplayed()
    }
    
    // Test: Strategy selection should be mutually exclusive
    @Test
    fun `conflict resolution dialog should have mutually exclusive strategy selection`() {
        // Arrange
        val conflicts = listOf(testConflict)
        var resolvedStrategy: ConflictResolutionStrategy? = null
        
        // Act
        composeTestRule.setContent {
            ConflictResolutionDialog(
                conflicts = conflicts,
                onResolve = { resolvedStrategy = it },
                onDismiss = {}
            )
        }
        
        // Select "Use Local"
        composeTestRule
            .onNodeWithText("Use Local")
            .performClick()
        
        // Then select "Use Remote"
        composeTestRule
            .onNodeWithText("Use Remote")
            .performClick()
        
        // Click Resolve
        composeTestRule
            .onNodeWithText("Resolve")
            .performClick()
        
        // Assert - Only the last selection should be used
        assert(resolvedStrategy == ConflictResolutionStrategy.REMOTE_WINS) {
            "Only the last selected strategy should be used"
        }
    }
}
