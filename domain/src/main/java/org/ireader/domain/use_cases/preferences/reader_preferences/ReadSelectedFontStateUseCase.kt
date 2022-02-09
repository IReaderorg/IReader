package org.ireader.infinity.core.domain.use_cases.preferences.reader_preferences

import org.ireader.core_ui.theme.FontType
import org.ireader.core_ui.theme.fonts
import org.ireader.domain.repository.Repository

class ReadSelectedFontStateUseCase(
    private val repository: Repository,
) {
    operator fun invoke(): FontType {
        return fonts[repository.preferencesHelper.readerFont.get()]
    }
}