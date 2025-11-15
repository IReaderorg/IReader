package ireader.domain.js.util

import ireader.domain.js.models.PluginMetadata

/**
 * Result of plugin validation.
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val reason: String) : ValidationResult()
    
    fun isValid(): Boolean = this is Valid
    fun getError(): String? = (this as? Invalid)?.reason
}

/**
 * Validator for JavaScript plugin code and metadata.
 * Performs security checks and format validation.
 */
class JSPluginValidator {
    
    companion object {
        // Regex patterns for validation
        private val PLUGIN_ID_PATTERN = Regex("^[a-z0-9-]+$")
        private val SEMVER_PATTERN = Regex("^\\d+\\.\\d+\\.\\d+$")
        
        // Dangerous patterns to check for
        private val DANGEROUS_PATTERNS = listOf(
            "eval\\s*\\(",
            "Function\\s*\\(",
            "require\\s*\\(\\s*['\"]fs['\"]",
            "require\\s*\\(\\s*['\"]child_process['\"]",
            "require\\s*\\(\\s*['\"]process['\"]",
            "require\\s*\\(\\s*['\"]os['\"]",
            "require\\s*\\(\\s*['\"]path['\"]",
            "require\\s*\\(\\s*['\"]net['\"]",
            "require\\s*\\(\\s*['\"]http['\"]",
            "require\\s*\\(\\s*['\"]https['\"]",
            "__dirname",
            "__filename",
            "process\\.exit",
            "process\\.env"
        )
    }
    
    /**
     * Validates JavaScript plugin code for security issues.
     * @param code The JavaScript code to validate
     * @return ValidationResult indicating if code is valid
     */
    fun validateCode(code: String): ValidationResult {
        // Check for empty code
        if (code.isBlank()) {
            return ValidationResult.Invalid("Plugin code is empty")
        }
        
        // Check for dangerous patterns
        for (pattern in DANGEROUS_PATTERNS) {
            val regex = Regex(pattern, RegexOption.IGNORE_CASE)
            if (regex.containsMatchIn(code)) {
                return ValidationResult.Invalid("Plugin contains forbidden pattern: $pattern")
            }
        }
        
        // Check for basic JavaScript syntax (very basic check)
        if (!code.contains("function") && !code.contains("=>") && !code.contains("class")) {
            return ValidationResult.Invalid("Plugin does not appear to contain valid JavaScript code")
        }
        
        return ValidationResult.Valid
    }
    
    /**
     * Validates plugin metadata for correctness.
     * @param metadata The plugin metadata to validate
     * @return ValidationResult indicating if metadata is valid
     */
    fun validateMetadata(metadata: PluginMetadata): ValidationResult {
        // Validate plugin ID format
        if (!PLUGIN_ID_PATTERN.matches(metadata.id)) {
            return ValidationResult.Invalid(
                "Plugin ID '${metadata.id}' must match pattern [a-z0-9-]+"
            )
        }
        
        // Validate version format (semantic versioning)
        if (!SEMVER_PATTERN.matches(metadata.version)) {
            return ValidationResult.Invalid(
                "Plugin version '${metadata.version}' must match semantic versioning format (X.Y.Z)"
            )
        }
        
        // Validate required fields are not empty
        if (metadata.name.isBlank()) {
            return ValidationResult.Invalid("Plugin name cannot be empty")
        }
        
        if (metadata.site.isBlank()) {
            return ValidationResult.Invalid("Plugin site cannot be empty")
        }
        
        if (metadata.lang.isBlank()) {
            return ValidationResult.Invalid("Plugin language cannot be empty")
        }
        
        // Validate icon URL format (basic check)
        if (metadata.icon.isNotBlank() && !metadata.icon.startsWith("http") && !metadata.icon.startsWith("data:")) {
            return ValidationResult.Invalid("Plugin icon must be a valid URL or data URI")
        }
        
        return ValidationResult.Valid
    }
    
    /**
     * Validates both code and metadata together.
     * @param code The JavaScript code
     * @param metadata The plugin metadata
     * @return ValidationResult indicating if both are valid
     */
    fun validate(code: String, metadata: PluginMetadata): ValidationResult {
        val codeResult = validateCode(code)
        if (!codeResult.isValid()) {
            return codeResult
        }
        
        return validateMetadata(metadata)
    }
    
    /**
     * Validates file access path for security.
     * Ensures path is within plugin storage directory and doesn't use directory traversal.
     * @param path The file path to validate
     * @param pluginId The plugin ID
     * @return ValidationResult indicating if path is safe
     */
    fun validateFileAccess(path: String, pluginId: String): ValidationResult {
        // Reject absolute paths
        if (path.startsWith("/") || path.contains(":\\")) {
            return ValidationResult.Invalid("Absolute paths are not allowed: $path")
        }
        
        // Reject parent directory traversal
        if (path.contains("..")) {
            return ValidationResult.Invalid("Parent directory traversal is not allowed: $path")
        }
        
        // Reject paths that try to escape plugin directory
        val normalizedPath = path.replace("\\", "/")
        if (normalizedPath.startsWith("../") || normalizedPath.contains("/../")) {
            return ValidationResult.Invalid("Path escapes plugin directory: $path")
        }
        
        // Reject suspicious patterns
        if (path.contains("~") || path.contains("\$")) {
            return ValidationResult.Invalid("Suspicious characters in path: $path")
        }
        
        return ValidationResult.Valid
    }
    
    /**
     * Validates network request URL for security.
     * Ensures URL uses safe protocols and doesn't access restricted hosts.
     * @param url The URL to validate
     * @param allowLocalhost Whether to allow localhost/127.0.0.1 (default: false)
     * @return ValidationResult indicating if URL is safe
     */
    fun validateNetworkRequest(url: String, allowLocalhost: Boolean = false): ValidationResult {
        // Ensure URL uses http or https
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return ValidationResult.Invalid("Only http:// and https:// protocols are allowed: $url")
        }
        
        // Reject file:// protocol
        if (url.startsWith("file://")) {
            return ValidationResult.Invalid("file:// protocol is not allowed: $url")
        }
        
        // Check for localhost/127.0.0.1 if not allowed
        if (!allowLocalhost) {
            val lowerUrl = url.lowercase()
            if (lowerUrl.contains("localhost") || 
                lowerUrl.contains("127.0.0.1") ||
                lowerUrl.contains("0.0.0.0") ||
                lowerUrl.contains("[::1]")) {
                return ValidationResult.Invalid("Localhost access is not allowed: $url")
            }
        }
        
        // Reject private IP ranges (basic check)
        if (url.contains("192.168.") || 
            url.contains("10.") || 
            url.contains("172.16.") ||
            url.contains("172.31.")) {
            return ValidationResult.Invalid("Private IP addresses are not allowed: $url")
        }
        
        return ValidationResult.Valid
    }
}
