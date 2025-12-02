package ireader.domain.models.lnreader

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a chapter from LNReader backup
 * Maps to LNReader's ChapterInfo
 * 
 * Note: LNReader uses 0/1 integers for boolean fields (bookmark, unread, isDownloaded)
 * instead of true/false, so we use Int and provide helper properties.
 */
@Serializable
data class LNReaderChapter(
    val id: Int,
    val novelId: Int,
    val path: String,
    val name: String,
    val releaseTime: String? = null,
    val readTime: String? = null,
    // LNReader uses 0/1 for booleans
    @SerialName("bookmark")
    val bookmarkInt: Int = 0,
    @SerialName("unread")
    val unreadInt: Int = 1,
    @SerialName("isDownloaded")
    val isDownloadedInt: Int = 0,
    val progress: Float? = null,
    val chapterNumber: Float? = null,
    val page: String? = null,
    val position: Int? = null,
    val updatedTime: String? = null
) {
    /** Whether this chapter is bookmarked */
    val bookmark: Boolean get() = bookmarkInt != 0
    
    /** Whether this chapter is unread */
    val unread: Boolean get() = unreadInt != 0
    
    /** Whether this chapter is downloaded */
    val isDownloaded: Boolean get() = isDownloadedInt != 0
}
