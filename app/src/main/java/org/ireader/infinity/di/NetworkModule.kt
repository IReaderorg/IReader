package org.ireader.infinity.di

import android.content.Context
import android.content.SharedPreferences
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
import org.ireader.data.repository.NetworkPreferences
import org.ireader.domain.local.BookDatabase
import org.ireader.domain.repository.PreferencesHelper
import org.ireader.domain.repository.Repository
import org.ireader.domain.source.NetworkHelper
import org.ireader.domain.use_cases.fetchers.FetchBookDetailAndChapterDetailFromWebView
import org.ireader.domain.use_cases.fetchers.FetchUseCase
import org.ireader.domain.use_cases.preferences.reader_preferences.*
import org.ireader.domain.use_cases.remote.GetRemoteBooksByRemoteMediator
import org.ireader.infinity.core.data.network.utils.MemoryCookieJar
import org.ireader.infinity.core.domain.repository.RemoteRepository
import org.ireader.infinity.core.domain.use_cases.preferences.apperance.ReadNightModePreferences
import org.ireader.infinity.core.domain.use_cases.preferences.apperance.SaveNightModePreferences
import org.ireader.infinity.core.domain.use_cases.preferences.reader_preferences.*
import org.ireader.infinity.core.domain.use_cases.preferences.services.ReadLastUpdateTime
import org.ireader.infinity.core.domain.use_cases.preferences.services.SetLastUpdateTime
import org.ireader.infinity.core.domain.use_cases.remote.GetBookDetail
import org.ireader.infinity.core.domain.use_cases.remote.GetRemoteChapters
import org.ireader.infinity.core.domain.use_cases.remote.GetRemoteReadingContent
import org.ireader.use_cases.remote.RemoteUseCases
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {

    @Provides
    @Singleton
    fun provideDataStoreUseCase(repository: Repository): org.ireader.domain.use_cases.preferences.reader_preferences.PreferencesUseCase {
        return org.ireader.domain.use_cases.preferences.reader_preferences.PreferencesUseCase(
            readSelectedFontStateUseCase = ReadSelectedFontStateUseCase(repository),
            saveSelectedFontStateUseCase = SaveSelectedFontStateUseCase(repository),
            readFontSizeStateUseCase = ReadFontSizeStateUseCase(repository),
            saveFontSizeStateUseCase = SaveFontSizeStateUseCase(repository),
            readBrightnessStateUseCase = ReadBrightnessStateUseCase(repository),
            saveBrightnessStateUseCase = SaveBrightnessStateUseCase(repository),
            saveLibraryLayoutUseCase = SaveLibraryLayoutTypeStateUseCase(repository),
            readLibraryLayoutUseCase = ReadLibraryLayoutTypeStateUseCase(repository),
            saveBrowseLayoutUseCase = SaveBrowseLayoutTypeStateUseCase(repository),
            readBrowseLayoutUseCase = ReadBrowseLayoutTypeStateUseCase(repository),
            readDohPrefUseCase = ReadDohPrefUseCase(repository = repository),
            saveDohPrefUseCase = SaveDohPrefUseCase(repository),
            getBackgroundColorUseCase = GetBackgroundColorUseCase(repository),
            setBackgroundColorUseCase = SetBackgroundColorUseCase(repository = repository),
            readFontHeightUseCase = ReadFontHeightUseCase(repository),
            saveFontHeightUseCase = SaveFontHeightUseCase(repository),
            saveParagraphDistanceUseCase = SaveParagraphDistanceUseCase(repository),
            readParagraphDistanceUseCase = ReadParagraphDistanceUseCase(repository),
            readOrientationUseCase = ReadOrientationUseCase(repository),
            saveOrientationUseCase = SaveOrientationUseCase(repository),
            readParagraphIndentUseCase = ReadParagraphIndentUseCase(repository),
            saveParagraphIndentUseCase = SaveParagraphIndentUseCase(repository),
            readFilterUseCase = ReadFilterUseCase(repository),
            saveFiltersUseCase = SaveFiltersUseCase(repository),
            readSortersUseCase = ReadSortersUseCase(repository),
            saveSortersUseCase = SaveSortersUseCase(repository),
            readLastUpdateTime = ReadLastUpdateTime(repository),
            setLastUpdateTime = SetLastUpdateTime(repository),
            readNightModePreferences = ReadNightModePreferences(repository),
            saveNightModePreferences = SaveNightModePreferences(repository)
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
    fun providePreferenceHelper(sharedPreferences: SharedPreferences): PreferencesHelper {
        return PreferencesHelper(sharedPreferences)
    }

    @Singleton
    @Provides
    fun provideNetworkPreference(sharedPreferences: SharedPreferences): NetworkPreferences {
        return NetworkPreferences(sharedPreferences)
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
