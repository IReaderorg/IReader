package ir.kazemcodes.infinity.domain.use_cases.preferences

import ir.kazemcodes.infinity.domain.repository.Repository

class SaveLibraryLayoutTypeStateUseCase(
    private val repository: Repository,
) {
    operator fun invoke(layoutIndex: Int) {
        repository.preferencesHelper.libraryLayoutTypeStateKey.set(layoutIndex)
    }
}