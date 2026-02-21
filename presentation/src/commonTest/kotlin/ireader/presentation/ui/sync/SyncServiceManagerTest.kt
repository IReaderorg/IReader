package ireader.presentation.ui.sync

import ireader.domain.models.sync.SyncStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for SyncServiceManager.
 * 
 * Following TDD methodology - these tests are written FIRST before implementation.
 * Tests verify that the manager:
 * - Tracks service state correctly
 * - Handles sync start/stop
 * - Updates progress
 * - Manages service lifecycle
 */
class SyncServiceManagerTest {

    @Test
    fun `initial state should be not running`() {
        // Arrange & Act
        val manager = SyncServiceManager()

        // Assert
        assertFalse(manager.isServiceRunning(), "Service should not be running initially")
    }

    @Test
    fun `startService should mark service as running`() {
        // Arrange
        val manager = SyncServiceManager()

        // Act
        manager.startService("Test Device")

        // Assert
        assertTrue(manager.isServiceRunning(), "Service should be running after start")
    }

    @Test
    fun `stopService should mark service as not running`() {
        // Arrange
        val manager = SyncServiceManager()
        manager.startService("Test Device")

        // Act
        manager.stopService()

        // Assert
        assertFalse(manager.isServiceRunning(), "Service should not be running after stop")
    }

    @Test
    fun `updateProgress should update current progress`() {
        // Arrange
        val manager = SyncServiceManager()
        manager.startService("Test Device")

        // Act
        manager.updateProgress(50, "Book.epub")

        // Assert
        assertEquals(50, manager.getCurrentProgress(), "Progress should be updated to 50")
        assertEquals("Book.epub", manager.getCurrentItem(), "Current item should be updated")
    }

    @Test
    fun `updateProgress should clamp to 0-100 range`() {
        // Arrange
        val manager = SyncServiceManager()
        manager.startService("Test Device")

        // Act - test negative
        manager.updateProgress(-10, "Item1")
        assertEquals(0, manager.getCurrentProgress(), "Progress should be clamped to 0")

        // Act - test over 100
        manager.updateProgress(150, "Item2")
        assertEquals(100, manager.getCurrentProgress(), "Progress should be clamped to 100")
    }

    @Test
    fun `getDeviceName should return current device name`() {
        // Arrange
        val deviceName = "Test Device"
        val manager = SyncServiceManager()

        // Act
        manager.startService(deviceName)

        // Assert
        assertEquals(deviceName, manager.getDeviceName(), "Device name should match")
    }

    @Test
    fun `stopService should clear device name and progress`() {
        // Arrange
        val manager = SyncServiceManager()
        manager.startService("Test Device")
        manager.updateProgress(50, "Book.epub")

        // Act
        manager.stopService()

        // Assert
        assertEquals("", manager.getDeviceName(), "Device name should be cleared")
        assertEquals(0, manager.getCurrentProgress(), "Progress should be reset to 0")
        assertEquals("", manager.getCurrentItem(), "Current item should be cleared")
    }

    @Test
    fun `multiple startService calls should update device name`() {
        // Arrange
        val manager = SyncServiceManager()
        manager.startService("Device 1")

        // Act
        manager.startService("Device 2")

        // Assert
        assertEquals("Device 2", manager.getDeviceName(), "Device name should be updated")
        assertTrue(manager.isServiceRunning(), "Service should still be running")
    }
}
