package ir.kazemcodes.infinity.domain.use_cases.datastore

import ir.kazemcodes.infinity.domain.repository.Repository

class SaveDohPrefUseCase(
    private val repository: Repository,
) {
    suspend operator fun invoke(dohPref: Int) {
        return repository.dataStoreRepository.saveDohPrefUseCase(dohPref)
    }
}