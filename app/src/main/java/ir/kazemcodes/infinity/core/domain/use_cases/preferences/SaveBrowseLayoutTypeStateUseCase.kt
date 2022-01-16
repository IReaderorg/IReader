package ir.kazemcodes.infinity.core.domain.use_cases.preferences

import ir.kazemcodes.infinity.core.domain.repository.Repository

class SaveBrowseLayoutTypeStateUseCase(
    private val repository: Repository,
) {
    operator fun invoke(layoutIndex: Int) {
        repository.preferencesHelper.browseLayoutTypeStateKey.set(layoutIndex)
    }
}