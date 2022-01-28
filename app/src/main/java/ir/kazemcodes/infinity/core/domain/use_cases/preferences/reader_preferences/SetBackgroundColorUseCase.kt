package ir.kazemcodes.infinity.core.domain.use_cases.preferences.reader_preferences

import ir.kazemcodes.infinity.core.domain.repository.Repository

class SetBackgroundColorUseCase(
    private val repository: Repository,
) {
    operator fun invoke(index: Int) {
        repository.preferencesHelper.backgroundColorIndex.set(index)
    }
}