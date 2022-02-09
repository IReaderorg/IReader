package org.ireader.domain.use_cases.preferences.reader_preferences

import org.ireader.domain.repository.Repository

class GetBackgroundColorUseCase(
    private val repository: Repository,
) {
    operator fun invoke(): Int {
        return repository.preferencesHelper.backgroundColorIndex.get()
    }
}