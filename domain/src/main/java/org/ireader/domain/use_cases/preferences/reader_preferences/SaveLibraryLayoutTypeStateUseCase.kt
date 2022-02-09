package org.ireader.infinity.core.domain.use_cases.preferences.reader_preferences

import org.ireader.domain.repository.Repository

class SaveLibraryLayoutTypeStateUseCase(
    private val repository: Repository,
) {
    operator fun invoke(layoutIndex: Int) {
        repository.preferencesHelper.libraryLayoutTypeStateKey.set(layoutIndex)
    }
}