package ir.kazemcodes.infinity.library_feature.domain.use_case.chapter

import ir.kazemcodes.infinity.base_feature.repository.Repository
import ir.kazemcodes.infinity.core.Resource
import ir.kazemcodes.infinity.explore_feature.data.model.Chapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetLocalChapterReadingContent @Inject constructor(
    private val repository: Repository
) {

    operator fun invoke(chapter:Chapter): Flow<Resource<Chapter>> =
        flow {
            try {
                emit(Resource.Loading())
                repository.localChapterRepository.getChapterByChapter(chapterTitle = chapter.title , bookName = chapter.bookName?:"")
                    .collect { chapter ->
                        emit(Resource.Success<Chapter>(data = chapter.toChapter()))
                    }
            } catch (e: Exception) {
                emit(Resource.Error<Chapter>(message = e.message.toString()))
            }
        }


}