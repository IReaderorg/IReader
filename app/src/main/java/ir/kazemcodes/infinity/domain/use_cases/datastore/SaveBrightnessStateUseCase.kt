package ir.kazemcodes.infinity.domain.use_cases.datastore

import ir.kazemcodes.infinity.domain.repository.Repository

class SaveBrightnessStateUseCase (
    private val repository: Repository
) {
    suspend operator fun invoke(brightness: Float) {
        return repository.dataStoreRepository.saveBrightnessState(brightness)
    }
}