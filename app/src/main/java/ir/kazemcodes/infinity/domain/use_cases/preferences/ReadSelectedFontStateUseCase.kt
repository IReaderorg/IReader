package ir.kazemcodes.infinity.domain.use_cases.preferences

import ir.kazemcodes.infinity.domain.models.FontType
import ir.kazemcodes.infinity.domain.repository.Repository
import ir.kazemcodes.infinity.presentation.theme.fonts

class ReadSelectedFontStateUseCase(
    private val repository: Repository,
) {
    operator fun invoke(): FontType  {
        return  fonts[repository.preferencesHelper.readerFont.get()]
    }
}