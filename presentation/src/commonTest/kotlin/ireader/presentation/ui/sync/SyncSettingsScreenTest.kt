package ireader.presentation.ui.sync

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import ireader.domain.models.sync.ConflictResolutionStrategy
import ireader.domain.models.sync.DeviceInfo
import ireader.domain.models.sync.DeviceType
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * UI tests for SyncSettingsScreen following TDD methodology.
 * 
 * Tests are written BEFORE implementation to ensure proper behavior.
 */
class SyncSettingsScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `settings screen should display title in top bar`() {
        // Arrange & Act
        composeTestRule.setContent {
            SyncSettingsScreen(
                conflictResolutionStrategy = ConflictResolutionStrategy.LATEST_TIMESTAMP,
                trustedDevices = emptyList(),
                syncOnChargerOnly = false,
                onConflictStrategyChange = {},
                onRemoveDevice = {},
                onSyncOnChargerOnlyChange = {},
                onNavigateBack = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Sync Settings")
            .assertIsDisplayed()
    }
    
    @Test
    fun `settings screen should display back button`() {
        // Arrange & Act
        composeTestRule.setContent {
            SyncSettingsScreen(
                conflictResolutionStrategy = ConflictResolutionStrategy.LATEST_TIMESTAMP,
                trustedDevices = emptyList(),
                syncOnChargerOnly = false,
                onConflictStrategyChange = {},
                onRemoveDevice = {},
                onSyncOnChargerOnlyChange = {},
                onNavigateBack = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithContentDescription("Navigate back")
            .assertIsDisplayed()
    }
    
    @Test
    fun `clicking back button should trigger navigation callback`() {
        // Arrange
        var navigatedBack = false
        
        // Act
        composeTestRule.setContent {
            SyncSettingsScreen(
                conflictResolutionStrategy = ConflictResolutionStrategy.LATEST_TIMESTAMP,
                trustedDevices = emptyList(),
                syncOnChargerOnly = false,
                onConflictStrategyChange = {},
                onRemoveDevice = {},
                onSyncOnChargerOnlyChange = {},
                onNavigateBack = { navigatedBack = true }
            )
        }
        
        composeTestRule
            .onNodeWithContentDescription("Navigate back")
            .performClick()
        
        // Assert
        assertTrue(navigatedBack)
    }
    
    @Test
    fun `settings screen should display conflict resolution section`() {
        // Arrange & Act
        composeTestRule.setContent {
            SyncSettingsScreen(
                conflictResolutionStrategy = ConflictResolutionStrategy.LATEST_TIMESTAMP,
                trustedDevices = emptyList(),
                syncOnChargerOnly = false,
                onConflictStrategyChange = {},
                onRemoveDevice = {},
                onSyncOnChargerOnlyChange = {},
                onNavigateBack = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Conflict Resolution")
            .assertIsDisplayed()
    }
    
    @Test
    fun `settings screen should display current conflict strategy`() {
        // Arrange & Act
        composeTestRule.setContent {
            SyncSettingsScreen(
                conflictResolutionStrategy = ConflictResolutionStrategy.LATEST_TIMESTAMP,
                trustedDevices = emptyList(),
                syncOnChargerOnly = false,
                onConflictStrategyChange = {},
                onRemoveDevice = {},
                onSyncOnChargerOnlyChange = {},
                onNavigateBack = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Use Latest Timestamp")
            .assertIsDisplayed()
    }
    
    @Test
    fun `clicking conflict strategy should show selection dialog`() {
        // Arrange & Act
        composeTestRule.setContent {
            SyncSettingsScreen(
                conflictResolutionStrategy = ConflictResolutionStrategy.LATEST_TIMESTAMP,
                trustedDevices = emptyList(),
                syncOnChargerOnly = false,
                onConflictStrategyChange = {},
                onRemoveDevice = {},
                onSyncOnChargerOnlyChange = {},
                onNavigateBack = {}
            )
        }
        
        composeTestRule
            .onNodeWithText("Use Latest Timestamp")
            .performClick()
        
        // Assert - Dialog should appear with all strategies
        composeTestRule
            .onNodeWithText("Select Conflict Resolution Strategy")
            .assertIsDisplayed()
    }
    
    @Test
    fun `settings screen should display trusted devices section`() {
        // Arrange & Act
        composeTestRule.setContent {
            SyncSettingsScreen(
                conflictResolutionStrategy = ConflictResolutionStrategy.LATEST_TIMESTAMP,
                trustedDevices = emptyList(),
                syncOnChargerOnly = false,
                onConflictStrategyChange = {},
                onRemoveDevice = {},
                onSyncOnChargerOnlyChange = {},
                onNavigateBack = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Trusted Devices")
            .assertIsDisplayed()
    }
    
    @Test
    fun `settings screen should display empty state when no trusted devices`() {
        // Arrange & Act
        composeTestRule.setContent {
            SyncSettingsScreen(
                conflictResolutionStrategy = ConflictResolutionStrategy.LATEST_TIMESTAMP,
                trustedDevices = emptyList(),
                syncOnChargerOnly = false,
                onConflictStrategyChange = {},
                onRemoveDevice = {},
                onSyncOnChargerOnlyChange = {},
                onNavigateBack = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("No trusted devices")
            .assertIsDisplayed()
    }
    
    @Test
    fun `settings screen should display trusted devices list`() {
        // Arrange
        val testDevice = DeviceInfo(
            deviceId = "test-id",
            deviceName = "Test Device",
            deviceType = DeviceType.ANDROID,
            appVersion = "1.0.0",
            ipAddress = "192.168.1.100",
            port = 8080,
            lastSeen = System.currentTimeMillis()
        )
        
        // Act
        composeTestRule.setContent {
            SyncSettingsScreen(
                conflictResolutionStrategy = ConflictResolutionStrategy.LATEST_TIMESTAMP,
                trustedDevices = listOf(testDevice),
                syncOnChargerOnly = false,
                onConflictStrategyChange = {},
                onRemoveDevice = {},
                onSyncOnChargerOnlyChange = {},
                onNavigateBack = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Test Device")
            .assertIsDisplayed()
    }
    
    @Test
    fun `trusted device item should display remove button`() {
        // Arrange
        val testDevice = DeviceInfo(
            deviceId = "test-id",
            deviceName = "Test Device",
            deviceType = DeviceType.ANDROID,
            appVersion = "1.0.0",
            ipAddress = "192.168.1.100",
            port = 8080,
            lastSeen = System.currentTimeMillis()
        )
        
        // Act
        composeTestRule.setContent {
            SyncSettingsScreen(
                conflictResolutionStrategy = ConflictResolutionStrategy.LATEST_TIMESTAMP,
                trustedDevices = listOf(testDevice),
                syncOnChargerOnly = false,
                onConflictStrategyChange = {},
                onRemoveDevice = {},
                onSyncOnChargerOnlyChange = {},
                onNavigateBack = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithContentDescription("Remove Test Device")
            .assertIsDisplayed()
    }
    
    @Test
    fun `clicking remove button should trigger callback with device id`() {
        // Arrange
        val testDevice = DeviceInfo(
            deviceId = "test-id",
            deviceName = "Test Device",
            deviceType = DeviceType.ANDROID,
            appVersion = "1.0.0",
            ipAddress = "192.168.1.100",
            port = 8080,
            lastSeen = System.currentTimeMillis()
        )
        var removedDeviceId: String? = null
        
        // Act
        composeTestRule.setContent {
            SyncSettingsScreen(
                conflictResolutionStrategy = ConflictResolutionStrategy.LATEST_TIMESTAMP,
                trustedDevices = listOf(testDevice),
                syncOnChargerOnly = false,
                onConflictStrategyChange = {},
                onRemoveDevice = { removedDeviceId = it },
                onSyncOnChargerOnlyChange = {},
                onNavigateBack = {}
            )
        }
        
        composeTestRule
            .onNodeWithContentDescription("Remove Test Device")
            .performClick()
        
        // Assert
        assertEquals("test-id", removedDeviceId)
    }
    
    @Test
    fun `settings screen should display sync on charger only toggle`() {
        // Arrange & Act
        composeTestRule.setContent {
            SyncSettingsScreen(
                conflictResolutionStrategy = ConflictResolutionStrategy.LATEST_TIMESTAMP,
                trustedDevices = emptyList(),
                syncOnChargerOnly = false,
                onConflictStrategyChange = {},
                onRemoveDevice = {},
                onSyncOnChargerOnlyChange = {},
                onNavigateBack = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Sync on Charger Only")
            .assertIsDisplayed()
    }
    
    @Test
    fun `sync on charger toggle should reflect current state`() {
        // Arrange & Act
        composeTestRule.setContent {
            SyncSettingsScreen(
                conflictResolutionStrategy = ConflictResolutionStrategy.LATEST_TIMESTAMP,
                trustedDevices = emptyList(),
                syncOnChargerOnly = true,
                onConflictStrategyChange = {},
                onRemoveDevice = {},
                onSyncOnChargerOnlyChange = {},
                onNavigateBack = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNode(hasTestTag("sync_on_charger_toggle"))
            .assertIsOn()
    }
    
    @Test
    fun `toggling sync on charger should trigger callback`() {
        // Arrange
        var toggledValue: Boolean? = null
        
        // Act
        composeTestRule.setContent {
            SyncSettingsScreen(
                conflictResolutionStrategy = ConflictResolutionStrategy.LATEST_TIMESTAMP,
                trustedDevices = emptyList(),
                syncOnChargerOnly = false,
                onConflictStrategyChange = {},
                onRemoveDevice = {},
                onSyncOnChargerOnlyChange = { toggledValue = it },
                onNavigateBack = {}
            )
        }
        
        composeTestRule
            .onNode(hasTestTag("sync_on_charger_toggle"))
            .performClick()
        
        // Assert
        assertEquals(true, toggledValue)
    }
    
    @Test
    fun `settings screen should display privacy policy link`() {
        // Arrange & Act
        composeTestRule.setContent {
            SyncSettingsScreen(
                conflictResolutionStrategy = ConflictResolutionStrategy.LATEST_TIMESTAMP,
                trustedDevices = emptyList(),
                syncOnChargerOnly = false,
                onConflictStrategyChange = {},
                onRemoveDevice = {},
                onSyncOnChargerOnlyChange = {},
                onNavigateBack = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Privacy Policy")
            .assertIsDisplayed()
    }
    
    @Test
    fun `selecting conflict strategy should trigger callback`() {
        // Arrange
        var selectedStrategy: ConflictResolutionStrategy? = null
        
        // Act
        composeTestRule.setContent {
            SyncSettingsScreen(
                conflictResolutionStrategy = ConflictResolutionStrategy.LATEST_TIMESTAMP,
                trustedDevices = emptyList(),
                syncOnChargerOnly = false,
                onConflictStrategyChange = { selectedStrategy = it },
                onRemoveDevice = {},
                onSyncOnChargerOnlyChange = {},
                onNavigateBack = {}
            )
        }
        
        // Open dialog
        composeTestRule
            .onNodeWithText("Use Latest Timestamp")
            .performClick()
        
        // Select different strategy
        composeTestRule
            .onNodeWithText("Always Use Local")
            .performClick()
        
        // Assert
        assertEquals(ConflictResolutionStrategy.LOCAL_WINS, selectedStrategy)
    }
}
