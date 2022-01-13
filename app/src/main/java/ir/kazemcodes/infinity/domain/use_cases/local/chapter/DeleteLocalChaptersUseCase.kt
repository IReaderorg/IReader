package ir.kazemcodes.infinity.domain.use_cases.local.chapter

import ir.kazemcodes.infinity.domain.repository.Repository
import timber.log.Timber
import javax.inject.Inject

class DeleteLocalChaptersUseCase @Inject constructor(
    private val repository: Repository,
) {

    suspend operator fun invoke(bookName: String, source: String) {
        Timber.d("Timber: GetLocalBookByNameUseCase was Called")
        repository.localChapterRepository.deleteChapters(bookName, source)
        Timber.d("Timber: GetLocalBookByNameUseCase was Finished Successfully")
    }
}

class DeleteAllLocalChaptersUseCase @Inject constructor(
    private val repository: Repository,
) {

    suspend operator fun invoke() {
        Timber.d("Timber: GetLocalBookByNameUseCase was Called")
        repository.localChapterRepository.deleteAllChapters()
        Timber.d("Timber: GetLocalBookByNameUseCase was Finished Successfully")
    }
}

class DeleteNotInLibraryLocalChaptersUseCase @Inject constructor(
    private val repository: Repository,
) {

    suspend operator fun invoke() {
        Timber.d("Timber: GetLocalBookByNameUseCase was Called")
        repository.localChapterRepository.deleteNotInLibraryChapters()
        Timber.d("Timber: GetLocalBookByNameUseCase was Finished Successfully")
    }
}

class DeleteLastReadChapterChaptersUseCase @Inject constructor(
    private val repository: Repository,
) {

    suspend operator fun invoke(
        bookName: String,
        source: String,
    ) {
        Timber.d("Timber: GetLocalBookByNameUseCase was Called")
        repository.localChapterRepository.deleteLastReadChapter(
            bookName,
            source)
        Timber.d("Timber: GetLocalBookByNameUseCase was Finished Successfully")
    }
}