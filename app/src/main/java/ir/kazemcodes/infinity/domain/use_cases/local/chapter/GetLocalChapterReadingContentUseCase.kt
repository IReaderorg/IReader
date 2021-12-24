package ir.kazemcodes.infinity.domain.use_cases.local.chapter

import ir.kazemcodes.infinity.domain.models.Chapter
import ir.kazemcodes.infinity.domain.repository.Repository
import ir.kazemcodes.infinity.domain.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class GetLocalChapterReadingContentUseCase @Inject constructor(
    private val repository: Repository
) {

    operator fun invoke(chapter: Chapter): Flow<Resource<Chapter?>> =
        flow {
            try {
                Timber.d("Timber: GetLocalChapterReadingContentUseCase was Called")
                emit(Resource.Loading())
                repository.localChapterRepository.getChapterByChapter(chapterTitle = chapter.title , bookName = chapter.bookName?:"")
                    .collect { chapter ->
                            emit(Resource.Success<Chapter?>(data = chapter?.toChapter()))
                    }
                Timber.d("GetLocalChapterReadingContentUseCase was Finished Successfully")
            } catch (e: Exception) {
                emit(Resource.Error<Chapter?>(message = e.message.toString()))
            }
        }


}