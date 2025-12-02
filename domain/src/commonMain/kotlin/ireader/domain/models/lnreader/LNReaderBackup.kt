package ireader.domain.models.lnreader

import kotlinx.serialization.Serializable

/**
 * Represents a complete LNReader backup containing all data
 */
@Serializable
data class LNReaderBackup(
    val version: LNReaderVersion,
    val novels: List<LNReaderNovel> = emptyList(),
    val categories: List<LNReaderCategory> = emptyList(),
    val settings: Map<String, String> = emptyMap()
)

/**
 * LNReader backup version information
 */
@Serializable
data class LNReaderVersion(
    val version: String
)
