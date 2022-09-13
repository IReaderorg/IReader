

package ireader.common.models.entities

import ireader.common.models.BookCover
import kotlinx.datetime.LocalDate
import java.util.*

data class HistoryWithRelations(
    val id: Long,
    val chapterId: Long,
    val bookId: Long,
    val title: String,
    val chapterName:String,
    val chapterNumber: Float,
    val readAt: Long?,
    val readDuration: Long,
    val coverData: BookCover,
)

