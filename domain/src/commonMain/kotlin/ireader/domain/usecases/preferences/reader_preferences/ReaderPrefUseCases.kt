package ireader.domain.usecases.preferences.reader_preferences

data class ReaderPrefUseCases(
        val brightnessStateUseCase: BrightnessStateUseCase,
        val scrollModeUseCase: ScrollModeUseCase,
        val autoScrollMode: AutoScrollMode,
        val fontHeightUseCase: FontHeightUseCase,
        val fontSizeStateUseCase: FontSizeStateUseCase,
        val paragraphDistanceUseCase: ParagraphDistanceUseCase,
        val paragraphIndentUseCase: ParagraphIndentUseCase,
        val scrollIndicatorUseCase: ScrollIndicatorUseCase,
        val immersiveModeUseCase: ImmersiveModeUseCase,
        val backgroundColorUseCase: BackgroundColorUseCase,
        val textColorUseCase: TextColorUseCase,
        val textAlignmentUseCase: TextAlignmentUseCase,
)
