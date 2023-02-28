package org.ireader.app.di


import android.content.Context
import io.ktor.client.plugins.cookies.*
import ireader.core.http.AcceptAllCookiesStorage
import ireader.core.http.WebViewManger
import ireader.core.prefs.AndroidPreferenceStore
import ireader.core.prefs.PreferenceStore
import ireader.i18n.ProjectConfig
import ireader.presentation.imageloader.LibraryCovers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.ireader.app.BuildConfig
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import java.io.File

val localModule = DI.Module("localModule") {
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
    bindSingleton<CookiesStorage> { AcceptAllCookiesStorage() }
    bindSingleton<WebViewManger> { WebViewManger(instance()) }
    bindProvider<CoroutineScope> { CoroutineScope(Dispatchers.IO + SupervisorJob()) }
}