package ir.kazemcodes.infinity.domain.use_cases.preferences

import ir.kazemcodes.infinity.domain.repository.Repository
import ir.kazemcodes.infinity.presentation.layouts.DisplayMode
import ir.kazemcodes.infinity.presentation.layouts.layouts

class ReadLibraryLayoutTypeStateUseCase(
    private val repository: Repository,
) {
    operator fun invoke(): DisplayMode {
        return layouts[repository.preferencesHelper.libraryLayoutTypeStateKey.get()]
    }
}