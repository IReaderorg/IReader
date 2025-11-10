package ireader.domain.services.tts_service.piper

import java.io.File
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.absolute
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile

/**
 * Manages security policies and sandboxing for Piper TTS operations.
 * 
 * This class provides:
 * - File access control and sandboxing
 * - Resource limits enforcement
 * - Security policy validation
 * - Security event logging
 * - Permission management
 * 
 * The SecurityManager ensures that all file operations are confined to
 * approved directories and that resource usage stays within safe limits.
 * 
 * Usage:
 * ```kotlin
 * val securityManager = SecurityManager()
 * 
 * // Validate model file access
 * if (securityManager.canAccessFile(modelPath)) {
 *     // Safe to load the model
 * }
 * 
 * // Check resource limits
 * if (securityManager.canAllocateMemory(requiredBytes)) {
 *     // Safe to proceed
 * }
 * ```
 * 
 * @see InputSanitizer
 * @see LibraryVerifier
 */
class SecurityManager {
    
    /**
     * Security policy configuration.
     */
    data class SecurityPolicy(
        val allowedModelDirectories: Set<Path> = emptySet(),
        val allowedLibraryDirectories: Set<Path> = emptySet(),
        val maxModelFileSize: Long = 500L * 1024 * 1024, // 500 MB
        val maxConfigFileSize: Long = 10L * 1024 * 1024, // 10 MB
        val maxMemoryUsage: Long = 2L * 1024 * 1024 * 1024, // 2 GB
        val maxConcurrentInstances: Int = 10,
        val allowedModelExtensions: Set<String> = setOf("onnx"),
        val allowedConfigExtensions: Set<String> = setOf("json", "yaml", "yml"),
        val enableFileAccessLogging: Boolean = true,
        val enableResourceMonitoring: Boolean = true
    )
    
    /**
     * Security event types for logging.
     */
    enum class SecurityEventType {
        FILE_ACCESS_DENIED,
        FILE_ACCESS_GRANTED,
        RESOURCE_LIMIT_EXCEEDED,
        INVALID_FILE_EXTENSION,
        PATH_TRAVERSAL_ATTEMPT,
        SUSPICIOUS_ACTIVITY,
        POLICY_VIOLATION
    }
    
    /**
     * Security event for audit logging.
     */
    data class SecurityEvent(
        val type: SecurityEventType,
        val timestamp: Instant = Instant.now(),
        val message: String,
        val details: Map<String, String> = emptyMap(),
        val severity: Severity = Severity.WARNING
    ) {
        enum class Severity {
            INFO,
            WARNING,
            ERROR,
            CRITICAL
        }
    }
    
    private val policy: SecurityPolicy
    private val inputSanitizer = InputSanitizer()
    private val securityEvents = mutableListOf<SecurityEvent>()
    private val activeInstances = mutableSetOf<Long>()
    
    /**
     * Create a SecurityManager with default policy.
     */
    constructor() {
        this.policy = createDefaultPolicy()
    }
    
    /**
     * Create a SecurityManager with custom policy.
     */
    constructor(policy: SecurityPolicy) {
        this.policy = policy
    }
    
    /**
     * Create default security policy based on system configuration.
     */
    private fun createDefaultPolicy(): SecurityPolicy {
        val userHome = System.getProperty("user.home")
        val appDataDir = when {
            System.getProperty("os.name").lowercase().contains("win") -> {
                File(System.getenv("APPDATA") ?: "$userHome\\AppData\\Roaming", "IReader").toPath()
            }
            System.getProperty("os.name").lowercase().contains("mac") -> {
                File(userHome, "Library/Application Support/IReader").toPath()
            }
            else -> {
                File(userHome, ".local/share/ireader").toPath()
            }
        }
        
        val modelsDir = appDataDir.resolve("models")
        val tempDir = File(System.getProperty("java.io.tmpdir"), "piper_native").toPath()
        
        return SecurityPolicy(
            allowedModelDirectories = setOf(modelsDir, tempDir),
            allowedLibraryDirectories = setOf(tempDir)
        )
    }
    
    /**
     * Validate that a file can be accessed according to security policy.
     * 
     * Checks:
     * - File is within allowed directories
     * - File extension is allowed
     * - File size is within limits
     * - No path traversal attempts
     * 
     * @param filePath Path to the file
     * @param fileType Type of file (for appropriate validation)
     * @return true if access is allowed, false otherwise
     */
    fun canAccessFile(filePath: String, fileType: FileType = FileType.MODEL): Boolean {
        try {
            val path = File(filePath).toPath().absolute().normalize()
            
            // Check if file exists
            if (!path.exists()) {
                logSecurityEvent(
                    SecurityEventType.FILE_ACCESS_DENIED,
                    "File does not exist: $filePath",
                    mapOf("path" to filePath, "type" to fileType.name)
                )
                return false
            }
            
            // Check if it's a regular file
            if (!path.isRegularFile()) {
                logSecurityEvent(
                    SecurityEventType.FILE_ACCESS_DENIED,
                    "Path is not a regular file: $filePath",
                    mapOf("path" to filePath, "type" to fileType.name)
                )
                return false
            }
            
            // Check if file is within allowed directories
            val allowedDirs = when (fileType) {
                FileType.MODEL, FileType.CONFIG -> policy.allowedModelDirectories
                FileType.LIBRARY -> policy.allowedLibraryDirectories
            }
            
            val isInAllowedDir = allowedDirs.any { allowedDir ->
                inputSanitizer.isPathWithinDirectory(path, allowedDir)
            }
            
            if (!isInAllowedDir && allowedDirs.isNotEmpty()) {
                logSecurityEvent(
                    SecurityEventType.PATH_TRAVERSAL_ATTEMPT,
                    "File is outside allowed directories: $filePath",
                    mapOf(
                        "path" to filePath,
                        "type" to fileType.name,
                        "allowed_dirs" to allowedDirs.joinToString(", ")
                    ),
                    SecurityEvent.Severity.ERROR
                )
                return false
            }
            
            // Check file extension
            val allowedExtensions = when (fileType) {
                FileType.MODEL -> policy.allowedModelExtensions
                FileType.CONFIG -> policy.allowedConfigExtensions
                FileType.LIBRARY -> setOf("dll", "dylib", "so")
            }
            
            if (path.extension.lowercase() !in allowedExtensions) {
                logSecurityEvent(
                    SecurityEventType.INVALID_FILE_EXTENSION,
                    "Invalid file extension: ${path.extension}",
                    mapOf(
                        "path" to filePath,
                        "extension" to path.extension,
                        "allowed" to allowedExtensions.joinToString(", ")
                    )
                )
                return false
            }
            
            // Check file size
            val maxSize = when (fileType) {
                FileType.MODEL -> policy.maxModelFileSize
                FileType.CONFIG -> policy.maxConfigFileSize
                FileType.LIBRARY -> Long.MAX_VALUE // No limit for libraries
            }
            
            if (path.fileSize() > maxSize) {
                logSecurityEvent(
                    SecurityEventType.RESOURCE_LIMIT_EXCEEDED,
                    "File size exceeds limit: ${path.fileSize()} > $maxSize",
                    mapOf(
                        "path" to filePath,
                        "size" to path.fileSize().toString(),
                        "limit" to maxSize.toString()
                    )
                )
                return false
            }
            
            // Log successful access
            if (policy.enableFileAccessLogging) {
                logSecurityEvent(
                    SecurityEventType.FILE_ACCESS_GRANTED,
                    "File access granted: $filePath",
                    mapOf("path" to filePath, "type" to fileType.name),
                    SecurityEvent.Severity.INFO
                )
            }
            
            return true
            
        } catch (e: Exception) {
            logSecurityEvent(
                SecurityEventType.FILE_ACCESS_DENIED,
                "Error validating file access: ${e.message}",
                mapOf("path" to filePath, "error" to (e.message ?: "unknown"))
            )
            return false
        }
    }
    
    /**
     * Validate that a model file can be loaded.
     * 
     * @param modelPath Path to the model file
     * @return true if model can be loaded, false otherwise
     */
    fun canLoadModel(modelPath: String): Boolean {
        return canAccessFile(modelPath, FileType.MODEL)
    }
    
    /**
     * Validate that a config file can be loaded.
     * 
     * @param configPath Path to the config file
     * @return true if config can be loaded, false otherwise
     */
    fun canLoadConfig(configPath: String): Boolean {
        return canAccessFile(configPath, FileType.CONFIG)
    }
    
    /**
     * Validate that a library file can be loaded.
     * 
     * @param libraryPath Path to the library file
     * @return true if library can be loaded, false otherwise
     */
    fun canLoadLibrary(libraryPath: String): Boolean {
        return canAccessFile(libraryPath, FileType.LIBRARY)
    }
    
    /**
     * Check if memory allocation is within limits.
     * 
     * @param requestedBytes Number of bytes to allocate
     * @return true if allocation is allowed, false otherwise
     */
    fun canAllocateMemory(requestedBytes: Long): Boolean {
        if (!policy.enableResourceMonitoring) {
            return true
        }
        
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val availableMemory = runtime.maxMemory() - usedMemory
        
        if (requestedBytes > availableMemory) {
            logSecurityEvent(
                SecurityEventType.RESOURCE_LIMIT_EXCEEDED,
                "Insufficient memory for allocation",
                mapOf(
                    "requested" to requestedBytes.toString(),
                    "available" to availableMemory.toString(),
                    "used" to usedMemory.toString()
                )
            )
            return false
        }
        
        if (usedMemory + requestedBytes > policy.maxMemoryUsage) {
            logSecurityEvent(
                SecurityEventType.RESOURCE_LIMIT_EXCEEDED,
                "Memory allocation would exceed policy limit",
                mapOf(
                    "requested" to requestedBytes.toString(),
                    "current_usage" to usedMemory.toString(),
                    "limit" to policy.maxMemoryUsage.toString()
                )
            )
            return false
        }
        
        return true
    }
    
    /**
     * Register a new Piper instance.
     * 
     * @param instanceId The instance ID
     * @return true if instance can be created, false if limit exceeded
     */
    fun registerInstance(instanceId: Long): Boolean {
        if (activeInstances.size >= policy.maxConcurrentInstances) {
            logSecurityEvent(
                SecurityEventType.RESOURCE_LIMIT_EXCEEDED,
                "Maximum concurrent instances limit reached",
                mapOf(
                    "current" to activeInstances.size.toString(),
                    "limit" to policy.maxConcurrentInstances.toString()
                )
            )
            return false
        }
        
        activeInstances.add(instanceId)
        return true
    }
    
    /**
     * Unregister a Piper instance.
     * 
     * @param instanceId The instance ID
     */
    fun unregisterInstance(instanceId: Long) {
        activeInstances.remove(instanceId)
    }
    
    /**
     * Get the number of active instances.
     * 
     * @return Number of active instances
     */
    fun getActiveInstanceCount(): Int {
        return activeInstances.size
    }
    
    /**
     * Validate a directory for model storage.
     * 
     * @param directoryPath Path to the directory
     * @return true if directory is valid for storage, false otherwise
     */
    fun validateStorageDirectory(directoryPath: String): Boolean {
        val result = inputSanitizer.validateDirectoryPath(
            path = directoryPath,
            mustExist = false,
            mustBeWritable = true
        )
        
        if (!result.isValid) {
            logSecurityEvent(
                SecurityEventType.POLICY_VIOLATION,
                "Invalid storage directory: ${result.errorMessage}",
                mapOf("path" to directoryPath)
            )
            return false
        }
        
        return true
    }
    
    /**
     * Log a security event.
     * 
     * @param type Type of security event
     * @param message Event message
     * @param details Additional details
     * @param severity Event severity
     */
    private fun logSecurityEvent(
        type: SecurityEventType,
        message: String,
        details: Map<String, String> = emptyMap(),
        severity: SecurityEvent.Severity = SecurityEvent.Severity.WARNING
    ) {
        val event = SecurityEvent(
            type = type,
            message = message,
            details = details,
            severity = severity
        )
        
        securityEvents.add(event)
        
        // Print to console (in production, this should go to a proper logging system)
        val severityPrefix = when (severity) {
            SecurityEvent.Severity.INFO -> "ℹ"
            SecurityEvent.Severity.WARNING -> "⚠"
            SecurityEvent.Severity.ERROR -> "✗"
            SecurityEvent.Severity.CRITICAL -> "🔥"
        }
        
        println("$severityPrefix [SECURITY] $type: $message")
        if (details.isNotEmpty()) {
            details.forEach { (key, value) ->
                println("    $key: $value")
            }
        }
        
        // In production, also write to security audit log
        // SecurityAuditLogger.log(event)
    }
    
    /**
     * Get recent security events.
     * 
     * @param limit Maximum number of events to return
     * @return List of recent security events
     */
    fun getRecentEvents(limit: Int = 100): List<SecurityEvent> {
        return securityEvents.takeLast(limit)
    }
    
    /**
     * Get security events of a specific type.
     * 
     * @param type Type of events to retrieve
     * @param limit Maximum number of events to return
     * @return List of security events
     */
    fun getEventsByType(type: SecurityEventType, limit: Int = 100): List<SecurityEvent> {
        return securityEvents.filter { it.type == type }.takeLast(limit)
    }
    
    /**
     * Clear security event log.
     * 
     * Should only be called for testing or maintenance.
     */
    fun clearEventLog() {
        securityEvents.clear()
    }
    
    /**
     * Get a security report.
     * 
     * @return String containing security statistics and recent events
     */
    fun getSecurityReport(): String = buildString {
        appendLine("=== Security Report ===")
        appendLine()
        appendLine("Policy Configuration:")
        appendLine("  Max Model File Size: ${policy.maxModelFileSize / (1024 * 1024)} MB")
        appendLine("  Max Config File Size: ${policy.maxConfigFileSize / (1024 * 1024)} MB")
        appendLine("  Max Memory Usage: ${policy.maxMemoryUsage / (1024 * 1024)} MB")
        appendLine("  Max Concurrent Instances: ${policy.maxConcurrentInstances}")
        appendLine("  Allowed Model Extensions: ${policy.allowedModelExtensions.joinToString(", ")}")
        appendLine("  Allowed Config Extensions: ${policy.allowedConfigExtensions.joinToString(", ")}")
        appendLine()
        appendLine("Current Status:")
        appendLine("  Active Instances: ${activeInstances.size}")
        appendLine("  Total Security Events: ${securityEvents.size}")
        appendLine()
        
        // Event statistics
        val eventsByType = securityEvents.groupBy { it.type }
        appendLine("Event Statistics:")
        SecurityEventType.values().forEach { type ->
            val count = eventsByType[type]?.size ?: 0
            if (count > 0) {
                appendLine("  $type: $count")
            }
        }
        appendLine()
        
        // Recent critical events
        val criticalEvents = securityEvents.filter { 
            it.severity == SecurityEvent.Severity.CRITICAL || it.severity == SecurityEvent.Severity.ERROR 
        }.takeLast(10)
        
        if (criticalEvents.isNotEmpty()) {
            appendLine("Recent Critical Events:")
            criticalEvents.forEach { event ->
                appendLine("  [${event.timestamp}] ${event.type}: ${event.message}")
            }
        }
    }
    
    /**
     * File type enumeration for access control.
     */
    enum class FileType {
        MODEL,
        CONFIG,
        LIBRARY
    }
}
