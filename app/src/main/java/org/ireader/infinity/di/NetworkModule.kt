package org.ireader.infinity.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.CookieJar
import org.ireader.core_ui.theme.AppPreferences
import org.ireader.core_ui.theme.UiPreferences
import org.ireader.data.local.AppDatabase
import org.ireader.data.local.dao.RemoteKeysDao
import org.ireader.data.repository.NetworkPreferences
import org.ireader.data.repository.RemoteKeyRepositoryImpl
import org.ireader.domain.repository.RemoteKeyRepository
import org.ireader.domain.repository.RemoteRepository
import org.ireader.domain.use_cases.fetchers.FetchBookDetailAndChapterDetailFromWebView
import org.ireader.domain.use_cases.fetchers.FetchUseCase
import org.ireader.domain.use_cases.preferences.apperance.ReadNightModePreferences
import org.ireader.domain.use_cases.preferences.apperance.SaveNightModePreferences
import org.ireader.domain.use_cases.preferences.reader_preferences.*
import org.ireader.domain.use_cases.preferences.services.ReadLastUpdateTime
import org.ireader.domain.use_cases.preferences.services.SetLastUpdateTime
import org.ireader.domain.use_cases.remote.*
import org.ireader.domain.use_cases.remote.key.*
import org.ireader.domain.utils.MemoryCookieJar
import tachiyomi.core.prefs.PreferenceStore
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {

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
            saveBrightnessStateUseCase = SaveBrightnessStateUseCase(appPreferences),
            saveLibraryLayoutUseCase = SaveLibraryLayoutTypeStateUseCase(appPreferences),
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
            saveNightModePreferences = SaveNightModePreferences(uiPreferences)
        )
    }

    @Provides
    @Singleton
    fun providesCookieJar(): CookieJar {
        return MemoryCookieJar()
    }


    @Provides
    @Singleton
    fun provideRemoteKeyRepository(
        remoteKeysDao: RemoteKeysDao,
    ): RemoteKeyRepository {
        return RemoteKeyRepositoryImpl(
            dao = remoteKeysDao
        )
    }

    @Provides
    @Singleton
    fun provideRemoteKeyUseCase(
        remoteKeyRepository: RemoteKeyRepository,
    ): RemoteKeyUseCase {
        return RemoteKeyUseCase(
            deleteAllExploredBook = DeleteAllExploredBook(remoteKeyRepository),
            deleteAllRemoteKeys = DeleteAllRemoteKeys(remoteKeyRepository),
            insertAllExploredBook = InsertAllExploredBook(remoteKeyRepository),
            insertAllRemoteKeys = InsertAllRemoteKeys(remoteKeyRepository)
        )
    }



    @Singleton
    @Provides
    fun provideNetworkPreference(preferenceStore: PreferenceStore): NetworkPreferences {
        return NetworkPreferences(preferenceStore)
    }

    @Singleton
    @Provides
    fun providesRemoteUseCase(
        remoteRepository: RemoteRepository,
        database: AppDatabase,
    ): RemoteUseCases {
        return RemoteUseCases(
            getBookDetail = GetBookDetail(remoteRepository),
            getRemoteChapters = GetRemoteChapters(remoteRepository),
            getRemoteReadingContent = GetRemoteReadingContent(remoteRepository),
            getRemoteBookByPaginationUseCase = GetRemoteBookByPaginationUseCase(remoteRepository)

        )
    }

    @Singleton
    @Provides
    fun providesFetchersUseCase(): FetchUseCase {
        return FetchUseCase(
            FetchBookDetailAndChapterDetailFromWebView()
        )
    }


}
