package org.ireader.infinity.di

import android.content.Context
import android.webkit.WebView
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.CookieJar
import org.ireader.core_ui.theme.AppPreferences
import org.ireader.core_ui.theme.UiPreferences
import org.ireader.data.repository.NetworkPreferences
import org.ireader.domain.use_cases.fetchers.FetchBookDetailAndChapterDetailFromWebView
import org.ireader.domain.use_cases.fetchers.FetchUseCase
import org.ireader.domain.use_cases.preferences.apperance.NightModePreferencesUseCase
import org.ireader.domain.use_cases.preferences.apperance.ReadNightModePreferences
import org.ireader.domain.use_cases.preferences.reader_preferences.*
import org.ireader.domain.use_cases.preferences.services.ReadLastUpdateTime
import org.ireader.domain.use_cases.preferences.services.SetLastUpdateTime
import org.ireader.domain.utils.MemoryCookieJar
import tachiyomi.core.prefs.PreferenceStore
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class TestNetworkModule {

    @Provides
    @Singleton
    fun provideDataStoreUseCase(
        appPreferences: AppPreferences,
        uiPreferences: UiPreferences,
    ): PreferencesUseCase {
        return PreferencesUseCase(
            readSelectedFontStateUseCase = ReadSelectedFontStateUseCase(appPreferences),
            saveSelectedFontStateUseCase = SaveSelectedFontStateUseCase(appPreferences),
            readFontSizeStateUseCase = ReadFontSizeStateUseCase(appPreferences),
            saveFontSizeStateUseCase = SaveFontSizeStateUseCase(appPreferences),
            readBrightnessStateUseCase = ReadBrightnessStateUseCase(appPreferences),
            brightnessStateUseCase = BrightnessStateUseCase(appPreferences),
            libraryLayoutPreferencesUseCase = LibraryLayoutTypeUseCase(appPreferences),
            readLibraryLayoutUseCase = ReadLibraryLayoutTypeStateUseCase(appPreferences),
            saveBrowseLayoutUseCase = SaveBrowseLayoutTypeStateUseCase(appPreferences),
            readBrowseLayoutUseCase = ReadBrowseLayoutTypeStateUseCase(appPreferences),
            readDohPrefUseCase = ReadDohPrefUseCase(appPreferences),
            saveDohPrefUseCase = SaveDohPrefUseCase(appPreferences),
            getBackgroundColorUseCase = GetBackgroundColorUseCase(appPreferences),
            setBackgroundColorUseCase = SetBackgroundColorUseCase(appPreferences),
            readFontHeightUseCase = ReadFontHeightUseCase(appPreferences),
            saveFontHeightUseCase = SaveFontHeightUseCase(appPreferences),
            saveParagraphDistanceUseCase = SaveParagraphDistanceUseCase(appPreferences),
            readParagraphDistanceUseCase = ReadParagraphDistanceUseCase(appPreferences),
            readOrientationUseCase = ReadOrientationUseCase(appPreferences),
            saveOrientationUseCase = SaveOrientationUseCase(appPreferences),
            readParagraphIndentUseCase = ReadParagraphIndentUseCase(appPreferences),
            saveParagraphIndentUseCase = SaveParagraphIndentUseCase(appPreferences),
            readFilterUseCase = ReadFilterUseCase(appPreferences),
            saveFiltersUseCase = SaveFiltersUseCase(appPreferences),
            readSortersUseCase = ReadSortersUseCase(appPreferences),
            saveSortersUseCase = SaveSortersUseCase(appPreferences),
            readLastUpdateTime = ReadLastUpdateTime(appPreferences),
            setLastUpdateTime = SetLastUpdateTime(appPreferences),
            readNightModePreferences = ReadNightModePreferences(uiPreferences),
            nightModePreferencesUseCase = NightModePreferencesUseCase(uiPreferences)
        )
    }

    @Singleton
    @Provides
    fun providesCookieJar(): CookieJar {
        return MemoryCookieJar()
    }


    @Singleton
    @Provides
    fun provideNetworkPreference(preferenceStore: PreferenceStore): NetworkPreferences {
        return NetworkPreferences(preferenceStore)
    }

    @Singleton
    @Provides
    fun providesWebView(@ApplicationContext context: Context): WebView {
        return WebView(context)
    }


    @Singleton
    @Provides
    fun providesFetchersUseCase(): FetchUseCase {
        return FetchUseCase(
            FetchBookDetailAndChapterDetailFromWebView()
        )
    }


}
