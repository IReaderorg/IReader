package ireader.domain.models.common

/**
 * Domain representation of alignment.
 * This is independent of any UI framework.
 */
enum class AlignmentModel {
    TOP_START,
    TOP_CENTER,
    TOP_END,
    CENTER_START,
    CENTER,
    CENTER_END,
    BOTTOM_START,
    BOTTOM_CENTER,
    BOTTOM_END
}

/**
 * Domain representation of text alignment.
 */
enum class TextAlignmentModel {
    LEFT,
    CENTER,
    RIGHT,
    JUSTIFY,
    START,
    END
}
