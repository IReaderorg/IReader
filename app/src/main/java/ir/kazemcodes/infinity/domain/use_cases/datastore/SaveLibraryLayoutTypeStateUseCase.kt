package ir.kazemcodes.infinity.domain.use_cases.datastore

import ir.kazemcodes.infinity.domain.repository.Repository

class SaveLibraryLayoutTypeStateUseCase(
    private val repository: Repository,
) {
    suspend operator fun invoke(layoutIndex: Int) {
        return repository.dataStoreRepository.saveLibraryLayoutTypeStateUseCase(layoutIndex)
    }
}