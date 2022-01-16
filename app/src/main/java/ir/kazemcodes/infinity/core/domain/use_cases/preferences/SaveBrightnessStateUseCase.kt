package ir.kazemcodes.infinity.core.domain.use_cases.preferences

import ir.kazemcodes.infinity.core.domain.repository.Repository

class SaveBrightnessStateUseCase(
    private val repository: Repository,
) {
    operator fun invoke(brightness: Float) {
        return repository.preferencesHelper.readerBrightness.set(brightness)
    }
}