package ireader.presentation.ui.sync.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import ireader.domain.models.sync.DeviceInfo
import ireader.domain.models.sync.DeviceType
import ireader.domain.models.sync.DiscoveredDevice
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

/**
 * UI tests for DeviceListItem component.
 * 
 * Tests device display, interaction, and accessibility features.
 */
class DeviceListItemTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `device list item should display device name`() {
        // Arrange
        val testDevice = createTestDevice(
            deviceName = "My Android Phone",
            deviceType = DeviceType.ANDROID
        )
        
        // Act
        composeTestRule.setContent {
            DeviceListItem(
                device = testDevice,
                onClick = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("My Android Phone")
            .assertIsDisplayed()
    }
    
    @Test
    fun `device list item should display IP address and port`() {
        // Arrange
        val testDevice = createTestDevice(
            ipAddress = "192.168.1.100",
            port = 8080
        )
        
        // Act
        composeTestRule.setContent {
            DeviceListItem(
                device = testDevice,
                onClick = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("192.168.1.100:8080")
            .assertIsDisplayed()
    }
    
    @Test
    fun `device list item should display Android icon for Android devices`() {
        // Arrange
        val testDevice = createTestDevice(
            deviceType = DeviceType.ANDROID
        )
        
        // Act
        composeTestRule.setContent {
            DeviceListItem(
                device = testDevice,
                onClick = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithContentDescription("ANDROID")
            .assertIsDisplayed()
    }
    
    @Test
    fun `device list item should display Desktop icon for Desktop devices`() {
        // Arrange
        val testDevice = createTestDevice(
            deviceType = DeviceType.DESKTOP
        )
        
        // Act
        composeTestRule.setContent {
            DeviceListItem(
                device = testDevice,
                onClick = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithContentDescription("DESKTOP")
            .assertIsDisplayed()
    }
    
    @Test
    fun `device list item should show reachable indicator for reachable devices`() {
        // Arrange
        val testDevice = createTestDevice(
            isReachable = true
        )
        
        // Act
        composeTestRule.setContent {
            DeviceListItem(
                device = testDevice,
                onClick = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithContentDescription("Device is reachable")
            .assertIsDisplayed()
    }
    
    @Test
    fun `device list item should show unreachable indicator for unreachable devices`() {
        // Arrange
        val testDevice = createTestDevice(
            isReachable = false
        )
        
        // Act
        composeTestRule.setContent {
            DeviceListItem(
                device = testDevice,
                onClick = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithContentDescription("Device is unreachable")
            .assertIsDisplayed()
    }
    
    @Test
    fun `clicking device list item should trigger onClick callback`() {
        // Arrange
        val testDevice = createTestDevice()
        var clicked = false
        
        // Act
        composeTestRule.setContent {
            DeviceListItem(
                device = testDevice,
                onClick = { clicked = true }
            )
        }
        
        composeTestRule
            .onNodeWithText(testDevice.deviceInfo.deviceName)
            .performClick()
        
        // Assert
        assertTrue(clicked)
    }
    
    @Test
    fun `device list item should have proper accessibility description`() {
        // Arrange
        val testDevice = createTestDevice(
            deviceName = "Test Device",
            ipAddress = "192.168.1.50",
            port = 9000,
            isReachable = true
        )
        
        // Act
        composeTestRule.setContent {
            DeviceListItem(
                device = testDevice,
                onClick = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNode(
                hasContentDescription(
                    "Device: Test Device, IP: 192.168.1.50:9000, reachable"
                )
            )
            .assertIsDisplayed()
    }
    
    @Test
    fun `device list item should have button role for accessibility`() {
        // Arrange
        val testDevice = createTestDevice()
        
        // Act
        composeTestRule.setContent {
            DeviceListItem(
                device = testDevice,
                onClick = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNode(hasClickAction())
            .assertIsDisplayed()
    }
    
    @Test
    fun `device list item should display different IP addresses correctly`() {
        // Arrange
        val testDevice1 = createTestDevice(ipAddress = "10.0.0.1", port = 8080)
        val testDevice2 = createTestDevice(ipAddress = "172.16.0.1", port = 9090)
        
        // Act
        composeTestRule.setContent {
            DeviceListItem(device = testDevice1, onClick = {})
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("10.0.0.1:8080")
            .assertIsDisplayed()
        
        // Act - change device
        composeTestRule.setContent {
            DeviceListItem(device = testDevice2, onClick = {})
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("172.16.0.1:9090")
            .assertIsDisplayed()
    }
    
    // Helper function to create test devices
    private fun createTestDevice(
        deviceId: String = "test-device-id",
        deviceName: String = "Test Device",
        deviceType: DeviceType = DeviceType.ANDROID,
        appVersion: String = "1.0.0",
        ipAddress: String = "192.168.1.100",
        port: Int = 8080,
        isReachable: Boolean = true
    ): DiscoveredDevice {
        return DiscoveredDevice(
            deviceInfo = DeviceInfo(
                deviceId = deviceId,
                deviceName = deviceName,
                deviceType = deviceType,
                appVersion = appVersion,
                ipAddress = ipAddress,
                port = port,
                lastSeen = System.currentTimeMillis()
            ),
            isReachable = isReachable,
            discoveredAt = System.currentTimeMillis()
        )
    }
}
