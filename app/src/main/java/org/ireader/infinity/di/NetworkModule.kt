package org.ireader.infinity.di

import android.content.Context
import android.webkit.WebView
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.CookieJar
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.ireader.core.prefs.PreferenceStore
import org.ireader.data.repository.NetworkPreferences
import org.ireader.domain.local.BookDatabase
import org.ireader.domain.repository.RemoteRepository
import org.ireader.domain.source.NetworkHelper
import org.ireader.domain.ui.AppPreferences
import org.ireader.domain.ui.UiPreferences
import org.ireader.domain.use_cases.fetchers.FetchBookDetailAndChapterDetailFromWebView
import org.ireader.domain.use_cases.fetchers.FetchUseCase
import org.ireader.domain.use_cases.preferences.apperance.ReadNightModePreferences
import org.ireader.domain.use_cases.preferences.apperance.SaveNightModePreferences
import org.ireader.domain.use_cases.preferences.reader_preferences.*
import org.ireader.domain.use_cases.preferences.services.ReadLastUpdateTime
import org.ireader.domain.use_cases.preferences.services.SetLastUpdateTime
import org.ireader.domain.use_cases.remote.*
import org.ireader.domain.utils.MemoryCookieJar
import java.util.concurrent.TimeUnit
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

    @Singleton
    @Provides
    fun providesCookieJar(): CookieJar {
        return MemoryCookieJar()
    }

    @Singleton
    @Provides
    fun providesOkHttpClient(
        cookieJar: CookieJar,
        networkPreferences: NetworkPreferences,
    ): OkHttpClient {
        return OkHttpClient.Builder().apply {
            connectTimeout(networkPreferences.connectionTimeOut.get(), TimeUnit.MINUTES)
            writeTimeout(networkPreferences.writeTimeOut.get(), TimeUnit.MINUTES)
            readTimeout(networkPreferences.readTimeOut.get(), TimeUnit.MINUTES)
            dispatcher(Dispatcher().apply {
                maxRequestsPerHost = networkPreferences.maxHostRequest.get()
                maxRequests = networkPreferences.maxRequest.get()
            })
            networkInterceptors().add(
                HttpLoggingInterceptor().apply {
                    setLevel(HttpLoggingInterceptor.Level.BASIC)
                    // proxy(Proxy(Proxy.Type.HTTP,InetSocketAddress("127.0.0.1",8080)))
                }
            )
            readTimeout(15, TimeUnit.SECONDS)
            connectTimeout(15, TimeUnit.SECONDS)
            cookieJar(cookieJar)
        }
            .build()
    }

    @Singleton
    @Provides
    fun providesNetworkHelper(@ApplicationContext context: Context): NetworkHelper {
        return NetworkHelper(context)
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
    fun providesRemoteUseCase(
        remoteRepository: RemoteRepository,
        database: BookDatabase,
    ): RemoteUseCases {
        return RemoteUseCases(
            getRemoteBooksByRemoteMediator = GetRemoteBooksByRemoteMediator(remoteRepository,
                database),
            getBookDetail = GetBookDetail(remoteRepository),
            getRemoteChapters = GetRemoteChapters(remoteRepository),
            getRemoteReadingContent = GetRemoteReadingContent(remoteRepository)
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
