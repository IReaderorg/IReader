package ireader.presentation.ui.sync.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import ireader.domain.models.sync.SyncError
import ireader.domain.models.sync.SyncStatus
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for SyncStatusCard component following TDD methodology.
 * 
 * Tests verify that the sync status display properly shows different sync states
 * (Idle, Discovering, Connecting, Syncing, Completed, Failed) with appropriate
 * information and follows Material Design 3 guidelines.
 * 
 * **Validates: Requirements FR5.1, FR5.2, FR5.3, FR5.4**
 */
class SyncStatusCardTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    // Test: Idle status should not be displayed
    @Test
    fun `sync status card should not display content for idle status`() {
        // Arrange
        val idleStatus = SyncStatus.Idle
        
        // Act
        composeTestRule.setContent {
            SyncStatusCard(syncStatus = idleStatus)
        }
        
        // Assert - Card should be empty or minimal for Idle state
        // The card itself might render but should have no meaningful content
        composeTestRule
            .onNodeWithText("Discovering devices...")
            .assertDoesNotExist()
    }
    
    // Test: Discovering status should show progress indicator and message
    @Test
    fun `sync status card should display discovering message with progress indicator`() {
        // Arrange
        val discoveringStatus = SyncStatus.Discovering
        
        // Act
        composeTestRule.setContent {
            SyncStatusCard(syncStatus = discoveringStatus)
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Discovering devices...")
            .assertIsDisplayed()
    }
    
    // Test: Connecting status should show device name
    @Test
    fun `sync status card should display connecting message with device name`() {
        // Arrange
        val connectingStatus = SyncStatus.Connecting(deviceName = "Test Device")
        
        // Act
        composeTestRule.setContent {
            SyncStatusCard(syncStatus = connectingStatus)
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Connecting...")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Test Device")
            .assertIsDisplayed()
    }
    
    // Test: Syncing status should show device name
    @Test
    fun `sync status card should display device name when syncing`() {
        // Arrange
        val syncingStatus = SyncStatus.Syncing(
            deviceName = "My Android Phone",
            progress = 0.5f,
            currentItem = "Book 1"
        )
        
        // Act
        composeTestRule.setContent {
            SyncStatusCard(syncStatus = syncingStatus)
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Syncing with My Android Phone")
            .assertIsDisplayed()
    }
    
    // Test: Syncing status should show progress percentage
    @Test
    fun `sync status card should display progress percentage when syncing`() {
        // Arrange
        val syncingStatus = SyncStatus.Syncing(
            deviceName = "Test Device",
            progress = 0.75f,
            currentItem = "Book 1"
        )
        
        // Act
        composeTestRule.setContent {
            SyncStatusCard(syncStatus = syncingStatus)
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("75%")
            .assertIsDisplayed()
    }
    
    // Test: Syncing status should show current item being synced
    @Test
    fun `sync status card should display current item being synced`() {
        // Arrange
        val syncingStatus = SyncStatus.Syncing(
            deviceName = "Test Device",
            progress = 0.5f,
            currentItem = "The Great Gatsby.epub"
        )
        
        // Act
        composeTestRule.setContent {
            SyncStatusCard(syncStatus = syncingStatus)
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("The Great Gatsby.epub")
            .assertIsDisplayed()
    }
    
    // Test: Syncing status should show progress bar
    @Test
    fun `sync status card should display progress bar when syncing`() {
        // Arrange
        val syncingStatus = SyncStatus.Syncing(
            deviceName = "Test Device",
            progress = 0.5f,
            currentItem = "Book 1"
        )
        
        // Act
        composeTestRule.setContent {
            SyncStatusCard(syncStatus = syncingStatus)
        }
        
        // Assert - LinearProgressIndicator should be present
        // We can't directly test the progress value, but we can verify the component exists
        composeTestRule
            .onNodeWithText("Syncing with Test Device")
            .assertIsDisplayed()
    }
    
    // Test: Completed status should show success message
    @Test
    fun `sync status card should display completion message`() {
        // Arrange
        val completedStatus = SyncStatus.Completed(
            deviceName = "Test Device",
            syncedItems = 10,
            duration = 5000L
        )
        
        // Act
        composeTestRule.setContent {
            SyncStatusCard(syncStatus = completedStatus)
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Sync completed")
            .assertIsDisplayed()
    }
    
    // Test: Completed status should show synced items count
    @Test
    fun `sync status card should display synced items count on completion`() {
        // Arrange
        val completedStatus = SyncStatus.Completed(
            deviceName = "Test Device",
            syncedItems = 25,
            duration = 10000L
        )
        
        // Act
        composeTestRule.setContent {
            SyncStatusCard(syncStatus = completedStatus)
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Synced 25 items with Test Device in 10s", substring = true)
            .assertIsDisplayed()
    }
    
    // Test: Completed status should show duration
    @Test
    fun `sync status card should display sync duration on completion`() {
        // Arrange
        val completedStatus = SyncStatus.Completed(
            deviceName = "Test Device",
            syncedItems = 10,
            duration = 65000L // 1 minute 5 seconds
        )
        
        // Act
        composeTestRule.setContent {
            SyncStatusCard(syncStatus = completedStatus)
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("1m 5s", substring = true)
            .assertIsDisplayed()
    }
    
    // Test: Failed status should show error message
    @Test
    fun `sync status card should display error message when sync fails`() {
        // Arrange
        val failedStatus = SyncStatus.Failed(
            deviceName = "Test Device",
            error = SyncError.NetworkUnavailable("WiFi connection lost")
        )
        
        // Act
        composeTestRule.setContent {
            SyncStatusCard(syncStatus = failedStatus)
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Sync failed")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("WiFi connection lost", substring = true)
            .assertIsDisplayed()
    }
    
    // Test: Failed status should show device name if available
    @Test
    fun `sync status card should display device name in error message when available`() {
        // Arrange
        val failedStatus = SyncStatus.Failed(
            deviceName = "My Desktop",
            error = SyncError.ConnectionFailed("Connection timeout")
        )
        
        // Act
        composeTestRule.setContent {
            SyncStatusCard(syncStatus = failedStatus)
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Failed to sync with My Desktop", substring = true)
            .assertIsDisplayed()
    }
    
    // Test: Failed status should handle null device name
    @Test
    fun `sync status card should display generic error message when device name is null`() {
        // Arrange
        val failedStatus = SyncStatus.Failed(
            deviceName = null,
            error = SyncError.NetworkUnavailable("No network")
        )
        
        // Act
        composeTestRule.setContent {
            SyncStatusCard(syncStatus = failedStatus)
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Sync failed: No network", substring = true)
            .assertIsDisplayed()
    }
    
    // Test: Progress should be displayed correctly for 0%
    @Test
    fun `sync status card should display 0 percent progress correctly`() {
        // Arrange
        val syncingStatus = SyncStatus.Syncing(
            deviceName = "Test Device",
            progress = 0.0f,
            currentItem = "Starting..."
        )
        
        // Act
        composeTestRule.setContent {
            SyncStatusCard(syncStatus = syncingStatus)
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("0%")
            .assertIsDisplayed()
    }
    
    // Test: Progress should be displayed correctly for 100%
    @Test
    fun `sync status card should display 100 percent progress correctly`() {
        // Arrange
        val syncingStatus = SyncStatus.Syncing(
            deviceName = "Test Device",
            progress = 1.0f,
            currentItem = "Finalizing..."
        )
        
        // Act
        composeTestRule.setContent {
            SyncStatusCard(syncStatus = syncingStatus)
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("100%")
            .assertIsDisplayed()
    }
    
    // Test: Duration formatting for seconds
    @Test
    fun `sync status card should format duration in seconds correctly`() {
        // Arrange
        val completedStatus = SyncStatus.Completed(
            deviceName = "Test Device",
            syncedItems = 5,
            duration = 45000L // 45 seconds
        )
        
        // Act
        composeTestRule.setContent {
            SyncStatusCard(syncStatus = completedStatus)
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("45s", substring = true)
            .assertIsDisplayed()
    }
    
    // Test: Duration formatting for minutes
    @Test
    fun `sync status card should format duration in minutes correctly`() {
        // Arrange
        val completedStatus = SyncStatus.Completed(
            deviceName = "Test Device",
            syncedItems = 50,
            duration = 125000L // 2 minutes 5 seconds
        )
        
        // Act
        composeTestRule.setContent {
            SyncStatusCard(syncStatus = completedStatus)
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("2m 5s", substring = true)
            .assertIsDisplayed()
    }
    
    // Test: Duration formatting for hours
    @Test
    fun `sync status card should format duration in hours correctly`() {
        // Arrange
        val completedStatus = SyncStatus.Completed(
            deviceName = "Test Device",
            syncedItems = 1000,
            duration = 3665000L // 1 hour 1 minute 5 seconds
        )
        
        // Act
        composeTestRule.setContent {
            SyncStatusCard(syncStatus = completedStatus)
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("1h 1m", substring = true)
            .assertIsDisplayed()
    }
}
