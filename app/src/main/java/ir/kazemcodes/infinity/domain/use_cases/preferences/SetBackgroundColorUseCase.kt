package ir.kazemcodes.infinity.domain.use_cases.preferences

import ir.kazemcodes.infinity.domain.repository.Repository

class SetBackgroundColorUseCase(
    private val repository: Repository,
) {
    operator fun invoke(index : Int) {
        repository.preferencesHelper.backgroundColorIndex.set(index)
    }
}