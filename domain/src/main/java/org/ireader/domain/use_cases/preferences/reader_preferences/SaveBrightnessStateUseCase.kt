package org.ireader.infinity.core.domain.use_cases.preferences.reader_preferences

import org.ireader.domain.repository.Repository

class SaveBrightnessStateUseCase(
    private val repository: Repository,
) {
    operator fun invoke(brightness: Float) {
        return repository.preferencesHelper.readerBrightness.set(brightness)
    }
}