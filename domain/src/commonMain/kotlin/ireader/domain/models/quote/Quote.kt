package ireader.domain.models.quote

import kotlinx.serialization.Serializable

/**
 * Available card styles for shareable quote cards
 */
enum class QuoteCardStyle(val displayName: String) {
    GRADIENT_SUNSET("Sunset"),
    GRADIENT_OCEAN("Ocean"),
    GRADIENT_FOREST("Forest"),
    GRADIENT_LAVENDER("Lavender"),
    GRADIENT_MIDNIGHT("Midnight"),
    MINIMAL_LIGHT("Minimal Light"),
    MINIMAL_DARK("Minimal Dark"),
    PAPER_TEXTURE("Paper"),
    BOOK_COVER("Book Cover")
}
