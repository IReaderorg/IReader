package ir.kazemcodes.infinity.domain.use_cases.local.chapter

import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.domain.models.remote.Chapter
import ir.kazemcodes.infinity.domain.repository.Repository
import ir.kazemcodes.infinity.util.InvalidBookException
import timber.log.Timber
import javax.inject.Inject

class InsertLocalChaptersUseCase @Inject constructor(
    private val repository: Repository,
) {

    @Throws(InvalidBookException::class)
    suspend operator fun invoke(chapters: List<Chapter>,book :Book,inLibrary : Boolean, source : Source) {
        try {
            Timber.d("Timber: InsertLocalChaptersUseCase was Called")
            repository.localChapterRepository.insertChapters(chapterEntity = chapters.map { it.copy(bookName = book.bookName,source=source.name, inLibrary = inLibrary).toChapterEntity() })
            Timber.d("Timber: InsertLocalChaptersUseCase was Finished Successfully")
        }catch (e : Exception) {
            Timber.e("Timber: InsertLocalChaptersUseCase: ${e.localizedMessage}")
        }

    }

}
class SetLastReadChaptersUseCase @Inject constructor(
    private val repository: Repository,
) {

    @Throws(InvalidBookException::class)
    suspend operator fun invoke(book :Book,chapterTitle : String, source : Source) {
        Timber.d("Timber: SetLastReadChaptersUseCase was Called")
        repository.localChapterRepository.setLastReadChapter(bookName = book.bookName,source=source.name, chapterTitle = chapterTitle)
        Timber.d("Timber: SetLastReadChaptersUseCase was Finished Successfully")
    }

}

