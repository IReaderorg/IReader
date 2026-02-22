package ireader.domain.models.sync

/**
 * Represents a paired device with trust information.
 */
data class PairedDevice(
    val device: DeviceInfo,
    val status: PairingStatus,
    val certificate: String,
    val isTrusted: Boolean
)

enum class PairingStatus {
    DISCOVERING,
    PAIRING,
    PAIRED,
    FAILED,
    DISCONNECTED
}
