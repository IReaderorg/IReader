package org.ireader.infinity.core.domain.use_cases.preferences.reader_preferences

import org.ireader.domain.repository.Repository

class SaveSelectedFontStateUseCase(
    private val repository: Repository,
) {
    /**
     * fontIndex is the index of font which is in fonts list inside the Type package
     */
    operator fun invoke(fontIndex: Int) {
        repository.preferencesHelper.readerFont.set(fontIndex)
    }
}