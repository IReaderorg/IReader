package ireader.domain.models.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class SyncStatusTest {

    @Test
    fun `SyncStatus Idle should be created`() {
        // Arrange & Act
        val status = SyncStatus.Idle

        // Assert
        assertIs<SyncStatus.Idle>(status)
    }

    @Test
    fun `SyncStatus Discovering should be created`() {
        // Arrange & Act
        val status = SyncStatus.Discovering

        // Assert
        assertIs<SyncStatus.Discovering>(status)
    }

    @Test
    fun `SyncStatus Connecting should be created with device name`() {
        // Arrange
        val deviceName = "My Phone"

        // Act
        val status = SyncStatus.Connecting(deviceName)

        // Assert
        assertIs<SyncStatus.Connecting>(status)
        assertEquals(deviceName, status.deviceName)
    }

    @Test
    fun `SyncStatus Syncing should be created with valid data`() {
        // Arrange
        val deviceName = "My Phone"
        val progress = 0.5f
        val currentItem = "Book Title"

        // Act
        val status = SyncStatus.Syncing(deviceName, progress, currentItem)

        // Assert
        assertIs<SyncStatus.Syncing>(status)
        assertEquals(deviceName, status.deviceName)
        assertEquals(progress, status.progress)
        assertEquals(currentItem, status.currentItem)
        assertEquals(0, status.currentIndex)
        assertEquals(0, status.totalItems)
    }

    @Test
    fun `SyncStatus Syncing should be created with item counts`() {
        // Arrange
        val deviceName = "My Phone"
        val progress = 0.5f
        val currentItem = "Book Title"
        val currentIndex = 5
        val totalItems = 10

        // Act
        val status = SyncStatus.Syncing(deviceName, progress, currentItem, currentIndex, totalItems)

        // Assert
        assertIs<SyncStatus.Syncing>(status)
        assertEquals(deviceName, status.deviceName)
        assertEquals(progress, status.progress)
        assertEquals(currentItem, status.currentItem)
        assertEquals(currentIndex, status.currentIndex)
        assertEquals(totalItems, status.totalItems)
    }

    @Test
    fun `SyncStatus Syncing should reject negative progress`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            SyncStatus.Syncing("My Phone", -0.1f, "Book Title")
        }
    }

    @Test
    fun `SyncStatus Syncing should reject progress above 1`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            SyncStatus.Syncing("My Phone", 1.1f, "Book Title")
        }
    }

    @Test
    fun `SyncStatus Syncing should accept progress of 0`() {
        // Arrange & Act
        val status = SyncStatus.Syncing("My Phone", 0.0f, "Book Title")

        // Assert
        assertEquals(0.0f, status.progress)
    }

    @Test
    fun `SyncStatus Syncing should accept progress of 1`() {
        // Arrange & Act
        val status = SyncStatus.Syncing("My Phone", 1.0f, "Book Title")

        // Assert
        assertEquals(1.0f, status.progress)
    }

    @Test
    fun `SyncStatus Syncing should reject negative currentIndex`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            SyncStatus.Syncing("My Phone", 0.5f, "Book Title", -1, 10)
        }
    }

    @Test
    fun `SyncStatus Syncing should reject negative totalItems`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            SyncStatus.Syncing("My Phone", 0.5f, "Book Title", 5, -1)
        }
    }

    @Test
    fun `SyncStatus Completed should be created with valid data`() {
        // Arrange
        val deviceName = "My Phone"
        val syncedItems = 42
        val duration = 5000L

        // Act
        val status = SyncStatus.Completed(deviceName, syncedItems, duration)

        // Assert
        assertIs<SyncStatus.Completed>(status)
        assertEquals(deviceName, status.deviceName)
        assertEquals(syncedItems, status.syncedItems)
        assertEquals(duration, status.duration)
    }

    @Test
    fun `SyncStatus Completed should reject negative syncedItems`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            SyncStatus.Completed("My Phone", -1, 5000L)
        }
    }

    @Test
    fun `SyncStatus Completed should reject negative duration`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            SyncStatus.Completed("My Phone", 42, -1L)
        }
    }

    @Test
    fun `SyncStatus Failed should be created with device name and error`() {
        // Arrange
        val deviceName = "My Phone"
        val error = SyncError.NetworkUnavailable

        // Act
        val status = SyncStatus.Failed(deviceName, error)

        // Assert
        assertIs<SyncStatus.Failed>(status)
        assertEquals(deviceName, status.deviceName)
        assertEquals(error, status.error)
    }

    @Test
    fun `SyncStatus Failed should allow null device name`() {
        // Arrange
        val error = SyncError.NetworkUnavailable

        // Act
        val status = SyncStatus.Failed(null, error)

        // Assert
        assertIs<SyncStatus.Failed>(status)
        assertEquals(null, status.deviceName)
    }
}
