package ireader.domain.models.sync

import kotlinx.serialization.Serializable

/**
 * Represents a paired device with trust information.
 *
 * @property device Information about the paired device
 * @property status Current pairing status
 * @property certificate Device certificate for secure communication
 * @property isTrusted Whether the device is trusted for syncing
 *
 * @throws IllegalArgumentException if validation fails
 */
@Serializable
data class PairedDevice(
    val device: DeviceInfo,
    val status: PairingStatus,
    val certificate: String,
    val isTrusted: Boolean
) {
    init {
        require(certificate.isNotBlank()) { "Certificate cannot be empty or blank" }
    }
}

/**
 * Status of device pairing process.
 */
@Serializable
enum class PairingStatus {
    /** Discovering devices on the network */
    DISCOVERING,
    
    /** Pairing in progress */
    PAIRING,
    
    /** Successfully paired */
    PAIRED,
    
    /** Pairing failed */
    FAILED,
    
    /** Device disconnected */
    DISCONNECTED
}
