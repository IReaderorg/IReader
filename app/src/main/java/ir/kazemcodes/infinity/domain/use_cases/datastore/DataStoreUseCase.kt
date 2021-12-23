package ir.kazemcodes.infinity.domain.use_cases.datastore

data class DataStoreUseCase(
    val readSelectedFontStateUseCase: ReadSelectedFontStateUseCase,
    val saveSelectedFontStateUseCase: SaveSelectedFontStateUseCase,
    val readFontSizeStateUseCase: ReadFontSizeStateUseCase,
    val saveFontSizeStateUseCase: SaveFontSizeStateUseCase,
    val readBrightnessStateUseCase: ReadBrightnessStateUseCase,
    val saveBrightnessStateUseCase: SaveBrightnessStateUseCase
)
