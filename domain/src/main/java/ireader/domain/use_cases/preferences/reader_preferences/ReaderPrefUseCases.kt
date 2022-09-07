package ireader.domain.use_cases.preferences.reader_preferences

data class ReaderPrefUseCases(
    val selectedFontStateUseCase: SelectedFontStateUseCase,
    val brightnessStateUseCase: BrightnessStateUseCase,
    val scrollModeUseCase: ScrollModeUseCase,
    val autoScrollMode: AutoScrollMode,
    val fontHeightUseCase: FontHeightUseCase,
    val fontSizeStateUseCase: FontSizeStateUseCase,
    val backgroundColorUseCase: BackgroundColorUseCase,
    val paragraphDistanceUseCase: ParagraphDistanceUseCase,
    val paragraphIndentUseCase: ParagraphIndentUseCase,
    val scrollIndicatorUseCase: ScrollIndicatorUseCase,
    val textColorUseCase: TextColorUseCase,
    val immersiveModeUseCase: ImmersiveModeUseCase,
    val textAlignmentUseCase: TextAlignmentUseCase,
)
