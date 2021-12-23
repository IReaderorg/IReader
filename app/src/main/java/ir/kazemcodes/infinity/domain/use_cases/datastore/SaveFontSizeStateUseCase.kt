package ir.kazemcodes.infinity.domain.use_cases.datastore

import ir.kazemcodes.infinity.domain.repository.Repository

class SaveFontSizeStateUseCase (
    private val repository: Repository
) {
    suspend operator fun invoke(fontSize: Int) {
        return repository.dataStoreRepository.saveFontSizeState(fontSize)
    }
}