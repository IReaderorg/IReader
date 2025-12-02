package ireader.domain.models.lnreader

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a novel from LNReader backup
 * Maps to LNReader's NovelInfo + chapters
 * 
 * Note: LNReader uses 0/1 integers for boolean fields (inLibrary, isLocal)
 * instead of true/false, so we use Int and provide helper properties.
 */
@Serializable
data class LNReaderNovel(
    val id: Int,
    val path: String,
    val pluginId: String,
    val name: String,
    val cover: String? = null,
    val summary: String? = null,
    val author: String? = null,
    val artist: String? = null,
    val status: String? = null,
    val genres: String? = null,
    // LNReader uses 0/1 for booleans
    @SerialName("inLibrary")
    val inLibraryInt: Int = 0,
    @SerialName("isLocal")
    val isLocalInt: Int = 0,
    val totalPages: Int = 0,
    val chapters: List<LNReaderChapter> = emptyList()
) {
    /** Whether this novel is in the library */
    val inLibrary: Boolean get() = inLibraryInt != 0
    
    /** Whether this novel is local */
    val isLocal: Boolean get() = isLocalInt != 0
}
