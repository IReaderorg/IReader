package ireader.domain.services.tts_service.piper

import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile

/**
 * Sanitizes and validates user inputs for security.
 * 
 * This class provides comprehensive input validation and sanitization to prevent:
 * - Path traversal attacks
 * - Injection attacks
 * - Resource exhaustion (DoS)
 * - Invalid or malicious input
 * 
 * All user-provided inputs should be validated through this sanitizer before
 * being used in file operations, native calls, or other sensitive operations.
 * 
 * Usage:
 * ```kotlin
 * val sanitizer = InputSanitizer()
 * 
 * // Sanitize text input
 * val safeText = sanitizer.sanitizeText(userInput)
 * 
 * // Validate file path
 * val result = sanitizer.validateFilePath(modelPath, allowedExtensions = listOf("onnx"))
 * if (result.isValid) {
 *     // Safe to use the path
 * }
 * ```
 * 
 * @see SecurityManager
 * @see LibraryVerifier
 */
class InputSanitizer {
    
    /**
     * Result of input validation.
     */
    data class ValidationResult(
        val isValid: Boolean,
        val sanitizedValue: String? = null,
        val errorMessage: String? = null,
        val warnings: List<String> = emptyList()
    ) {
        val isSuccess: Boolean get() = isValid && errorMessage == null
    }
    
    companion object {
        // Maximum text length for synthesis (prevents DoS)
        const val MAX_TEXT_LENGTH = 100_000 // 100k characters
        
        // Maximum file size for model files (prevents DoS)
        const val MAX_MODEL_FILE_SIZE = 500L * 1024 * 1024 // 500 MB
        
        // Maximum config file size
        const val MAX_CONFIG_FILE_SIZE = 10L * 1024 * 1024 // 10 MB
        
        // Allowed model file extensions
        val ALLOWED_MODEL_EXTENSIONS = setOf("onnx")
        
        // Allowed config file extensions
        val ALLOWED_CONFIG_EXTENSIONS = setOf("json", "yaml", "yml")
        
        // Dangerous characters that should be removed from text
        private val CONTROL_CHARACTERS_REGEX = Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]")
        
        // Path traversal patterns to detect
        private val PATH_TRAVERSAL_PATTERNS = listOf(
            "..",
            "~",
            "\$",
            "%",
            "\\\\",
            "//",
            "file://",
            "http://",
            "https://"
        )
    }
    
    /**
     * Sanitize text input for synthesis.
     * 
     * Removes:
     * - Control characters (except newlines and tabs)
     * - Null bytes
     * - Other potentially dangerous characters
     * 
     * Also enforces maximum length to prevent DoS attacks.
     * 
     * @param text The text to sanitize
     * @param maxLength Maximum allowed length (default: MAX_TEXT_LENGTH)
     * @return Sanitized text
     * @throws IllegalArgumentException if text is empty after sanitization
     */
    fun sanitizeText(text: String, maxLength: Int = MAX_TEXT_LENGTH): String {
        if (text.isEmpty()) {
            throw IllegalArgumentException("Text cannot be empty")
        }
        
        // Remove control characters (except newline, carriage return, and tab)
        var sanitized = text.replace(CONTROL_CHARACTERS_REGEX, "")
        
        // Trim whitespace
        sanitized = sanitized.trim()
        
        // Enforce maximum length
        if (sanitized.length > maxLength) {
            sanitized = sanitized.take(maxLength)
        }
        
        if (sanitized.isEmpty()) {
            throw IllegalArgumentException("Text is empty after sanitization")
        }
        
        return sanitized
    }
    
    /**
     * Validate text input and return detailed result.
     * 
     * @param text The text to validate
     * @param maxLength Maximum allowed length
     * @return ValidationResult with sanitized text or error
     */
    fun validateText(text: String, maxLength: Int = MAX_TEXT_LENGTH): ValidationResult {
        val warnings = mutableListOf<String>()
        
        try {
            if (text.isEmpty()) {
                return ValidationResult(
                    isValid = false,
                    errorMessage = "Text cannot be empty"
                )
            }
            
            // Check for control characters
            if (CONTROL_CHARACTERS_REGEX.containsMatchIn(text)) {
                warnings.add("Text contains control characters that will be removed")
            }
            
            // Check length
            if (text.length > maxLength) {
                warnings.add("Text exceeds maximum length and will be truncated to $maxLength characters")
            }
            
            val sanitized = sanitizeText(text, maxLength)
            
            return ValidationResult(
                isValid = true,
                sanitizedValue = sanitized,
                warnings = warnings
            )
            
        } catch (e: IllegalArgumentException) {
            return ValidationResult(
                isValid = false,
                errorMessage = e.message
            )
        }
    }
    
    /**
     * Validate a file path for security.
     * 
     * Checks for:
     * - Path traversal attempts
     * - File existence
     * - File type (regular file vs directory)
     * - File extension
     * - File size
     * - Readable permissions
     * 
     * @param path The file path to validate
     * @param allowedExtensions Set of allowed file extensions (without dot)
     * @param maxSize Maximum allowed file size in bytes
     * @param mustExist If true, file must exist
     * @return ValidationResult indicating if path is safe to use
     */
    fun validateFilePath(
        path: String,
        allowedExtensions: Set<String> = emptySet(),
        maxSize: Long = MAX_MODEL_FILE_SIZE,
        mustExist: Boolean = true
    ): ValidationResult {
        val warnings = mutableListOf<String>()
        
        try {
            // Check for empty path
            if (path.isBlank()) {
                return ValidationResult(
                    isValid = false,
                    errorMessage = "File path cannot be empty"
                )
            }
            
            // Check for path traversal patterns
            val pathLower = path.lowercase()
            for (pattern in PATH_TRAVERSAL_PATTERNS) {
                if (pathLower.contains(pattern)) {
                    return ValidationResult(
                        isValid = false,
                        errorMessage = "Path contains potentially dangerous pattern: $pattern"
                    )
                }
            }
            
            // Convert to Path object
            val filePath = try {
                File(path).toPath().absolute()
            } catch (e: Exception) {
                return ValidationResult(
                    isValid = false,
                    errorMessage = "Invalid file path: ${e.message}"
                )
            }
            
            // Check if file exists (if required)
            if (mustExist && !filePath.exists()) {
                return ValidationResult(
                    isValid = false,
                    errorMessage = "File does not exist: $path"
                )
            }
            
            if (filePath.exists()) {
                // Check if it's a regular file (not a directory or special file)
                if (!filePath.isRegularFile()) {
                    return ValidationResult(
                        isValid = false,
                        errorMessage = "Path is not a regular file: $path"
                    )
                }
                
                // Check if file is readable
                if (!filePath.toFile().canRead()) {
                    return ValidationResult(
                        isValid = false,
                        errorMessage = "File is not readable: $path"
                    )
                }
                
                // Check file size
                val fileSize = filePath.fileSize()
                if (fileSize > maxSize) {
                    return ValidationResult(
                        isValid = false,
                        errorMessage = "File size ($fileSize bytes) exceeds maximum allowed size ($maxSize bytes)"
                    )
                }
                
                if (fileSize == 0L) {
                    return ValidationResult(
                        isValid = false,
                        errorMessage = "File is empty: $path"
                    )
                }
            }
            
            // Check file extension
            if (allowedExtensions.isNotEmpty()) {
                val extension = filePath.extension.lowercase()
                if (extension !in allowedExtensions) {
                    return ValidationResult(
                        isValid = false,
                        errorMessage = "Invalid file extension: $extension (allowed: ${allowedExtensions.joinToString(", ")})"
                    )
                }
            }
            
            return ValidationResult(
                isValid = true,
                sanitizedValue = filePath.toString(),
                warnings = warnings
            )
            
        } catch (e: Exception) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Path validation error: ${e.message}"
            )
        }
    }
    
    /**
     * Validate a model file path.
     * 
     * Convenience method that validates with model-specific constraints.
     * 
     * @param modelPath Path to the model file
     * @return ValidationResult
     */
    fun validateModelPath(modelPath: String): ValidationResult {
        return validateFilePath(
            path = modelPath,
            allowedExtensions = ALLOWED_MODEL_EXTENSIONS,
            maxSize = MAX_MODEL_FILE_SIZE,
            mustExist = true
        )
    }
    
    /**
     * Validate a config file path.
     * 
     * Convenience method that validates with config-specific constraints.
     * 
     * @param configPath Path to the config file
     * @return ValidationResult
     */
    fun validateConfigPath(configPath: String): ValidationResult {
        return validateFilePath(
            path = configPath,
            allowedExtensions = ALLOWED_CONFIG_EXTENSIONS,
            maxSize = MAX_CONFIG_FILE_SIZE,
            mustExist = true
        )
    }
    
    /**
     * Validate a directory path.
     * 
     * Ensures the path points to a valid, accessible directory.
     * 
     * @param path The directory path to validate
     * @param mustExist If true, directory must exist
     * @param mustBeWritable If true, directory must be writable
     * @return ValidationResult
     */
    fun validateDirectoryPath(
        path: String,
        mustExist: Boolean = true,
        mustBeWritable: Boolean = false
    ): ValidationResult {
        val warnings = mutableListOf<String>()
        
        try {
            if (path.isBlank()) {
                return ValidationResult(
                    isValid = false,
                    errorMessage = "Directory path cannot be empty"
                )
            }
            
            // Check for path traversal patterns
            val pathLower = path.lowercase()
            for (pattern in PATH_TRAVERSAL_PATTERNS) {
                if (pathLower.contains(pattern)) {
                    return ValidationResult(
                        isValid = false,
                        errorMessage = "Path contains potentially dangerous pattern: $pattern"
                    )
                }
            }
            
            val dirPath = try {
                File(path).toPath().absolute()
            } catch (e: Exception) {
                return ValidationResult(
                    isValid = false,
                    errorMessage = "Invalid directory path: ${e.message}"
                )
            }
            
            if (mustExist && !dirPath.exists()) {
                return ValidationResult(
                    isValid = false,
                    errorMessage = "Directory does not exist: $path"
                )
            }
            
            if (dirPath.exists()) {
                if (!dirPath.isDirectory()) {
                    return ValidationResult(
                        isValid = false,
                        errorMessage = "Path is not a directory: $path"
                    )
                }
                
                if (!dirPath.toFile().canRead()) {
                    return ValidationResult(
                        isValid = false,
                        errorMessage = "Directory is not readable: $path"
                    )
                }
                
                if (mustBeWritable && !dirPath.toFile().canWrite()) {
                    return ValidationResult(
                        isValid = false,
                        errorMessage = "Directory is not writable: $path"
                    )
                }
            }
            
            return ValidationResult(
                isValid = true,
                sanitizedValue = dirPath.toString(),
                warnings = warnings
            )
            
        } catch (e: Exception) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Directory validation error: ${e.message}"
            )
        }
    }
    
    /**
     * Validate a parameter value is within a valid range.
     * 
     * @param value The value to validate
     * @param min Minimum valid value (inclusive)
     * @param max Maximum valid value (inclusive)
     * @param paramName Name of the parameter (for error messages)
     * @return ValidationResult
     */
    fun validateRange(
        value: Float,
        min: Float,
        max: Float,
        paramName: String
    ): ValidationResult {
        return if (value in min..max) {
            ValidationResult(
                isValid = true,
                sanitizedValue = value.toString()
            )
        } else {
            ValidationResult(
                isValid = false,
                errorMessage = "$paramName must be between $min and $max, got $value"
            )
        }
    }
    
    /**
     * Sanitize a filename by removing dangerous characters.
     * 
     * Removes:
     * - Path separators
     * - Special characters
     * - Control characters
     * 
     * @param filename The filename to sanitize
     * @return Sanitized filename
     */
    fun sanitizeFilename(filename: String): String {
        if (filename.isBlank()) {
            throw IllegalArgumentException("Filename cannot be empty")
        }
        
        // Remove path separators and dangerous characters
        var sanitized = filename
            .replace(Regex("[/\\\\:*?\"<>|]"), "_")
            .replace(CONTROL_CHARACTERS_REGEX, "")
            .trim()
        
        // Remove leading/trailing dots (hidden files on Unix)
        sanitized = sanitized.trim('.')
        
        // Ensure filename is not empty after sanitization
        if (sanitized.isEmpty()) {
            throw IllegalArgumentException("Filename is empty after sanitization")
        }
        
        // Limit length
        if (sanitized.length > 255) {
            sanitized = sanitized.take(255)
        }
        
        return sanitized
    }
    
    /**
     * Check if a path is within an allowed directory (prevents path traversal).
     * 
     * @param path The path to check
     * @param allowedDirectory The directory that the path must be within
     * @return true if path is within allowed directory, false otherwise
     */
    fun isPathWithinDirectory(path: Path, allowedDirectory: Path): Boolean {
        return try {
            val normalizedPath = path.absolute().normalize()
            val normalizedAllowed = allowedDirectory.absolute().normalize()
            normalizedPath.startsWith(normalizedAllowed)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Validate and sanitize a URL string.
     * 
     * Ensures the URL is well-formed and uses allowed protocols.
     * 
     * @param url The URL to validate
     * @param allowedProtocols Set of allowed protocols (e.g., "https", "http")
     * @return ValidationResult
     */
    fun validateUrl(
        url: String,
        allowedProtocols: Set<String> = setOf("https")
    ): ValidationResult {
        try {
            if (url.isBlank()) {
                return ValidationResult(
                    isValid = false,
                    errorMessage = "URL cannot be empty"
                )
            }
            
            val urlObj = java.net.URL(url)
            val protocol = urlObj.protocol.lowercase()
            
            if (protocol !in allowedProtocols) {
                return ValidationResult(
                    isValid = false,
                    errorMessage = "Protocol '$protocol' not allowed (allowed: ${allowedProtocols.joinToString(", ")})"
                )
            }
            
            return ValidationResult(
                isValid = true,
                sanitizedValue = url
            )
            
        } catch (e: Exception) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Invalid URL: ${e.message}"
            )
        }
    }
}
