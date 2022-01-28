package ir.kazemcodes.infinity.core.domain.use_cases.preferences.reader_preferences

import ir.kazemcodes.infinity.core.domain.use_cases.preferences.apperance.ReadNightModePreferences
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.apperance.SaveNightModePreferences
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.services.ReadLastUpdateTime
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.services.SetLastUpdateTime

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
    val getBackgroundColorUseCase: GetBackgroundColorUseCase,
    val setBackgroundColorUseCase: SetBackgroundColorUseCase,
    val saveFontHeightUseCase: SaveFontHeightUseCase,
    val readFontHeightUseCase: ReadFontHeightUseCase,
    val readParagraphDistanceUseCase: ReadParagraphDistanceUseCase,
    val saveParagraphDistanceUseCase: SaveParagraphDistanceUseCase,
    val saveParagraphIndentUseCase: SaveParagraphIndentUseCase,
    val readParagraphIndentUseCase: ReadParagraphIndentUseCase,
    val saveOrientationUseCase: SaveOrientationUseCase,
    val readOrientationUseCase: ReadOrientationUseCase,
    val readFilterUseCase: ReadFilterUseCase,
    val saveFiltersUseCase: SaveFiltersUseCase,
    val readSortersUseCase: ReadSortersUseCase,
    val saveSortersUseCase: SaveSortersUseCase,
    val readLastUpdateTime: ReadLastUpdateTime,
    val setLastUpdateTime: SetLastUpdateTime,
    val readNightModePreferences: ReadNightModePreferences,
    val saveNightModePreferences: SaveNightModePreferences

)
