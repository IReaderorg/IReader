package org.ireader.app.di

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.googlefonts.GoogleFont
import ireader.core.http.WebViewManger
import ireader.core.prefs.PreferenceStoreFactory
import ireader.data.catalog.impl.AndroidCatalogInstallationChanges
import ireader.data.catalog.impl.AndroidLocalInstaller
import ireader.domain.preferences.prefs.AndroidUiPreferences
import ireader.i18n.ProjectConfig
import ireader.presentation.imageloader.LibraryCovers
import ireader.presentation.ui.update.AndroidAppUpdateChecker
import ireader.presentation.ui.update.AppUpdateChecker
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.ireader.app.BuildConfig
import org.ireader.app.R
import org.ireader.app.initiators.*
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.io.File


@OptIn(ExperimentalTextApi::class) val AppModule = module  {


    single<GoogleFont.Provider> {
        GoogleFont.Provider(
            providerAuthority = "com.google.android.gms.fonts",
            providerPackage = "com.google.android.gms",
            certificates = R.array.com_google_android_gms_fonts_certs
        )
    }
    single<AppInitializers> { AppInitializers(EmojiCompatInitializer(androidApplication()),
        NotificationsInitializer(androidApplication(),get()), CrashHandler(androidApplication()), FirebaseInitializer(androidApplication()),
        UpdateServiceInitializer(androidApplication(),get()), get())
    }
    single<AndroidUiPreferences> {
        AndroidUiPreferences(
            preferenceStore = get(),
            provider = get()
        )
    }
    factory<CatalogStoreInitializer>  { CatalogStoreInitializer(get(), get(), get(), get()) }
    factory<CrashHandler>  { org.ireader.app.initiators.CrashHandler(androidApplication()) }
    factory<EmojiCompatInitializer>  { org.ireader.app.initiators.EmojiCompatInitializer(androidApplication()) }
    factory<FirebaseInitializer>  { org.ireader.app.initiators.FirebaseInitializer(androidApplication()) }
    factory<NotificationsInitializer>  { org.ireader.app.initiators.NotificationsInitializer(androidApplication(),get()) }
    factory<SecureActivityDelegateImpl>  { org.ireader.app.initiators.SecureActivityDelegateImpl() }
    factory<UpdateServiceInitializer>  { org.ireader.app.initiators.UpdateServiceInitializer(androidApplication(),get()) }
    single<WebViewManger> { WebViewManger(androidApplication()) }
    single<LibraryCovers> { LibraryCovers(
            FileSystem.SYSTEM,
            File(androidApplication().filesDir, "library_covers").toOkioPath()
    ) }
    single<ProjectConfig> { ProjectConfig(
            buildTime = BuildConfig.BUILD_TIME,
            commitCount = BuildConfig.COMMIT_COUNT,
            commitSHA = BuildConfig.COMMIT_SHA,
            includeUpdater = BuildConfig.INCLUDE_UPDATER,
            preview = BuildConfig.PREVIEW,
            versionCode = BuildConfig.VERSION_CODE,
            versionName = BuildConfig.VERSION_NAME,
            applicationId = BuildConfig.APPLICATION_ID
    ) }
    single<AndroidCatalogInstallationChanges> { AndroidCatalogInstallationChanges(androidApplication()) }
    single<AndroidLocalInstaller> {
        AndroidLocalInstaller(
            androidApplication(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    single<PreferenceStoreFactory> { PreferenceStoreFactory(androidContext()) }
    
    // App Update Checker
    single<AppUpdateChecker> { AndroidAppUpdateChecker(androidApplication(), get(), get()) }
}