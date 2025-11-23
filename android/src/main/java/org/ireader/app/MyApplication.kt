package org.ireader.app

import android.app.Application
import android.os.Build
import android.os.Looper
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import ireader.core.http.WebViewUtil
import ireader.data.core.DatabaseHandler
import ireader.data.di.DataModule
import ireader.data.di.dataPlatformModule
import ireader.data.di.remoteModule
import ireader.data.di.remotePlatformModule
import ireader.data.di.repositoryInjectModule
import ireader.data.di.reviewModule
import ireader.data.remote.AutoSyncService
import ireader.domain.di.CatalogModule
import ireader.domain.di.DomainModule
import ireader.domain.di.DomainServices
import ireader.domain.di.PluginModule
import ireader.domain.di.UseCasesInject
import ireader.domain.di.localModule
import ireader.domain.di.preferencesInjectModule
import ireader.presentation.core.di.PresentationModules
import ireader.presentation.core.di.presentationPlatformModule
import ireader.presentation.imageloader.CoilLoaderFactory
import kotlinx.coroutines.launch
import org.ireader.app.di.AppModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.ireader.app.crash.CrashHandler
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin

class MyApplication : Application(), SingletonImageLoader.Factory, KoinComponent {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize crash handler first
        CrashHandler.initialize(this)
        startKoin {
            // Log Koin into Android logger
            androidLogger()
            // Reference Android context
            androidContext(this@MyApplication)
            // Load modules
            workManagerFactory()
            KoinApplication.init()
            modules(dataPlatformModule)
            modules(AppModule)
            modules(CatalogModule)
            modules(DataModule)
            modules(localModule)
            modules(preferencesInjectModule)
            modules(repositoryInjectModule)
            modules(remotePlatformModule)
            modules(remoteModule)
            modules(reviewModule)
            modules(UseCasesInject)
            modules(PresentationModules)
            modules(DomainServices)
            modules(DomainModule)
            modules(PluginModule)
            modules(presentationPlatformModule)
        }
        
        // Ensure database is initialized
        try {
            val databaseHandler: DatabaseHandler by inject()
            databaseHandler.initialize()
        } catch (e: Exception) {
            println("Failed to initialize database: ${e.message}")
            e.printStackTrace()
        }
        
        // Initialize system fonts
        try {
            val systemFontsInitializer: ireader.domain.usecases.fonts.SystemFontsInitializer by inject()
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                systemFontsInitializer.initializeSystemFonts()
                println("System fonts initialized successfully")
            }
        } catch (e: Exception) {
            println("Failed to initialize system fonts: ${e.message}")
            e.printStackTrace()
        }
        
        // Start auto-sync service if available
        try {
            val autoSyncService: AutoSyncService? by inject()
            autoSyncService?.start()
            println("Auto-sync service started successfully")
        } catch (e: Exception) {
            println("Auto-sync service not available or failed to start: ${e.message}")
        }
    }

    val coil: CoilLoaderFactory by inject()

    override fun getPackageName(): String {
        // This causes freezes in Android 6/7 for some reason
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                // Override the value passed as X-Requested-With in WebView requests
                val stackTrace = Looper.getMainLooper().thread.stackTrace
                val chromiumElement = stackTrace.find {
                    it.className.equals(
                        "org.chromium.base.BuildInfo",
                        ignoreCase = true,
                    )
                }
                if (chromiumElement?.methodName.equals("getAll", ignoreCase = true)) {
                    return WebViewUtil.SPOOF_PACKAGE_NAME
                }
            } catch (e: Exception) {
            }
        }
        return super.getPackageName()
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return coil.newImageLoader(this)
    }
}
