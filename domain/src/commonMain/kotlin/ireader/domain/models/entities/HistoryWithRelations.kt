

package ireader.domain.models.entities

import ireader.domain.models.BookCover

data class HistoryWithRelations(
    val id: Long,
    val chapterId: Long,
    val bookId: Long,
    val title: String,
    val chapterName:String,
    val chapterNumber: Float,
    val readAt: Long = -1,
    val readDuration: Long,
    val coverData: BookCover,
    val progress: Float? = null
)

