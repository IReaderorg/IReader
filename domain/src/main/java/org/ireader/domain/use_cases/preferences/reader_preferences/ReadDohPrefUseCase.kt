package org.ireader.infinity.core.domain.use_cases.preferences.reader_preferences

import org.ireader.domain.repository.Repository

class ReadDohPrefUseCase(
    private val repository: Repository,
) {
    operator fun invoke(): Int {
        return repository.preferencesHelper.dohStateKey.get()
    }
}