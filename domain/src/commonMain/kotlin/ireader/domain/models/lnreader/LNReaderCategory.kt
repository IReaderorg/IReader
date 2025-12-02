package ireader.domain.models.lnreader

import kotlinx.serialization.Serializable

/**
 * Represents a category from LNReader backup
 * Maps to LNReader's BackupCategory
 */
@Serializable
data class LNReaderCategory(
    val id: Int,
    val name: String,
    val sort: Int,
    val novelIds: List<Int> = emptyList()
)
