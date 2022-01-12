package ir.kazemcodes.infinity.domain.use_cases.preferences

import ir.kazemcodes.infinity.domain.repository.Repository

class ReadBrightnessStateUseCase(
    private val repository: Repository,
) {
    operator fun invoke(): Float {
        return repository.preferencesHelper.readerBrightness.get()

    }
}