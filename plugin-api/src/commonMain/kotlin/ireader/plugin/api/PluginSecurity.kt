package ireader.plugin.api

import kotlinx.serialization.Serializable

/**
 * Security and sandboxing interface for plugins.
 * Defines security boundaries and resource limits.
 */
interface PluginSecurityManager {
    /**
     * Check if a permission is granted.
     */
    fun hasPermission(pluginId: String, permission: PluginPermission): Boolean
    
    /**
     * Request a permission at runtime.
     */
    suspend fun requestPermission(pluginId: String, permission: PluginPermission): PermissionResult
    
    /**
     * Get all granted permissions for a plugin.
     */
    fun getGrantedPermissions(pluginId: String): List<PluginPermission>
    
    /**
     * Check if URL is allowed for network access.
     */
    fun isUrlAllowed(pluginId: String, url: String): Boolean
    
    /**
     * Check if file path is allowed for storage access.
     */
    fun isPathAllowed(pluginId: String, path: String): Boolean
    
    /**
     * Get resource limits for a plugin.
     */
    fun getResourceLimits(pluginId: String): ResourceLimits
    
    /**
     * Report resource usage.
     */
    fun reportResourceUsage(pluginId: String, usage: ResourceUsage)
    
    /**
     * Check if plugin is within resource limits.
     */
    fun isWithinLimits(pluginId: String): Boolean
    
    /**
     * Get security audit log for a plugin.
     */
    suspend fun getAuditLog(pluginId: String, limit: Int = 100): List<SecurityAuditEntry>
    
    /**
     * Report a security violation.
     */
    fun reportViolation(pluginId: String, violation: SecurityViolation)
}

/**
 * Permission request result.
 */
@Serializable
sealed class PermissionResult {
    data object Granted : PermissionResult()
    data object Denied : PermissionResult()
    data object AlreadyGranted : PermissionResult()
    data class Error(val reason: String) : PermissionResult()
}

/**
 * Resource limits for plugins.
 */
@Serializable
data class ResourceLimits(
    /** Maximum memory usage in bytes */
    val maxMemoryBytes: Long = 50 * 1024 * 1024, // 50MB
    /** Maximum storage usage in bytes */
    val maxStorageBytes: Long = 100 * 1024 * 1024, // 100MB
    /** Maximum CPU time per operation in milliseconds */
    val maxCpuTimeMs: Long = 30000, // 30 seconds
    /** Maximum network requests per minute */
    val maxNetworkRequestsPerMinute: Int = 60,
    /** Maximum concurrent network connections */
    val maxConcurrentConnections: Int = 5,
    /** Maximum file handles */
    val maxFileHandles: Int = 10,
    /** Allowed URL patterns (regex) */
    val allowedUrlPatterns: List<String> = listOf(".*"),
    /** Blocked URL patterns (regex) */
    val blockedUrlPatterns: List<String> = emptyList(),
    /** Allowed file path patterns */
    val allowedPathPatterns: List<String> = emptyList()
)

/**
 * Current resource usage.
 */
@Serializable
data class ResourceUsage(
    /** Current memory usage in bytes */
    val memoryBytes: Long = 0,
    /** Current storage usage in bytes */
    val storageBytes: Long = 0,
    /** Network requests in last minute */
    val networkRequestsLastMinute: Int = 0,
    /** Current concurrent connections */
    val concurrentConnections: Int = 0,
    /** Open file handles */
    val openFileHandles: Int = 0,
    /** Total CPU time used in milliseconds */
    val totalCpuTimeMs: Long = 0
)

/**
 * Security audit log entry.
 */
@Serializable
data class SecurityAuditEntry(
    val timestamp: Long,
    val pluginId: String,
    val action: SecurityAction,
    val resource: String,
    val allowed: Boolean,
    val details: String? = null
)

/**
 * Security actions for audit logging.
 */
@Serializable
enum class SecurityAction {
    NETWORK_REQUEST,
    FILE_READ,
    FILE_WRITE,
    FILE_DELETE,
    PERMISSION_CHECK,
    PERMISSION_REQUEST,
    RESOURCE_LIMIT_CHECK,
    API_CALL,
    INTER_PLUGIN_COMMUNICATION
}

/**
 * Security violation.
 */
@Serializable
data class SecurityViolation(
    val type: ViolationType,
    val description: String,
    val resource: String? = null,
    val severity: ViolationSeverity = ViolationSeverity.WARNING
)

@Serializable
enum class ViolationType {
    PERMISSION_DENIED,
    RESOURCE_LIMIT_EXCEEDED,
    BLOCKED_URL_ACCESS,
    BLOCKED_PATH_ACCESS,
    SUSPICIOUS_ACTIVITY,
    RATE_LIMIT_EXCEEDED,
    INVALID_API_USAGE
}

@Serializable
enum class ViolationSeverity {
    INFO,
    WARNING,
    ERROR,
    CRITICAL
}

/**
 * Content security policy for plugins.
 */
@Serializable
data class ContentSecurityPolicy(
    /** Allowed script sources */
    val scriptSrc: List<String> = listOf("'self'"),
    /** Allowed style sources */
    val styleSrc: List<String> = listOf("'self'"),
    /** Allowed image sources */
    val imgSrc: List<String> = listOf("'self'", "data:", "https:"),
    /** Allowed font sources */
    val fontSrc: List<String> = listOf("'self'"),
    /** Allowed connection sources */
    val connectSrc: List<String> = listOf("'self'"),
    /** Allowed frame sources */
    val frameSrc: List<String> = emptyList(),
    /** Default source */
    val defaultSrc: List<String> = listOf("'self'")
)

/**
 * Plugin trust level.
 */
@Serializable
enum class TrustLevel {
    /** Untrusted - maximum restrictions */
    UNTRUSTED,
    /** Low trust - significant restrictions */
    LOW,
    /** Medium trust - moderate restrictions */
    MEDIUM,
    /** High trust - minimal restrictions */
    HIGH,
    /** Trusted - from official repository */
    TRUSTED,
    /** System - built-in plugins */
    SYSTEM
}

/**
 * Plugin signature verification.
 */
interface PluginSignatureVerifier {
    /**
     * Verify plugin signature.
     */
    suspend fun verifySignature(pluginFile: ByteArray): SignatureVerificationResult
    
    /**
     * Get plugin certificate info.
     */
    suspend fun getCertificateInfo(pluginFile: ByteArray): CertificateInfo?
    
    /**
     * Check if plugin is from trusted source.
     */
    suspend fun isTrustedSource(pluginFile: ByteArray): Boolean
}

/**
 * Signature verification result.
 */
@Serializable
sealed class SignatureVerificationResult {
    data object Valid : SignatureVerificationResult()
    data object Invalid : SignatureVerificationResult()
    data object Unsigned : SignatureVerificationResult()
    data object Expired : SignatureVerificationResult()
    data class Error(val reason: String) : SignatureVerificationResult()
}

/**
 * Certificate information.
 */
@Serializable
data class CertificateInfo(
    val issuer: String,
    val subject: String,
    val validFrom: Long,
    val validTo: Long,
    val fingerprint: String,
    val isTrusted: Boolean
)