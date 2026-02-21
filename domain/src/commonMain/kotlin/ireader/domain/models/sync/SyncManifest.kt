package ireader.domain.models.sync

import kotlinx.serialization.Serializable

/**
 * Manifest of items available for synchronization.
 * Used to determine what needs to be synced between devices.
 *
 * @property deviceId ID of the device that created this manifest
 * @property timestamp When this manifest was created
 * @property items List of items available for sync
 *
 * @throws IllegalArgumentException if validation fails
 */
@Serializable
data class SyncManifest(
    val deviceId: String,
    val timestamp: Long,
    val items: List<SyncManifestItem>
) {
    init {
        require(deviceId.isNotBlank()) { "Device ID cannot be empty or blank" }
        require(timestamp >= 0) { "Timestamp cannot be negative, got: $timestamp" }
    }
}

/**
 * Represents a single item in the sync manifest.
 *
 * @property itemId Unique identifier for the item
 * @property itemType Type of the item (book, progress, bookmark)
 * @property hash Hash of the item's content for change detection
 * @property lastModified When the item was last modified
 *
 * @throws IllegalArgumentException if validation fails
 */
@Serializable
data class SyncManifestItem(
    val itemId: String,
    val itemType: SyncItemType,
    val hash: String,
    val lastModified: Long
) {
    init {
        require(itemId.isNotBlank()) { "Item ID cannot be empty or blank" }
        require(hash.isNotBlank()) { "Hash cannot be empty or blank" }
        require(lastModified >= 0) { "Last modified timestamp cannot be negative, got: $lastModified" }
    }
}

/**
 * Types of items that can be synchronized.
 */
@Serializable
enum class SyncItemType {
    /** Book file and metadata */
    BOOK,
    
    /** Reading progress for a book */
    READING_PROGRESS,
    
    /** Bookmark in a book */
    BOOKMARK
}
