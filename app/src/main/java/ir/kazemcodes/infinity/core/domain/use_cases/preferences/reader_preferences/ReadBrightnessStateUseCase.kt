package ir.kazemcodes.infinity.core.domain.use_cases.preferences.reader_preferences

import ir.kazemcodes.infinity.core.domain.repository.Repository

class ReadBrightnessStateUseCase(
    private val repository: Repository,
) {
    operator fun invoke(): Float {
        return repository.preferencesHelper.readerBrightness.get()

    }
}