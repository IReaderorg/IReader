package ir.kazemcodes.infinity.domain.use_cases.local.chapter

import ir.kazemcodes.infinity.domain.models.remote.Chapter
import ir.kazemcodes.infinity.domain.repository.Repository
import ir.kazemcodes.infinity.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class GetLocalChapterReadingContentUseCase @Inject constructor(
    private val repository: Repository,
) {

    operator fun invoke(chapter: Chapter): Flow<Resource<Chapter?>> =
        flow {
            try {
                emit(Resource.Loading())
                Timber.d("Timber: GetLocalChapterReadingContentUseCase was Called")
                repository.localChapterRepository.getChapterByChapter(chapterTitle = chapter.title,
                    bookName = chapter.bookName ?: "")
                    .first { chapter ->
                        emit(Resource.Success<Chapter?>(data = chapter?.toChapter()))
                        return@first chapter != null
                    }
                Timber.d("GetLocalChapterReadingContentUseCase was Finished Successfully")
            } catch (e: Exception) {
                emit(Resource.Error<Chapter?>(message = e.message.toString()))
            }
        }


}