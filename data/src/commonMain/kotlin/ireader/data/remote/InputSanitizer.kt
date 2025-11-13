package ireader.data.remote

/**
 * Sanitizes and validates user input for remote operations
 */
object InputSanitizer {
    
    fun sanitizeUsername(username: String): String {
        return username.trim().take(50)
    }
    
    fun sanitizeBookId(bookId: String): String {
        return bookId.trim().take(255)
    }
    
    fun sanitizeChapterSlug(slug: String): String {
        return slug.trim().take(255)
    }
    
    fun validateScrollPosition(position: Float): Float {
        return position.coerceIn(0f, 1f)
    }
}
