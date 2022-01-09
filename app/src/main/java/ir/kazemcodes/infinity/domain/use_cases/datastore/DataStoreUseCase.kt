package ir.kazemcodes.infinity.domain.use_cases.datastore

data class DataStoreUseCase(
    val readSelectedFontStateUseCase: ReadSelectedFontStateUseCase,
    val saveSelectedFontStateUseCase: SaveSelectedFontStateUseCase,
    val getFontSizeStateUseCase: GetFontSizeStateUseCase,
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
