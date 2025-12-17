package ireader.plugin.api

import kotlinx.serialization.Serializable

/**
 * Base interface for plugins that connect to local servers.
 * Provides common functionality for server discovery, connection management,
 * and health monitoring.
 */
interface LocalServerPlugin : Plugin {
    /**
     * Server configuration.
     */
    val serverConfig: LocalServerConfig
    
    /**
     * Check if server is reachable.
     */
    suspend fun checkConnection(endpoint: String? = null): LocalServerResult<ServerStatus>
    
    /**
     * Discover servers on local network.
     */
    suspend fun discoverServers(): LocalServerResult<List<DiscoveredServer>>
    
    /**
     * Set the server endpoint.
     */
    fun setEndpoint(endpoint: String)
    
    /**
     * Get the current endpoint.
     */
    fun getEndpoint(): String
    
    /**
     * Test connection with timeout.
     */
    suspend fun testConnection(endpoint: String, timeoutMs: Long = 5000): Boolean
}

/**
 * Local server configuration.
 */
@Serializable
data class LocalServerConfig(
    /** Default server endpoint */
    val defaultEndpoint: String,
    /** Default port */
    val defaultPort: Int,
    /** Service type for mDNS discovery */
    val serviceType: String? = null,
    /** Whether to support auto-discovery */
    val supportsDiscovery: Boolean = false,
    /** Connection timeout in milliseconds */
    val connectionTimeoutMs: Long = 10000,
    /** Read timeout in milliseconds */
    val readTimeoutMs: Long = 60000,
    /** Whether to retry on failure */
    val retryOnFailure: Boolean = true,
    /** Maximum retry attempts */
    val maxRetries: Int = 3,
    /** Retry delay in milliseconds */
    val retryDelayMs: Long = 1000,
    /** Whether to use SSL/TLS */
    val useSsl: Boolean = false,
    /** Whether to verify SSL certificates */
    val verifySsl: Boolean = true
)

/**
 * Server status information.
 */
@Serializable
data class ServerStatus(
    /** Whether server is online */
    val isOnline: Boolean,
    /** Server endpoint */
    val endpoint: String,
    /** Server name */
    val serverName: String? = null,
    /** Server version */
    val serverVersion: String? = null,
    /** Response time in milliseconds */
    val responseTimeMs: Long,
    /** Server capabilities */
    val capabilities: List<String> = emptyList(),
    /** Additional server info */
    val info: Map<String, String> = emptyMap()
)

/**
 * Discovered server on local network.
 */
@Serializable
data class DiscoveredServer(
    /** Server name */
    val name: String,
    /** Server address */
    val address: String,
    /** Server port */
    val port: Int,
    /** Service type */
    val serviceType: String? = null,
    /** Server properties */
    val properties: Map<String, String> = emptyMap()
) {
    val endpoint: String get() = "http://$address:$port"
}

/**
 * Result wrapper for local server operations.
 */
sealed class LocalServerResult<out T> {
    data class Success<T>(val data: T) : LocalServerResult<T>()
    data class Error(val error: LocalServerError) : LocalServerResult<Nothing>()
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    inline fun <R> map(transform: (T) -> R): LocalServerResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }
}

/**
 * Local server errors.
 */
@Serializable
sealed class LocalServerError {
    data class ConnectionRefused(val endpoint: String) : LocalServerError()
    data class Timeout(val endpoint: String, val timeoutMs: Long) : LocalServerError()
    data class HostNotFound(val host: String) : LocalServerError()
    data class SslError(val reason: String) : LocalServerError()
    data class AuthenticationRequired(val endpoint: String) : LocalServerError()
    data class ServerError(val statusCode: Int, val message: String) : LocalServerError()
    data class DiscoveryFailed(val reason: String) : LocalServerError()
    data class Unknown(val message: String) : LocalServerError()
}

/**
 * Helper class for building local server endpoints.
 */
object LocalServerEndpoint {
    /**
     * Build endpoint URL from components.
     */
    fun build(
        host: String,
        port: Int,
        path: String = "",
        useSsl: Boolean = false
    ): String {
        val protocol = if (useSsl) "https" else "http"
        val normalizedPath = if (path.startsWith("/")) path else "/$path"
        return "$protocol://$host:$port$normalizedPath"
    }
    
    /**
     * Parse endpoint URL into components.
     */
    fun parse(endpoint: String): EndpointComponents? {
        val regex = Regex("""^(https?)://([^:/]+)(?::(\d+))?(.*)$""")
        val match = regex.matchEntire(endpoint) ?: return null
        
        val (protocol, host, portStr, path) = match.destructured
        val port = portStr.toIntOrNull() ?: if (protocol == "https") 443 else 80
        
        return EndpointComponents(
            protocol = protocol,
            host = host,
            port = port,
            path = path.ifEmpty { "/" },
            useSsl = protocol == "https"
        )
    }
    
    /**
     * Check if endpoint is a local address.
     */
    fun isLocalAddress(endpoint: String): Boolean {
        val components = parse(endpoint) ?: return false
        val host = components.host.lowercase()
        
        return host == "localhost" ||
                host == "127.0.0.1" ||
                host.startsWith("192.168.") ||
                host.startsWith("10.") ||
                host.startsWith("172.16.") ||
                host.startsWith("172.17.") ||
                host.startsWith("172.18.") ||
                host.startsWith("172.19.") ||
                host.startsWith("172.2") ||
                host.startsWith("172.30.") ||
                host.startsWith("172.31.")
    }
}

/**
 * Endpoint URL components.
 */
data class EndpointComponents(
    val protocol: String,
    val host: String,
    val port: Int,
    val path: String,
    val useSsl: Boolean
)

/**
 * Common local server ports.
 */
object CommonPorts {
    const val GRADIO_DEFAULT = 7860
    const val AUTOMATIC1111 = 7860
    const val COMFYUI = 8188
    const val REAL_ESRGAN = 7861
    const val COQUI_TTS = 5002
    const val XTTS = 8020
    const val OLLAMA = 11434
    const val LLAMA_CPP = 8080
}