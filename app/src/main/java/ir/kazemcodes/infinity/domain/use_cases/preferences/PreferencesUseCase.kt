package ir.kazemcodes.infinity.domain.use_cases.preferences

data class PreferencesUseCase(
    val readSelectedFontStateUseCase: ReadSelectedFontStateUseCase,
    val saveSelectedFontStateUseCase: SaveSelectedFontStateUseCase,
    val readFontSizeStateUseCase: ReadFontSizeStateUseCase,
    val saveFontSizeStateUseCase: SaveFontSizeStateUseCase,
    val readBrightnessStateUseCase: ReadBrightnessStateUseCase,
    val saveBrightnessStateUseCase: SaveBrightnessStateUseCase,
    val readLibraryLayoutUseCase: ReadLibraryLayoutTypeStateUseCase,
    val saveLibraryLayoutUseCase: SaveLibraryLayoutTypeStateUseCase,
    val readBrowseLayoutUseCase: ReadBrowseLayoutTypeStateUseCase,
    val saveBrowseLayoutUseCase: SaveBrowseLayoutTypeStateUseCase,
    val readDohPrefUseCase: ReadDohPrefUseCase,
    val saveDohPrefUseCase: SaveDohPrefUseCase,
    )
