package ir.kazemcodes.infinity.library_feature.domain.use_case.chapter

import ir.kazemcodes.infinity.base_feature.repository.Repository
import ir.kazemcodes.infinity.core.Resource
import ir.kazemcodes.infinity.explore_feature.data.model.Chapter
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetLocalChaptersByBookNameUseCase @Inject constructor(
    private val repository: Repository
) {

    operator fun invoke(bookName : String): kotlinx.coroutines.flow.Flow<Resource<List<Chapter>>> =
        flow {
            try {
                emit(Resource.Loading())

                val chapters = repository.localChapterRepository.getChapterByName(bookName = bookName)
                        emit(Resource.Success<List<Chapter>>(data = chapters.map { it.copy(bookName = bookName).toChapter() }))

            } catch (e: Exception) {
                emit(Resource.Error<List<Chapter>>(message = e.message.toString()))
            }
        }


}