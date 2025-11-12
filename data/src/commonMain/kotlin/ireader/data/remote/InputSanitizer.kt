package ireader.data.remote

/**
 * Utility for sanitizing user inputs to prevent injection attacks and ensure data quality
 * 
 * Requirements: 11.3
 */
object InputSanitizer {
    
    // Username constraints
    private const val MIN_USERNAME_LENGTH = 3
    private const val MAX_USERNAME_LENGTH = 30
    private val USERNAME_PATTERN = Regex("^[a-zA-Z0-9_-]+$")
    
    /**
     * Sanitizes and validates a username
     * 
     * Rules:
     * - Length between 3 and 30 characters
     * - Only alphanumeric characters, underscores, and hyphens
     * - No leading or trailing whitespace
     * 
     * @param username The username to sanitize
     * @return Sanitized username
     * @throws IllegalArgumentException if username is invalid
     */
    fun sanitizeUsername(username: String): String {
        val trimmed = username.trim()
        
        if (trimmed.length < MIN_USERNAME_LENGTH) {
            throw IllegalArgumentException("Username must be at least $MIN_USERNAME_LENGTH characters")
        }
        
        if (trimmed.length > MAX_USERNAME_LENGTH) {
            throw IllegalArgumentException("Username must be at most $MAX_USERNAME_LENGTH characters")
        }
        
        if (!USERNAME_PATTERN.matches(trimmed)) {
            throw IllegalArgumentException("Username can only contain letters, numbers, underscores, and hyphens")
        }
        
        return trimmed
    }
    
    /**
     * Sanitizes a book ID by removing potentially dangerous characters
     * 
     * @param bookId The book ID to sanitize
     * @return Sanitized book ID
     */
    fun sanitizeBookId(bookId: String): String {
        return bookId.trim()
            .replace(Regex("[^a-z0-9-]"), "")
            .take(200) // Limit length
    }
    
    /**
     * Sanitizes a chapter slug by removing potentially dangerous characters
     * 
     * @param chapterSlug The chapter slug to sanitize
     * @return Sanitized chapter slug
     */
    fun sanitizeChapterSlug(chapterSlug: String): String {
        return chapterSlug.trim()
            .replace(Regex("[^a-zA-Z0-9-_]"), "")
            .take(200) // Limit length
    }
    
    /**
     * Validates that a scroll position is within acceptable bounds
     * 
     * @param position The scroll position to validate
     * @return Validated position (clamped to 0.0-1.0)
     */
    fun validateScrollPosition(position: Float): Float {
        return position.coerceIn(0.0f, 1.0f)
    }
}
