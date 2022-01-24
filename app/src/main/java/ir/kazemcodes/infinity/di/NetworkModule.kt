package ir.kazemcodes.infinity.di

import android.content.Context
import android.content.SharedPreferences
import android.webkit.WebView
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ir.kazemcodes.infinity.core.data.local.BookDatabase
import ir.kazemcodes.infinity.core.data.network.utils.MemoryCookieJar
import ir.kazemcodes.infinity.core.data.repository.PreferencesHelper
import ir.kazemcodes.infinity.core.domain.repository.RemoteRepository
import ir.kazemcodes.infinity.core.domain.repository.Repository
import ir.kazemcodes.infinity.core.domain.use_cases.fetchers.FetchBookDetailAndChapterDetailFromWebView
import ir.kazemcodes.infinity.core.domain.use_cases.fetchers.FetchUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.*
import ir.kazemcodes.infinity.core.domain.use_cases.remote.*
import ir.kazemcodes.infinity.feature_sources.sources.utils.NetworkHelper
import ir.kazemcodes.infinity.feature_updater.ProgressListener
import okhttp3.Call
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {

    @Provides
    @Singleton
    fun provideDataStoreUseCase(repository: Repository): PreferencesUseCase {
        return PreferencesUseCase(
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
            saveSortersUseCase = SaveSortersUseCase(repository)
        )
    }
    @Singleton
    @Provides
    fun providesCookieJar(): CookieJar {
        return MemoryCookieJar()
    }

    @Singleton
    @Provides
    fun providesOkHttpClient(cookieJar: CookieJar): OkHttpClient {
        return OkHttpClient.Builder().apply {
            networkInterceptors().add(
                HttpLoggingInterceptor().apply {
                    setLevel(HttpLoggingInterceptor.Level.BASIC)
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
    fun providesNetworkHelper(context: Context): NetworkHelper {
        return NetworkHelper(context)
    }

    @Singleton
    @Provides
    fun providePreferenceHelper(sharedPreferences: SharedPreferences): PreferencesHelper {
        return PreferencesHelper(sharedPreferences)
    }

    @Singleton
    @Provides
    fun providesWebView(context: Context): WebView {
        return WebView(context)
    }

    @Singleton
    @Provides
    fun providesRemoteUseCase(remoteRepository: RemoteRepository,database: BookDatabase): RemoteUseCases {
        return RemoteUseCases(
            getRemoteBooksByRemoteMediator = GetRemoteBooksByRemoteMediator(remoteRepository,database),
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
fun OkHttpClient.newCallWithProgress(request: Request, listener: ProgressListener): Call {
    val progressClient = newBuilder()
        .cache(null)
        .addNetworkInterceptor { chain ->
            val originalResponse = chain.proceed(chain.request())
            originalResponse.newBuilder()
                .build()
        }
        .build()

    return progressClient.newCall(request)
}