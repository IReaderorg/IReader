package org.ireader.infinity.core.domain.use_cases.preferences.reader_preferences

import org.ireader.domain.repository.Repository

class ReadBrightnessStateUseCase(
    private val repository: Repository,
) {
    operator fun invoke(): Float {
        return repository.preferencesHelper.readerBrightness.get()

    }
}