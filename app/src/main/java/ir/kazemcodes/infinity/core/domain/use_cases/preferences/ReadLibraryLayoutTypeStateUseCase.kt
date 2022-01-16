package ir.kazemcodes.infinity.core.domain.use_cases.preferences

import ir.kazemcodes.infinity.core.domain.repository.Repository
import ir.kazemcodes.infinity.core.presentation.layouts.DisplayMode
import ir.kazemcodes.infinity.core.presentation.layouts.layouts

class ReadLibraryLayoutTypeStateUseCase(
    private val repository: Repository,
) {
    operator fun invoke(): DisplayMode {
        return layouts[repository.preferencesHelper.libraryLayoutTypeStateKey.get()]
    }
}