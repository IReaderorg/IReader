package ireader.domain.use_cases.preferences.reader_preferences.screens

import ireader.domain.use_cases.preferences.reader_preferences.LibraryLayoutTypeUseCase
import ireader.domain.use_cases.preferences.reader_preferences.SortersDescUseCase
import ireader.domain.use_cases.preferences.reader_preferences.SortersUseCase

data class LibraryScreenPrefUseCases(
    val libraryLayoutTypeUseCase: LibraryLayoutTypeUseCase,
    val sortersDescUseCase: SortersDescUseCase,
    val sortersUseCase: SortersUseCase,

)
