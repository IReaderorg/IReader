package ireader.domain.models.fonts

/**
 * Represents a custom font that can be imported and used in the reader
 */
data class CustomFont(
    val id: String,
    val name: String,
    val filePath: String,
    val isSystemFont: Boolean = false,
    val dateAdded: Long = System.currentTimeMillis()
)
