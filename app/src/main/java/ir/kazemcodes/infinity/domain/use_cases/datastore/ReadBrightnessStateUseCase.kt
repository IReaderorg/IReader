package ir.kazemcodes.infinity.domain.use_cases.datastore

import ir.kazemcodes.infinity.domain.repository.Repository
import ir.kazemcodes.infinity.domain.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class ReadBrightnessStateUseCase(
    private val repository: Repository,
) {
    operator fun invoke(): Flow<Resource<Float>> = flow {
        try {
            Timber.d("Timber: ReadFontSizeState was Called")
            emit(Resource.Loading<Float>())
            repository.dataStoreRepository.readBrightnessState().collect { brightness ->
                emit(Resource.Success<Float>(brightness))
            }
            Timber.d("Timber: ReadFontSizeState was Finished Successfully")
        } catch (e: Exception) {
            Timber.e("ReadFontSizeState: " + e.localizedMessage)
            emit(Resource.Error<Float>(message = e.message.toString()))
        }
    }
}