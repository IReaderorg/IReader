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



import org.koin.dsl.module
import java.io.File

val localModule = module {
    single<LibraryCovers>(qualifier=null) { LibraryCovers(
        FileSystem.SYSTEM,
        File(get<Context>().filesDir, "library_covers").toOkioPath()
    ) }
    single<PreferenceStore>(qualifier=null) { AndroidPreferenceStore(get(),"ui") }
    single<ProjectConfig>(qualifier=null) { ProjectConfig(
        buildTime = BuildConfig.BUILD_TIME,
        commitCount = BuildConfig.COMMIT_COUNT,
        commitSHA = BuildConfig.COMMIT_SHA,
        includeUpdater = BuildConfig.INCLUDE_UPDATER,
        preview = BuildConfig.PREVIEW,
        versionCode = BuildConfig.VERSION_CODE,
        versionName = BuildConfig.VERSION_NAME,
        applicationId = BuildConfig.APPLICATION_ID
    ) }
    single<CookiesStorage>(qualifier=null) { AcceptAllCookiesStorage() }
    single<WebViewManger>(qualifier=null) { WebViewManger(get()) }
    factory<CoroutineScope>(qualifier=null) { CoroutineScope(Dispatchers.IO + SupervisorJob()) }
}