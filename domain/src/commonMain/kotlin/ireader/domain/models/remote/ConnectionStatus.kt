package ireader.domain.models.remote

/**
 * Represents the connection status to the remote backend
 */
enum class ConnectionStatus {
    CONNECTED,
    DISCONNECTED,
    CONNECTING,
    ERROR
}
