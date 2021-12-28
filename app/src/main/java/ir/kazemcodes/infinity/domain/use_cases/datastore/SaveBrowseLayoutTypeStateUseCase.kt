package ir.kazemcodes.infinity.domain.use_cases.datastore

import ir.kazemcodes.infinity.domain.repository.Repository

class SaveBrowseLayoutTypeStateUseCase(
    private val repository: Repository,
) {
    suspend operator fun invoke(layoutIndex: Int) {
        return repository.dataStoreRepository.saveBrowseLayoutTypeStateUseCase(layoutIndex)
    }
}