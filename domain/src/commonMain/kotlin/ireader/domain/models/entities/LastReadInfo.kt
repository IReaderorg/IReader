package ireader.domain.models.entities

/**
 * Data model containing information about the last read chapter
 * Used for the Resume Reading feature
 */
data class LastReadInfo(
    val novelId: Long,
    val novelTitle: String,
    val coverUrl: String,
    val chapterId: Long,
    val chapterNumber: Float,
    val chapterTitle: String,
    val progressPercent: Float,
    val scrollPosition: Long,
    val lastReadAt: Long
)
