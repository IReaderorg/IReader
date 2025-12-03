package ireader.domain.models.fonts

import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Represents a custom font that can be imported and used in the reader
 */
data class CustomFont(
    val id: String,
    val name: String,
    val filePath: String,
    val isSystemFont: Boolean = false,
    val dateAdded: Long = currentTimeToLong()
)
