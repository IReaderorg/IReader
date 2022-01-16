package ir.kazemcodes.infinity.core.domain.use_cases.preferences

import ir.kazemcodes.infinity.core.domain.repository.Repository

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