package ir.kazemcodes.infinity.domain.use_cases.preferences

import ir.kazemcodes.infinity.domain.repository.Repository

class SaveFontSizeStateUseCase(
    private val repository: Repository,
) {
    operator fun invoke(fontSize: Int) {
        repository.preferencesHelper.setFontScale(fontSize)
    }
}
