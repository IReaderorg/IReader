package ireader.domain.models.quote

/**
 * Constants for quote card generation across all platforms.
 * Centralizes magic numbers to improve maintainability and consistency.
 */
object QuoteCardConstants {
    
    // Image dimensions (Instagram story format)
    const val IMAGE_WIDTH = 1080
    const val IMAGE_HEIGHT = 1920
    
    // Text sizes (in platform-specific units)
    const val LOGO_TEXT_SIZE = 56f
    const val QUOTE_MARK_SIZE = 48f
    const val BOOK_TITLE_SIZE = 48f
    const val QUOTE_TEXT_SIZE = 42f
    const val AUTHOR_TEXT_SIZE = 40f
    
    // Vertical positioning offsets (relative to centerY)
    const val LOGO_OFFSET_Y = -400f
    const val QUOTE_MARK_OFFSET_Y = -300f
    const val BOOK_TITLE_OFFSET_Y = 250f
    const val AUTHOR_OFFSET_Y = 310f
    
    // Validation constraints
    const val MIN_QUOTE_LENGTH = 10
    
    // Rate limiting (milliseconds)
    const val SHARE_RATE_LIMIT_MS = 30_000L // 30 seconds between shares
    
    // Layout constants
    const val HORIZONTAL_MARGIN = 80
    const val LINE_HEIGHT = 70
}
