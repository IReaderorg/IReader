package ir.kazemcodes.infinity.core.domain.use_cases.preferences.reader_preferences

import ir.kazemcodes.infinity.core.domain.models.FontType
import ir.kazemcodes.infinity.core.domain.repository.Repository
import ir.kazemcodes.infinity.core.presentation.theme.fonts

class ReadSelectedFontStateUseCase(
    private val repository: Repository,
) {
    operator fun invoke(): FontType {
        return fonts[repository.preferencesHelper.readerFont.get()]
    }
}