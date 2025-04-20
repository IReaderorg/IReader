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
import ireader.data.di.repositoryInjectModule
import ireader.domain.di.CatalogModule
import ireader.domain.di.DomainModule
import ireader.domain.di.DomainServices
import ireader.domain.di.UseCasesInject
import ireader.domain.di.localModule
import ireader.domain.di.preferencesInjectModule
import ireader.domain.image.CoverCache
import ireader.presentation.core.di.PresentationModules
import ireader.presentation.core.di.presentationPlatformModule
import ireader.presentation.imageloader.CoilLoaderFactory
import org.ireader.app.di.AppModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.context.KoinContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MyApplication : Application(), SingletonImageLoader.Factory, KoinComponent {
    override fun onCreate() {
        super.onCreate()
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
            modules(UseCasesInject)
            modules(PresentationModules)
            modules(DomainServices)
            modules(DomainModule)
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
