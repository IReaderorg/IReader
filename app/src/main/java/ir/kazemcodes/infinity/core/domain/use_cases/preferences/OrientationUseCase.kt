package ir.kazemcodes.infinity.core.domain.use_cases.preferences

import ir.kazemcodes.infinity.core.domain.repository.Repository

class SaveOrientationUseCase(
    private val repository: Repository,
) {
    operator fun invoke(paragraphDistance: Int) {
        repository.preferencesHelper.orientation.set(paragraphDistance)
    }
}

class ReadOrientationUseCase(
    private val repository: Repository,
) {
    operator fun invoke(): Int {
        return repository.preferencesHelper.orientation.get()
    }
}
