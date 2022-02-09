package org.ireader.infinity.core.domain.use_cases.preferences.reader_preferences

import org.ireader.domain.repository.Repository

class SaveDohPrefUseCase(
    private val repository: Repository,
) {
    operator fun invoke(dohPref: Int) {
        repository.preferencesHelper.dohStateKey.set(dohPref)
    }
}