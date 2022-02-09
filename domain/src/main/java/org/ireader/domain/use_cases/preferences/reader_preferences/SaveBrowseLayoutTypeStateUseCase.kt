package org.ireader.infinity.core.domain.use_cases.preferences.reader_preferences

import org.ireader.domain.repository.Repository

class SaveBrowseLayoutTypeStateUseCase(
    private val repository: Repository,
) {
    operator fun invoke(layoutIndex: Int) {
        repository.preferencesHelper.browseLayoutTypeStateKey.set(layoutIndex)
    }
}