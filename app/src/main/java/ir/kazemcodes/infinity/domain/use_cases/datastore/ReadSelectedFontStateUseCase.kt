package ir.kazemcodes.infinity.domain.use_cases.datastore

import ir.kazemcodes.infinity.domain.models.FontType
import ir.kazemcodes.infinity.domain.repository.Repository
import ir.kazemcodes.infinity.presentation.theme.fonts
import ir.kazemcodes.infinity.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class ReadSelectedFontStateUseCase(
    private val repository: Repository,
) {
    operator fun invoke(): Flow<Resource<FontType>> = flow {
        try {
            Timber.d("Timber: ReadSelectedFontStateUseCase was Called")
            emit(Resource.Loading<FontType>())
            repository.dataStoreRepository.readSelectedFontState().collect { fontIndex ->
                emit(Resource.Success<FontType>(fonts[fontIndex]))
            }
            Timber.d("Timber: ReadSelectedFontStateUseCase was Finished Successfully")
        } catch (e: Exception) {
            Timber.e("ReadSelectedFontStateUseCase: " + e.localizedMessage)
            emit(Resource.Error<FontType>(message = e.message.toString()))
        }
    }
}