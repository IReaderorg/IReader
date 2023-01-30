package ireader.domain.usecases.local.chapter_usecases

import ireader.domain.data.repository.ChapterRepository
import kotlinx.coroutines.flow.Flow
import ireader.domain.models.entities.Chapter
import kotlinx.coroutines.flow.map


/**
 * get all Chapter using a bookId
 * note: if nothing is found it return a resource of error
 */
class SubscribeChaptersByBookId(private val chapterRepository: ChapterRepository) {
    operator fun invoke(
        bookId: Long,
        sort: String = "default",
    ): Flow<List<Chapter>> {
        return chapterRepository.subscribeChaptersByBookId(bookId = bookId).map {
            it.sort(sort)
        }
    }
}

private fun List<Chapter>.sort(sort:String): List<Chapter> {
   return when(sort) {
        "default" -> this
        "by_name" -> this.sortedBy { it.name }
        "by_source" -> this.sortedBy { it.sourceOrder }
        "by_chapter_number" -> this.sortedBy { it.number }
        "date_fetched" -> this.sortedBy { it.dateFetch }
        "date_upload" -> this.sortedBy { it.dateUpload }
        "bookmark" -> this.sortedBy { it.bookmark }
        else ->this
    }.let {
        if (sort.endsWith("Desc")) it.reversed() else it
   }
}


class FindChaptersByBookId(private val chapterRepository: ChapterRepository) {
    suspend operator fun invoke(
        bookId: Long,
    ): List<Chapter> {
        return chapterRepository.findChaptersByBookId(bookId = bookId)
    }
}
