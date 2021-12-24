package ir.kazemcodes.infinity.domain.use_cases.datastore

import ir.kazemcodes.infinity.domain.models.LastReadChapter
import ir.kazemcodes.infinity.domain.repository.Repository
import ir.kazemcodes.infinity.domain.utils.Resource
import ir.kazemcodes.infinity.explore_feature.domain.repository.moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class ReadLatestChapterUseCase(
    private val repository: Repository,
) {
    operator fun invoke(): Flow<Resource<List<LastReadChapter>>> = flow {
        try {
            Timber.d("Timber: ReadFontSizeState was Called")
            emit(Resource.Loading<List<LastReadChapter>>())
            repository.dataStoreRepository.readLatestChapterUseCase().collect { latestRead ->
                kotlin.runCatching {
                val latest = moshi.adapter<List<LastReadChapter>>(LastReadChapter::class.java).fromJson(latestRead)
                emit(Resource.Success<List<LastReadChapter>>(latest?: emptyList()))
                }
            }
            Timber.d("Timber: ReadFontSizeState was Finished Successfully")
        } catch (e: Exception) {
            Timber.e("ReadFontSizeState: " + e.localizedMessage)
            emit(Resource.Error<List<LastReadChapter>>(message = e.message.toString()))
        }
    }
}