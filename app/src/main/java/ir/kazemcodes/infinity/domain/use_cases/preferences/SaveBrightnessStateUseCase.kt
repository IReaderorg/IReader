package ir.kazemcodes.infinity.domain.use_cases.preferences

import ir.kazemcodes.infinity.domain.repository.Repository

class SaveBrightnessStateUseCase(
    private val repository: Repository,
) {
    operator fun invoke(brightness: Float) {
        return repository.preferencesHelper.setBrightness(brightness)
    }
}