package ireader.domain.models.sync

import kotlinx.serialization.Serializable

/**
 * Messages exchanged during sync protocol.
 * 
 * Protocol flow:
 * 1. Client connects to Server
 * 2. Client sends ManifestRequest
 * 3. Server responds with ManifestMessage (server's manifest)
 * 4. Client sends ManifestMessage (client's manifest)
 * 5. Both calculate what to send/receive
 * 6. Both exchange DataMessages
 * 7. Both send SyncComplete when done
 */
@Serializable
sealed class SyncMessage {
    /**
     * Request to exchange manifests.
     * Sent by client to initiate sync.
     */
    @Serializable
    data object ManifestRequest : SyncMessage()
    
    /**
     * Contains a device's sync manifest.
     * Sent by both client and server to exchange what they have.
     */
    @Serializable
    data class ManifestMessage(val manifest: SyncManifest) : SyncMessage()
    
    /**
     * Contains actual sync data (books, progress, bookmarks).
     * Sent by device that has data to share.
     */
    @Serializable
    data class DataMessage(val data: SyncData) : SyncMessage()
    
    /**
     * Indicates sync is complete.
     * Sent by both devices when they're done.
     */
    @Serializable
    data object SyncComplete : SyncMessage()
    
    /**
     * Error during sync.
     */
    @Serializable
    data class ErrorMessage(val error: String) : SyncMessage()
}
