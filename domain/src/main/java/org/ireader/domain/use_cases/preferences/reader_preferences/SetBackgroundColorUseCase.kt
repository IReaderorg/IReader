package org.ireader.infinity.core.domain.use_cases.preferences.reader_preferences

import org.ireader.domain.repository.Repository

class SetBackgroundColorUseCase(
    private val repository: Repository,
) {
    operator fun invoke(index: Int) {
        repository.preferencesHelper.backgroundColorIndex.set(index)
    }
}