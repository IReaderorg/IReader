package ireader.domain.usecases.preferences



data class AndroidReaderPrefUseCases(
        val selectedFontStateUseCase: SelectedFontStateUseCase,
        val backgroundColorUseCase: BackgroundColorUseCase,
        val textColorUseCase: TextColorUseCase,
        val textAlignmentUseCase: TextAlignmentUseCase,
)
