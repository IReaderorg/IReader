package org.ireader.app.di

import android.content.Context
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.googlefonts.GoogleFont
import ireader.core.http.WebViewManger
import ireader.core.prefs.AndroidPreferenceStore
import ireader.core.prefs.PreferenceStore
import ireader.data.catalog.impl.AndroidCatalogInstallationChanges
import ireader.data.catalog.impl.AndroidLocalInstaller
import ireader.domain.preferences.prefs.AndroidUiPreferences
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.i18n.ProjectConfig
import ireader.presentation.imageloader.LibraryCovers
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.ireader.app.BuildConfig
import org.ireader.app.R
import org.ireader.app.initiators.*
import org.kodein.di.*
import java.io.File


@OptIn(ExperimentalTextApi::class) val AppModule = DI.Module("appModule") {


    bindSingleton {
        GoogleFont.Provider(
            providerAuthority = "com.google.android.gms.fonts",
            providerPackage = "com.google.android.gms",
            certificates = R.array.com_google_android_gms_fonts_certs
        )
    }
    bindSingleton { AppInitializers(EmojiCompatInitializer(instance()),
        NotificationsInitializer(instance(),instance()), CrashHandler(instance()), FirebaseInitializer(instance()),
        UpdateServiceInitializer(instance(),instance()), instance())
    }
    bindSingleton {
        AndroidUiPreferences(
            preferenceStore = instance(),
            provider = instance()
        )
    }
    bindProvider { new(::CatalogStoreInitializer) }
    bindProvider { org.ireader.app.initiators.CrashHandler(instance()) }
    bindProvider { org.ireader.app.initiators.EmojiCompatInitializer(instance()) }
    bindProvider { org.ireader.app.initiators.FirebaseInitializer(instance()) }
    bindProvider { org.ireader.app.initiators.NotificationsInitializer(instance(),instance()) }
    bindProvider { org.ireader.app.initiators.SecureActivityDelegateImpl() }
    bindProvider { org.ireader.app.initiators.UpdateServiceInitializer(instance(),instance()) }
    bindSingleton<WebViewManger> { WebViewManger(instance()) }
    bindSingleton<LibraryCovers> { LibraryCovers(
            FileSystem.SYSTEM,
            File(instance<Context>().filesDir, "library_covers").toOkioPath()
    ) }
    bindSingleton<PreferenceStore> { AndroidPreferenceStore(instance(),"ui") }
    bindSingleton<ProjectConfig> { ProjectConfig(
            buildTime = BuildConfig.BUILD_TIME,
            commitCount = BuildConfig.COMMIT_COUNT,
            commitSHA = BuildConfig.COMMIT_SHA,
            includeUpdater = BuildConfig.INCLUDE_UPDATER,
            preview = BuildConfig.PREVIEW,
            versionCode = BuildConfig.VERSION_CODE,
            versionName = BuildConfig.VERSION_NAME,
            applicationId = BuildConfig.APPLICATION_ID
    ) }
    bindSingleton<AndroidCatalogInstallationChanges> { AndroidCatalogInstallationChanges(instance()) }
    bindSingleton<AndroidLocalInstaller> { AndroidLocalInstaller(instance(),instance(),instance(),instance(),instance(),instance()) }

}