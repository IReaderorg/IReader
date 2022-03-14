package org.ireader.infinity.di

import android.content.Context
import android.webkit.WebView
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.CookieJar
import org.ireader.domain.use_cases.fetchers.FetchBookDetailAndChapterDetailFromWebView
import org.ireader.domain.use_cases.fetchers.FetchUseCase
import org.ireader.domain.utils.MemoryCookieJar
import tachiyomi.core.prefs.PreferenceStore
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class TestNetworkModule {


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
