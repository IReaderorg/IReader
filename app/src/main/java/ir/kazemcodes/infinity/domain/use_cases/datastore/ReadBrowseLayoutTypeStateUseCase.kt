package ir.kazemcodes.infinity.domain.use_cases.datastore

import ir.kazemcodes.infinity.domain.repository.Repository
import ir.kazemcodes.infinity.util.Resource
import ir.kazemcodes.infinity.presentation.layouts.DisplayMode
import ir.kazemcodes.infinity.presentation.layouts.layouts
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class ReadBrowseLayoutTypeStateUseCase(
    private val repository: Repository,
) {
    operator fun invoke(): Flow<Resource<DisplayMode>> = flow {
        try {
            Timber.d("Timber: ReadFontSizeState was Called")
            emit(Resource.Loading<DisplayMode>())
            repository.dataStoreRepository.readBrowseLayoutTypeStateUseCase().collect { layout ->
                emit(Resource.Success<DisplayMode>(layouts[layout]))
            }
            Timber.d("Timber: ReadFontSizeState was Finished Successfully")
        } catch (e: Exception) {
            Timber.e("ReadFontSizeState: " + e.localizedMessage)
            emit(Resource.Error<DisplayMode>(message = e.message.toString()))
        }
    }
}