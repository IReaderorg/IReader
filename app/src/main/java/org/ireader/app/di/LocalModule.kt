package org.ireader.app.di

import android.content.Context
import io.ktor.client.plugins.cookies.CookiesStorage
import ireader.i18n.ProjectConfig
import ireader.core.http.AcceptAllCookiesStorage
import ireader.core.http.WebViewManger
import ireader.core.prefs.AndroidPreferenceStore
import ireader.core.prefs.PreferenceStore
import ireader.imageloader.LibraryCovers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.ireader.app.BuildConfig
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Single
import java.io.File

@org.koin.core.annotation.Module
@ComponentScan("org.ireader.app.di.LocalModule")
class LocalModule {

        @Single
    fun provideLibraryCovers(
        context: Context,
    ): LibraryCovers {
        return LibraryCovers(
            FileSystem.SYSTEM,
            File(context.filesDir, "library_covers").toOkioPath()
        )
    }


        @Single
    fun providePreferencesStore(context: Context): PreferenceStore {
        return AndroidPreferenceStore(context = context, "ui")
    }

        @Single
    fun provideProjectConfig(context: Context): ProjectConfig {
        return ProjectConfig(
            buildTime = BuildConfig.BUILD_TIME,
            commitCount = BuildConfig.COMMIT_COUNT,
            commitSHA = BuildConfig.COMMIT_SHA,
            includeUpdater = BuildConfig.INCLUDE_UPDATER,
            preview = BuildConfig.PREVIEW,
            versionCode = BuildConfig.VERSION_CODE,
            versionName = BuildConfig.VERSION_NAME,
            applicationId = BuildConfig.APPLICATION_ID
        )
    }


        @Single
    fun provideCookieJar(): CookiesStorage {
        return AcceptAllCookiesStorage()
    }

        @Single
    fun provideWebViewManager(context: Context): WebViewManger {
        return WebViewManger(context)
    }

//    @OptIn(ExperimentalTextApi::class)
//    @Singleton
//    fun provideGoogleFontProvider(): GoogleFont.Provider {
//        return GoogleFont.Provider(
//            providerAuthority = "com.google.android.gms.fonts",
//            providerPackage = "com.google.android.gms",
//            certificates = R.array.com_google_android_gms_fonts_certs
//        )
//    }

        @Factory
    fun provideActivityCoroutineScope(): CoroutineScope {
        return CoroutineScope(Dispatchers.IO + SupervisorJob())
    }



}
