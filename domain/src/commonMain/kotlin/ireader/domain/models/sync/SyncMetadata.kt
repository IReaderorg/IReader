package ireader.domain.models.sync

import kotlinx.serialization.Serializable

/**
 * Metadata about a sync operation.
 *
 * @property deviceId ID of the device that created this sync data
 * @property timestamp When the sync data was created (milliseconds since epoch)
 * @property version Protocol version number (must be >= 1)
 * @property checksum SHA-256 checksum of the sync data for integrity verification
 *
 * @throws IllegalArgumentException if validation fails
 */
@Serializable
data class SyncMetadata(
    val deviceId: String,
    val timestamp: Long,
    val version: Int,
    val checksum: String
) {
    init {
        require(deviceId.isNotBlank()) { "Device ID cannot be empty or blank" }
        require(timestamp >= 0) { "Timestamp cannot be negative, got: $timestamp" }
        require(version >= 1) { "Version must be at least 1, got: $version" }
        require(checksum.isNotBlank()) { "Checksum cannot be empty or blank" }
    }
}
