package org.ireader.domain.use_cases.preferences.reader_preferences.screens

import org.ireader.domain.use_cases.preferences.reader_preferences.LibraryLayoutTypeUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.SortersDescUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.SortersUseCase

data class LibraryScreenPrefUseCases(
    val libraryLayoutTypeUseCase: LibraryLayoutTypeUseCase,
    val sortersDescUseCase: SortersDescUseCase,
    val sortersUseCase: SortersUseCase,
    
)