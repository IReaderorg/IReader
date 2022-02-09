package org.ireader.infinity.core.domain.use_cases.preferences.reader_preferences

import org.ireader.domain.repository.Repository

class SaveFontSizeStateUseCase(
    private val repository: Repository,
) {
    operator fun invoke(fontSize: Int) {
        repository.preferencesHelper.setFontScale(fontSize)
    }
}
