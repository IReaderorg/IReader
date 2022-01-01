package ir.kazemcodes.infinity.domain.use_cases.datastore

import ir.kazemcodes.infinity.domain.repository.Repository
import ir.kazemcodes.infinity.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class ReadDohPrefUseCase(
    private val repository: Repository,
) {
    suspend operator fun invoke(): Flow<Resource<Int>> = flow {
        try {
            Timber.d("Timber: ReadFontSizeState was Called")
            emit(Resource.Loading<Int>())
            repository.dataStoreRepository.readDohPrefUseCase().collect { code ->
                emit(Resource.Success<Int>(code))
            }
            Timber.d("Timber: ReadFontSizeState was Finished Successfully")
        } catch (e: Exception) {
            Timber.e("ReadFontSizeState: " + e.localizedMessage)
            emit(Resource.Error<Int>(message = e.message.toString()))
        }
    }
}