package org.ireader.domain.use_cases.preferences.reader_preferences

import androidx.annotation.Keep
import javax.inject.Inject

@Keep
data class ReaderPrefUseCases @Inject constructor(
    val selectedFontStateUseCase: SelectedFontStateUseCase,
    val brightnessStateUseCase: BrightnessStateUseCase,
    val scrollModeUseCase: ScrollModeUseCase,
    val autoScrollMode: AutoScrollMode,
    val fontHeightUseCase: FontHeightUseCase,
    val fontSizeStateUseCase: FontSizeStateUseCase,
    val backgroundColorUseCase: BackgroundColorUseCase,
    val paragraphDistanceUseCase: ParagraphDistanceUseCase,
    val paragraphIndentUseCase: ParagraphIndentUseCase,
    val orientationUseCase: OrientationUseCase,
    val scrollIndicatorUseCase: ScrollIndicatorUseCase,
    val textColorUseCase: TextColorUseCase,
    val immersiveModeUseCase: ImmersiveModeUseCase,
)