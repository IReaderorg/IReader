package ireader.domain.models.entities

import ireader.domain.models.BookCover


data class UpdatesWithRelations(
    val bookId: Long,
    val bookTitle: String,
    val chapterId: Long,
    val chapterName: String,
    val scanlator: String?,
    val read: Boolean,
    val bookmark: Boolean,
    val sourceId: Long,
    val dateFetch: Long,
    val coverData: BookCover,
    val downloaded: Boolean
) {
    companion object {
        fun UpdatesWithRelations.toUpdate(): Update {
            return Update(
                id = this.chapterId,
                bookId = this.bookId,
                chapterId = this.chapterId,
                date = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
            )
        }
    }
}

data class Update(

    val id: Long = 0,
    val chapterId: Long,
    val bookId: Long,
    val date: Long,
)
