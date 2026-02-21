package ireader.presentation.ui.sync

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import ireader.domain.models.sync.*
import ireader.presentation.ui.sync.viewmodel.SyncViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * UI tests for SyncScreen following TDD methodology.
 * 
 * Tests are written BEFORE implementation to ensure proper behavior.
 */
class SyncScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `sync screen should display title in top bar`() {
        // Arrange
        val testState = SyncViewModel.State()
        
        // Act
        composeTestRule.setContent {
            SyncScreen(
                state = testState,
                onStartDiscovery = {},
                onStopDiscovery = {},
                onDeviceClick = {},
                onNavigateBack = {},
                onPairDevice = {},
                onDismissPairing = {},
                onResolveConflicts = {},
                onDismissConflicts = {},
                onCancelSync = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("WiFi Sync")
            .assertIsDisplayed()
    }
    
    @Test
    fun `sync screen should display back button`() {
        // Arrange
        val testState = SyncViewModel.State()
        
        // Act
        composeTestRule.setContent {
            SyncScreen(
                state = testState,
                onStartDiscovery = {},
                onStopDiscovery = {},
                onDeviceClick = {},
                onNavigateBack = {},
                onPairDevice = {},
                onDismissPairing = {},
                onResolveConflicts = {},
                onDismissConflicts = {},
                onCancelSync = {}
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
        val testState = SyncViewModel.State()
        var navigatedBack = false
        
        // Act
        composeTestRule.setContent {
            SyncScreen(
                state = testState,
                onStartDiscovery = {},
                onStopDiscovery = {},
                onDeviceClick = {},
                onNavigateBack = { navigatedBack = true },
                onPairDevice = {},
                onDismissPairing = {},
                onResolveConflicts = {},
                onDismissConflicts = {},
                onCancelSync = {}
            )
        }
        
        composeTestRule
            .onNodeWithContentDescription("Navigate back")
            .performClick()
        
        // Assert
        assertTrue(navigatedBack)
    }
    
    @Test
    fun `sync screen should display start discovery button when not discovering`() {
        // Arrange
        val testState = SyncViewModel.State(isDiscovering = false)
        
        // Act
        composeTestRule.setContent {
            SyncScreen(
                state = testState,
                onStartDiscovery = {},
                onStopDiscovery = {},
                onDeviceClick = {},
                onNavigateBack = {},
                onPairDevice = {},
                onDismissPairing = {},
                onResolveConflicts = {},
                onDismissConflicts = {},
                onCancelSync = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Start Discovery")
            .assertIsDisplayed()
    }
    
    @Test
    fun `sync screen should display stop discovery button when discovering`() {
        // Arrange
        val testState = SyncViewModel.State(isDiscovering = true)
        
        // Act
        composeTestRule.setContent {
            SyncScreen(
                state = testState,
                onStartDiscovery = {},
                onStopDiscovery = {},
                onDeviceClick = {},
                onNavigateBack = {},
                onPairDevice = {},
                onDismissPairing = {},
                onResolveConflicts = {},
                onDismissConflicts = {},
                onCancelSync = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Stop Discovery")
            .assertIsDisplayed()
    }
    
    @Test
    fun `clicking start discovery button should trigger callback`() {
        // Arrange
        val testState = SyncViewModel.State(isDiscovering = false)
        var discoveryStarted = false
        
        // Act
        composeTestRule.setContent {
            SyncScreen(
                state = testState,
                onStartDiscovery = { discoveryStarted = true },
                onStopDiscovery = {},
                onDeviceClick = {},
                onNavigateBack = {},
                onPairDevice = {},
                onDismissPairing = {},
                onResolveConflicts = {},
                onDismissConflicts = {},
                onCancelSync = {}
            )
        }
        
        composeTestRule
            .onNodeWithText("Start Discovery")
            .performClick()
        
        // Assert
        assertTrue(discoveryStarted)
    }
    
    @Test
    fun `clicking stop discovery button should trigger callback`() {
        // Arrange
        val testState = SyncViewModel.State(isDiscovering = true)
        var discoveryStopped = false
        
        // Act
        composeTestRule.setContent {
            SyncScreen(
                state = testState,
                onStartDiscovery = {},
                onStopDiscovery = { discoveryStopped = true },
                onDeviceClick = {},
                onNavigateBack = {},
                onPairDevice = {},
                onDismissPairing = {},
                onResolveConflicts = {},
                onDismissConflicts = {},
                onCancelSync = {}
            )
        }
        
        composeTestRule
            .onNodeWithText("Stop Discovery")
            .performClick()
        
        // Assert
        assertTrue(discoveryStopped)
    }
    
    @Test
    fun `sync screen should display empty state when no devices found`() {
        // Arrange
        val testState = SyncViewModel.State(
            discoveredDevices = emptyList(),
            isDiscovering = false
        )
        
        // Act
        composeTestRule.setContent {
            SyncScreen(
                state = testState,
                onStartDiscovery = {},
                onStopDiscovery = {},
                onDeviceClick = {},
                onNavigateBack = {},
                onPairDevice = {},
                onDismissPairing = {},
                onResolveConflicts = {},
                onDismissConflicts = {},
                onCancelSync = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("No devices found")
            .assertIsDisplayed()
    }
    
    @Test
    fun `sync screen should display device list when devices are discovered`() {
        // Arrange
        val testDevice = DiscoveredDevice(
            deviceInfo = DeviceInfo(
                deviceId = "test-id",
                deviceName = "Test Device",
                deviceType = DeviceType.ANDROID,
                appVersion = "1.0.0",
                ipAddress = "192.168.1.100",
                port = 8080,
                lastSeen = System.currentTimeMillis()
            ),
            isReachable = true,
            discoveredAt = System.currentTimeMillis()
        )
        val testState = SyncViewModel.State(
            discoveredDevices = listOf(testDevice)
        )
        
        // Act
        composeTestRule.setContent {
            SyncScreen(
                state = testState,
                onStartDiscovery = {},
                onStopDiscovery = {},
                onDeviceClick = {},
                onNavigateBack = {},
                onPairDevice = {},
                onDismissPairing = {},
                onResolveConflicts = {},
                onDismissConflicts = {},
                onCancelSync = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Test Device")
            .assertIsDisplayed()
    }
    
    @Test
    fun `device list item should display device IP address`() {
        // Arrange
        val testDevice = DiscoveredDevice(
            deviceInfo = DeviceInfo(
                deviceId = "test-id",
                deviceName = "Test Device",
                deviceType = DeviceType.ANDROID,
                appVersion = "1.0.0",
                ipAddress = "192.168.1.100",
                port = 8080,
                lastSeen = System.currentTimeMillis()
            ),
            isReachable = true,
            discoveredAt = System.currentTimeMillis()
        )
        val testState = SyncViewModel.State(
            discoveredDevices = listOf(testDevice)
        )
        
        // Act
        composeTestRule.setContent {
            SyncScreen(
                state = testState,
                onStartDiscovery = {},
                onStopDiscovery = {},
                onDeviceClick = {},
                onNavigateBack = {},
                onPairDevice = {},
                onDismissPairing = {},
                onResolveConflicts = {},
                onDismissConflicts = {},
                onCancelSync = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("192.168.1.100:8080")
            .assertIsDisplayed()
    }
    
    @Test
    fun `clicking device should trigger callback with device`() {
        // Arrange
        val testDevice = DiscoveredDevice(
            deviceInfo = DeviceInfo(
                deviceId = "test-id",
                deviceName = "Test Device",
                deviceType = DeviceType.ANDROID,
                appVersion = "1.0.0",
                ipAddress = "192.168.1.100",
                port = 8080,
                lastSeen = System.currentTimeMillis()
            ),
            isReachable = true,
            discoveredAt = System.currentTimeMillis()
        )
        val testState = SyncViewModel.State(
            discoveredDevices = listOf(testDevice)
        )
        var clickedDevice: DiscoveredDevice? = null
        
        // Act
        composeTestRule.setContent {
            SyncScreen(
                state = testState,
                onStartDiscovery = {},
                onStopDiscovery = {},
                onDeviceClick = { clickedDevice = it },
                onNavigateBack = {},
                onPairDevice = {},
                onDismissPairing = {},
                onResolveConflicts = {},
                onDismissConflicts = {},
                onCancelSync = {}
            )
        }
        
        composeTestRule
            .onNodeWithText("Test Device")
            .performClick()
        
        // Assert
        assertEquals(testDevice, clickedDevice)
    }
    
    @Test
    fun `sync screen should display sync status when syncing`() {
        // Arrange
        val testState = SyncViewModel.State(
            syncStatus = SyncStatus.Syncing(
                deviceName = "Test Device",
                progress = 0.5f,
                currentItem = "Book 1"
            )
        )
        
        // Act
        composeTestRule.setContent {
            SyncScreen(
                state = testState,
                onStartDiscovery = {},
                onStopDiscovery = {},
                onDeviceClick = {},
                onNavigateBack = {},
                onPairDevice = {},
                onDismissPairing = {},
                onResolveConflicts = {},
                onDismissConflicts = {},
                onCancelSync = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Syncing with Test Device")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Book 1")
            .assertIsDisplayed()
    }
    
    @Test
    fun `sync screen should display error message when error occurs`() {
        // Arrange
        val testState = SyncViewModel.State(
            error = "Connection failed"
        )
        
        // Act
        composeTestRule.setContent {
            SyncScreen(
                state = testState,
                onStartDiscovery = {},
                onStopDiscovery = {},
                onDeviceClick = {},
                onNavigateBack = {},
                onPairDevice = {},
                onDismissPairing = {},
                onResolveConflicts = {},
                onDismissConflicts = {},
                onCancelSync = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Connection failed")
            .assertIsDisplayed()
    }
    
    @Test
    fun `sync screen should not display snackbar when no error`() {
        // Arrange
        val testState = SyncViewModel.State(
            error = null
        )
        
        // Act
        composeTestRule.setContent {
            SyncScreen(
                state = testState,
                onStartDiscovery = {},
                onStopDiscovery = {},
                onDeviceClick = {},
                onNavigateBack = {},
                onPairDevice = {},
                onDismissPairing = {},
                onResolveConflicts = {},
                onDismissConflicts = {},
                onCancelSync = {}
            )
        }
        
        // Assert - Snackbar should not be visible
        composeTestRule
            .onAllNodesWithText("Connection failed")
            .assertCountEquals(0)
    }
    
    @Test
    fun `sync screen should display cancel button when sync is in progress`() {
        // Arrange
        val testState = SyncViewModel.State(
            syncStatus = SyncStatus.Syncing(
                deviceName = "Test Device",
                progress = 0.5f,
                currentItem = "Book 1"
            )
        )
        
        // Act
        composeTestRule.setContent {
            SyncScreen(
                state = testState,
                onStartDiscovery = {},
                onStopDiscovery = {},
                onDeviceClick = {},
                onNavigateBack = {},
                onPairDevice = {},
                onDismissPairing = {},
                onResolveConflicts = {},
                onDismissConflicts = {},
                onCancelSync = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Cancel Sync")
            .assertIsDisplayed()
    }
    
    @Test
    fun `sync screen should not display cancel button when sync is idle`() {
        // Arrange
        val testState = SyncViewModel.State(
            syncStatus = SyncStatus.Idle
        )
        
        // Act
        composeTestRule.setContent {
            SyncScreen(
                state = testState,
                onStartDiscovery = {},
                onStopDiscovery = {},
                onDeviceClick = {},
                onNavigateBack = {},
                onPairDevice = {},
                onDismissPairing = {},
                onResolveConflicts = {},
                onDismissConflicts = {},
                onCancelSync = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Cancel Sync")
            .assertDoesNotExist()
    }
    
    @Test
    fun `clicking cancel button should trigger callback`() {
        // Arrange
        val testState = SyncViewModel.State(
            syncStatus = SyncStatus.Syncing(
                deviceName = "Test Device",
                progress = 0.5f,
                currentItem = "Book 1"
            )
        )
        var syncCancelled = false
        
        // Act
        composeTestRule.setContent {
            SyncScreen(
                state = testState,
                onStartDiscovery = {},
                onStopDiscovery = {},
                onDeviceClick = {},
                onNavigateBack = {},
                onPairDevice = {},
                onDismissPairing = {},
                onResolveConflicts = {},
                onDismissConflicts = {},
                onCancelSync = { syncCancelled = true }
            )
        }
        
        composeTestRule
            .onNodeWithText("Cancel Sync")
            .performClick()
        
        // Assert
        assertTrue(syncCancelled)
    }
    
    @Test
    fun `cancel button should be enabled during sync`() {
        // Arrange
        val testState = SyncViewModel.State(
            syncStatus = SyncStatus.Syncing(
                deviceName = "Test Device",
                progress = 0.5f,
                currentItem = "Book 1"
            )
        )
        
        // Act
        composeTestRule.setContent {
            SyncScreen(
                state = testState,
                onStartDiscovery = {},
                onStopDiscovery = {},
                onDeviceClick = {},
                onNavigateBack = {},
                onPairDevice = {},
                onDismissPairing = {},
                onResolveConflicts = {},
                onDismissConflicts = {},
                onCancelSync = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Cancel Sync")
            .assertIsEnabled()
    }
    

}
