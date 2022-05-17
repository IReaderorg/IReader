package org.ireader.app.di

import android.content.Context
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.googlefonts.GoogleFont
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.plugins.cookies.CookiesStorage
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.ireader.app.BuildConfig
import org.ireader.app.R
import org.ireader.common_resources.ProjectConfig
import org.ireader.core_api.http.AcceptAllCookiesStorage
import org.ireader.core_api.prefs.AndroidPreferenceStore
import org.ireader.core_api.prefs.PreferenceStore
import org.ireader.image_loader.LibraryCovers
import java.io.File
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class LocalModule {
    @Provides
    @Singleton
    fun provideLibraryCovers(
        @ApplicationContext context: Context,
    ): LibraryCovers {
        return org.ireader.image_loader.LibraryCovers(
            FileSystem.SYSTEM,
            File(context.filesDir, "library_covers").toOkioPath()
        )
    }

    @Provides
    @Singleton
    fun providePreferencesStore(@ApplicationContext context: Context): PreferenceStore {
        return AndroidPreferenceStore(context = context, "ui")
    }
    @Provides
    @Singleton
    fun provideProjectConfig(@ApplicationContext context: Context): ProjectConfig {
        return ProjectConfig(
            buildTime = BuildConfig.BUILD_TIME,
            commitCount = BuildConfig.COMMIT_COUNT,
            commitSHA = BuildConfig.COMMIT_SHA,
            includeUpdater =BuildConfig.INCLUDE_UPDATER ,
            preview =BuildConfig.PREVIEW ,
            versionCode = BuildConfig.VERSION_CODE,
            versionName = BuildConfig.VERSION_NAME
        )
    }

    @Provides
    @Singleton
    fun provideCookieJar(): CookiesStorage {
        return AcceptAllCookiesStorage()
    }



    @OptIn(ExperimentalTextApi::class)
    @Provides
    @Singleton
    fun provideGoogleFontProvider(): GoogleFont.Provider {
        return  GoogleFont.Provider(
            providerAuthority = "com.google.android.gms.fonts",
            providerPackage = "com.google.android.gms",
            certificates = R.array.com_google_android_gms_fonts_certs
        )
    }
}
