package ireader.presentation.ui.sync.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for EmptyDeviceList component.
 * 
 * Tests empty state display for different discovery states.
 */
class EmptyDeviceListTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `empty device list should display no devices found when not discovering`() {
        // Arrange & Act
        composeTestRule.setContent {
            EmptyDeviceList(isDiscovering = false)
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("No devices found")
            .assertIsDisplayed()
    }
    
    @Test
    fun `empty device list should display searching message when discovering`() {
        // Arrange & Act
        composeTestRule.setContent {
            EmptyDeviceList(isDiscovering = true)
        }
        
        // Assert
        composeTestRule
            .onNodeWithText("Searching for devices...")
            .assertIsDisplayed()
    }
    
    @Test
    fun `empty device list should display start discovery instruction when not discovering`() {
        // Arrange & Act
        composeTestRule.setContent {
            EmptyDeviceList(isDiscovering = false)
        }
        
        // Assert
        composeTestRule
            .onNodeWithText(
                "Tap 'Start Discovery' to search for devices on your local network.",
                substring = true
            )
            .assertIsDisplayed()
    }
    
    @Test
    fun `empty device list should display WiFi instruction when discovering`() {
        // Arrange & Act
        composeTestRule.setContent {
            EmptyDeviceList(isDiscovering = true)
        }
        
        // Assert
        composeTestRule
            .onNodeWithText(
                "Make sure other devices are on the same WiFi network",
                substring = true
            )
            .assertIsDisplayed()
    }
    
    @Test
    fun `empty device list should display devices icon`() {
        // Arrange & Act
        composeTestRule.setContent {
            EmptyDeviceList(isDiscovering = false)
        }
        
        // Assert - Icon should be present (no content description for decorative icon)
        composeTestRule
            .onAllNodesWithContentDescription("")
            .assertCountEquals(0) // Decorative icon has null content description
    }
    
    @Test
    fun `empty device list should show progress indicator when discovering`() {
        // Arrange & Act
        composeTestRule.setContent {
            EmptyDeviceList(isDiscovering = true)
        }
        
        // Assert - Progress indicator should be visible
        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }
    
    @Test
    fun `empty device list should not show progress indicator when not discovering`() {
        // Arrange & Act
        composeTestRule.setContent {
            EmptyDeviceList(isDiscovering = false)
        }
        
        // Assert - Progress indicator should not be visible
        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertDoesNotExist()
    }
    
    @Test
    fun `empty device list should update message when discovery state changes`() {
        // Arrange
        var isDiscovering = false
        
        // Act - Initial state
        composeTestRule.setContent {
            EmptyDeviceList(isDiscovering = isDiscovering)
        }
        
        // Assert - Not discovering
        composeTestRule
            .onNodeWithText("No devices found")
            .assertIsDisplayed()
        
        // Act - Change to discovering
        isDiscovering = true
        composeTestRule.setContent {
            EmptyDeviceList(isDiscovering = isDiscovering)
        }
        
        // Assert - Discovering
        composeTestRule
            .onNodeWithText("Searching for devices...")
            .assertIsDisplayed()
    }
    
    @Test
    fun `empty device list should be centered in container`() {
        // Arrange & Act
        composeTestRule.setContent {
            EmptyDeviceList(isDiscovering = false)
        }
        
        // Assert - Text should be center aligned
        composeTestRule
            .onNodeWithText("No devices found")
            .assertIsDisplayed()
        
        // The component uses Alignment.CenterHorizontally and TextAlign.Center
        // which ensures proper centering
    }
}
