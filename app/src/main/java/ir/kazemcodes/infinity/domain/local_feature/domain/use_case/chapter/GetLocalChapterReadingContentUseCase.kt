package ir.kazemcodes.infinity.domain.local_feature.domain.use_case.chapter

import ir.kazemcodes.infinity.domain.repository.Repository
import ir.kazemcodes.infinity.domain.models.Resource
import ir.kazemcodes.infinity.domain.models.Chapter
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
                        if (chapter!= null) {
                            Timber.d("Timber: GetLocalChapterReadingContentUseCase $chapter")
                            emit(Resource.Success<Chapter?>(data = chapter.toChapter()))
                        } else {
                            emit(Resource.Success<Chapter?>(data = null))
                        }

                    }
                Timber.d("GetLocalChapterReadingContentUseCase was Finished Successfully")
            } catch (e: Exception) {
                emit(Resource.Error<Chapter?>(message = e.message.toString()))
            }
        }


}