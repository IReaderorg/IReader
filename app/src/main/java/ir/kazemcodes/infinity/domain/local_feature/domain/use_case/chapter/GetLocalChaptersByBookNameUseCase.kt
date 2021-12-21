package ir.kazemcodes.infinity.domain.local_feature.domain.use_case.chapter

import ir.kazemcodes.infinity.domain.models.Chapter
import ir.kazemcodes.infinity.domain.models.Resource
import ir.kazemcodes.infinity.domain.repository.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class GetLocalChaptersByBookNameUseCase @Inject constructor(
    private val repository: Repository
) {
    operator fun invoke(bookName: String): Flow<Resource<List<Chapter>>> =
        flow {
            try {
                Timber.d("Timber: GetLocalChaptersByBookNameUseCase was Called")
                emit(Resource.Loading())
                repository.localChapterRepository.getChapterByName(bookName = bookName)
                    .collect { chapters ->
                        emit(Resource.Success<List<Chapter>>(data = chapters.map { chapterEntity ->
                            chapterEntity.toChapter()
                        }))
                    }
                Timber.d("Timber: GetLocalChaptersByBookNameUseCase was Finished Successfully")
            } catch (e: Exception) {
                emit(Resource.Error<List<Chapter>>(message = e.message.toString()))
            }
        }


}