package ir.kazemcodes.infinity.domain.use_cases.local.chapter

import ir.kazemcodes.infinity.domain.models.remote.Chapter
import ir.kazemcodes.infinity.domain.repository.Repository
import ir.kazemcodes.infinity.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class GetLocalChaptersByBookNameUseCase @Inject constructor(
    private val repository: Repository,
) {
    operator fun invoke(bookName: String,source: String): Flow<Resource<List<Chapter>>> = flow {
            try {
                Timber.d("Timber: GetLocalChaptersByBookNameUseCase was Called")
                emit(Resource.Loading())
                repository.localChapterRepository.getChapterByName(bookName = bookName,source)
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

class GetLocalAllChaptersUseCase @Inject constructor(
    private val repository: Repository,
) {
    operator fun invoke(): Flow<Resource<List<Chapter>>> = flow {
        try {
            Timber.d("Timber: GetLocalChaptersByBookNameUseCase was Called")
            emit(Resource.Loading())
            repository.localChapterRepository.getAllChapter()
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

class GetLastReadChapterUseCase @Inject constructor(
    private val repository: Repository,
) {
    operator fun invoke(bookName: String,source: String): Flow<Resource<Chapter>> = flow {
        try {
            Timber.d("Timber: GetLocalChaptersByBookNameUseCase was Called")
            emit(Resource.Loading())
            repository.localChapterRepository.getLastReadChapter(bookName, source).collect { chapter ->
                    emit(Resource.Success<Chapter>(data = chapter.toChapter()))
                }
            Timber.d("Timber: GetLocalChaptersByBookNameUseCase was Finished Successfully")
        } catch (e: Exception) {
            emit(Resource.Error<Chapter>(message = e.message.toString()))
        }
    }
}