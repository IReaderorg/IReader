package ir.kazemcodes.infinity.core.domain.use_cases.preferences

import ir.kazemcodes.infinity.core.domain.repository.Repository

class SaveLibraryLayoutTypeStateUseCase(
    private val repository: Repository,
) {
    operator fun invoke(layoutIndex: Int) {
        repository.preferencesHelper.libraryLayoutTypeStateKey.set(layoutIndex)
    }
}