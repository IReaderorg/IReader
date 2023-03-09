

package ireader.domain.models.entities

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter

data class Download(
    val chapterId: Long,
    val bookId: Long,
    val priority: Int,
)

data class SavedDownload(
    val chapterId: Long,
    val bookId: Long,
    val priority: Int,
    val bookName: String,
    val chapterKey: String,
    val chapterName: String,
    val translator: String
) {
    fun toDownload(): Download {
        return Download(
            chapterId = this.chapterId,
            bookId = this.bookId,
            priority = this.priority
        )
    }
}

data class SavedDownloadWithInfo(
    val chapterId: Long,
    val bookId: Long,
    val priority: Int = 0,
    val id: Long,
    val sourceId: Long,
    val bookName: String,
    val chapterKey: String,
    val chapterName: String,
    val translator: String,
    val isDownloaded: Boolean
) {
    fun toDownload(): Download {
        return Download(
            chapterId = this.chapterId,
            bookId = this.bookId,
            priority = this.priority
        )
    }
}

fun SavedDownloadWithInfo.toSavedDownload(): SavedDownload {
    return SavedDownload(
        bookId = bookId,
        priority = 1,
        chapterName = chapterName,
        chapterKey = chapterKey,
        translator = translator,
        chapterId = chapterId,
        bookName = bookName,
    )
}

fun buildSavedDownload(book: Book, chapter: Chapter): SavedDownload {
    return SavedDownload(
        bookId = book.id,
        priority = 1,
        chapterName = chapter.name,
        chapterKey = chapter.key,
        translator = chapter.translator,
        chapterId = chapter.id,
        bookName = book.title,
    )
}
