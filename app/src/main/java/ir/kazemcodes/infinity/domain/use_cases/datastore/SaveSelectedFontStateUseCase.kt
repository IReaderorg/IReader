package ir.kazemcodes.infinity.domain.use_cases.datastore

import ir.kazemcodes.infinity.domain.repository.Repository

class SaveSelectedFontStateUseCase(
    private val repository: Repository,
) {
    /**
     * fontIndex is the index of font which is in fonts list inside the Type package
     */
    suspend operator fun invoke(fontIndex: Int) {
        return repository.dataStoreRepository.saveSelectedFontState(fontIndex)
    }
}