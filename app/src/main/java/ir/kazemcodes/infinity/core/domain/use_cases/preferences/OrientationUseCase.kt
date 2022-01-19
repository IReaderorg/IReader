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
class SaveFiltersUseCase(
    private val repository: Repository,
) {
    operator fun invoke(value: Int) {
        repository.preferencesHelper.filterLibraryScreen.set(value)
    }
}

class ReadFilterUseCase(
    private val repository: Repository,
) {
    operator fun invoke(): Int {
        return repository.preferencesHelper.filterLibraryScreen.get()
    }
}

class SaveSortersUseCase(
    private val repository: Repository,
) {
    operator fun invoke(value: Int) {
        repository.preferencesHelper.sortLibraryScreen.set(value)
    }
}

class ReadSortersUseCase(
    private val repository: Repository,
) {
    operator fun invoke(): Int {
        return repository.preferencesHelper.sortLibraryScreen.get()
    }
}

