package ireader.domain.models.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SyncManifestTest {

    @Test
    fun `SyncManifest should be created with valid data`() {
        // Arrange
        val deviceId = "test-device-123"
        val timestamp = System.currentTimeMillis()
        val items = listOf(createTestManifestItem())

        // Act
        val manifest = SyncManifest(
            deviceId = deviceId,
            timestamp = timestamp,
            items = items
        )

        // Assert
        assertEquals(deviceId, manifest.deviceId)
        assertEquals(timestamp, manifest.timestamp)
        assertEquals(items, manifest.items)
    }

    @Test
    fun `SyncManifest should reject empty deviceId`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            SyncManifest(
                deviceId = "",
                timestamp = System.currentTimeMillis(),
                items = listOf(createTestManifestItem())
            )
        }
    }

    @Test
    fun `SyncManifest should reject negative timestamp`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            SyncManifest(
                deviceId = "test-device-123",
                timestamp = -1L,
                items = listOf(createTestManifestItem())
            )
        }
    }

    @Test
    fun `SyncManifest should allow empty items list`() {
        // Arrange & Act
        val manifest = SyncManifest(
            deviceId = "test-device-123",
            timestamp = System.currentTimeMillis(),
            items = emptyList()
        )

        // Assert
        assertTrue(manifest.items.isEmpty())
    }

    private fun createTestManifestItem(): SyncManifestItem {
        return SyncManifestItem(
            itemId = "book-123",
            itemType = SyncItemType.BOOK,
            hash = "abc123def456",
            lastModified = System.currentTimeMillis()
        )
    }
}

class SyncManifestItemTest {

    @Test
    fun `SyncManifestItem should be created with valid data`() {
        // Arrange
        val itemId = "book-123"
        val itemType = SyncItemType.BOOK
        val hash = "abc123def456"
        val lastModified = System.currentTimeMillis()

        // Act
        val item = SyncManifestItem(
            itemId = itemId,
            itemType = itemType,
            hash = hash,
            lastModified = lastModified
        )

        // Assert
        assertEquals(itemId, item.itemId)
        assertEquals(itemType, item.itemType)
        assertEquals(hash, item.hash)
        assertEquals(lastModified, item.lastModified)
    }

    @Test
    fun `SyncManifestItem should reject empty itemId`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            SyncManifestItem(
                itemId = "",
                itemType = SyncItemType.BOOK,
                hash = "abc123",
                lastModified = System.currentTimeMillis()
            )
        }
    }

    @Test
    fun `SyncManifestItem should reject empty hash`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            SyncManifestItem(
                itemId = "book-123",
                itemType = SyncItemType.BOOK,
                hash = "",
                lastModified = System.currentTimeMillis()
            )
        }
    }

    @Test
    fun `SyncManifestItem should reject negative lastModified timestamp`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            SyncManifestItem(
                itemId = "book-123",
                itemType = SyncItemType.BOOK,
                hash = "abc123",
                lastModified = -1L
            )
        }
    }

    @Test
    fun `SyncItemType should have all required types`() {
        // Arrange & Act
        val bookType = SyncItemType.BOOK
        val progressType = SyncItemType.READING_PROGRESS
        val bookmarkType = SyncItemType.BOOKMARK

        // Assert
        assertTrue(SyncItemType.values().contains(SyncItemType.BOOK))
        assertTrue(SyncItemType.values().contains(SyncItemType.READING_PROGRESS))
        assertTrue(SyncItemType.values().contains(SyncItemType.BOOKMARK))
    }
}
