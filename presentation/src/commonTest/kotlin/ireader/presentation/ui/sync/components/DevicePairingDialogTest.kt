package ireader.presentation.ui.sync.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import ireader.domain.models.sync.DeviceInfo
import ireader.domain.models.sync.DeviceType
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for DevicePairingDialog component following TDD methodology.
 * 
 * Tests verify that the device pairing dialog properly displays a 6-digit PIN code,
 * device information, PIN verification UI, and pairing feedback following
 * Material Design 3 guidelines.
 * 
 * **Validates: Requirements FR6.1, FR6.2**
 */
class DevicePairingDialogTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    private val testDevice = DeviceInfo(
        deviceId = "test-device-123",
        deviceName = "Test Android Phone",
        deviceType = DeviceType.ANDROID,
        appVersion = "1.0.0",
        ipAddress = "192.168.1.100",
        port = 8080,
        lastSeen = System.currentTimeMillis()
    )
    
    // Test: Dialog should display 6-digit PIN code prominently
    @Test
    fun `device pairing dialog should display 6-digit PIN code prominently`() {
        // Arrange
        val pinCode = "123456"
        
        // Act
        composeTestRule.setContent {
            DevicePairingDialog(
                device = testDevice,
                pinCode = pinCode,
                onConfirm = {},
                onDismiss = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText(pinCode)
            .assertIsDisplayed()
    }
    
    // Test: Dialog should display device name
    @Test
    fun `device pairing dialog should display device name`() {
        // Arrange
        val pinCode = "654321"
        
        // Act
        composeTestRule.setContent {
            DevicePairingDialog(
                device = testDevice,
                pinCode = pinCode,
                onConfirm = {},
                onDismiss = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Test Android Phone", substring = true)
            .assertIsDisplayed()
    }
    
    // Test: Dialog should display device type
    @Test
    fun `device pairing dialog should display device type`() {
        // Arrange
        val pinCode = "111111"
        
        // Act
        composeTestRule.setContent {
            DevicePairingDialog(
                device = testDevice,
                pinCode = pinCode,
                onConfirm = {},
                onDismiss = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("ANDROID", substring = true)
            .assertIsDisplayed()
    }
    
    // Test: Dialog should have confirm button
    @Test
    fun `device pairing dialog should have confirm button`() {
        // Arrange
        val pinCode = "222222"
        
        // Act
        composeTestRule.setContent {
            DevicePairingDialog(
                device = testDevice,
                pinCode = pinCode,
                onConfirm = {},
                onDismiss = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Confirm")
            .assertIsDisplayed()
            .assertHasClickAction()
    }
    
    // Test: Dialog should have cancel button
    @Test
    fun `device pairing dialog should have cancel button`() {
        // Arrange
        val pinCode = "333333"
        
        // Act
        composeTestRule.setContent {
            DevicePairingDialog(
                device = testDevice,
                pinCode = pinCode,
                onConfirm = {},
                onDismiss = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Cancel")
            .assertIsDisplayed()
            .assertHasClickAction()
    }
    
    // Test: Confirm button should trigger onConfirm callback
    @Test
    fun `device pairing dialog confirm button should trigger callback`() {
        // Arrange
        val pinCode = "444444"
        var confirmCalled = false
        
        // Act
        composeTestRule.setContent {
            DevicePairingDialog(
                device = testDevice,
                pinCode = pinCode,
                onConfirm = { confirmCalled = true },
                onDismiss = {}
            )
        }
        
        composeTestRule
            .onNodeWithText("Confirm")
            .performClick()
        
        // Assert
        assert(confirmCalled) { "onConfirm callback should be called" }
    }
    
    // Test: Cancel button should trigger onDismiss callback
    @Test
    fun `device pairing dialog cancel button should trigger callback`() {
        // Arrange
        val pinCode = "555555"
        var dismissCalled = false
        
        // Act
        composeTestRule.setContent {
            DevicePairingDialog(
                device = testDevice,
                pinCode = pinCode,
                onConfirm = {},
                onDismiss = { dismissCalled = true }
            )
        }
        
        composeTestRule
            .onNodeWithText("Cancel")
            .performClick()
        
        // Assert
        assert(dismissCalled) { "onDismiss callback should be called" }
    }
    
    // Test: Dialog should display pairing instructions
    @Test
    fun `device pairing dialog should display pairing instructions`() {
        // Arrange
        val pinCode = "666666"
        
        // Act
        composeTestRule.setContent {
            DevicePairingDialog(
                device = testDevice,
                pinCode = pinCode,
                onConfirm = {},
                onDismiss = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Verify that this PIN matches", substring = true)
            .assertIsDisplayed()
    }
    
    // Test: PIN code should be formatted for readability (e.g., "123 456")
    @Test
    fun `device pairing dialog should format PIN code for readability`() {
        // Arrange
        val pinCode = "789012"
        
        // Act
        composeTestRule.setContent {
            DevicePairingDialog(
                device = testDevice,
                pinCode = pinCode,
                onConfirm = {},
                onDismiss = {}
            )
        }
        
        // Assert - PIN should be displayed with spacing for readability
        composeTestRule
            .onNodeWithText("789 012", substring = true)
            .assertIsDisplayed()
    }
    
    // Test: Dialog should display title
    @Test
    fun `device pairing dialog should display title`() {
        // Arrange
        val pinCode = "123456"
        
        // Act
        composeTestRule.setContent {
            DevicePairingDialog(
                device = testDevice,
                pinCode = pinCode,
                onConfirm = {},
                onDismiss = {}
            )
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Device Pairing", substring = true)
            .assertIsDisplayed()
    }
}
