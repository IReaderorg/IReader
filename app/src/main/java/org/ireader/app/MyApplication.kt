package org.ireader.app

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Looper
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import ireader.data.di.dataPlatformModule
import ireader.data.di.repositoryInjectModule
import ireader.domain.di.*
import ireader.domain.utils.WebViewUtil
import ireader.presentation.core.di.PresentationModules
import ireader.presentation.core.di.presentationPlatformModule
import ireader.presentation.imageloader.coil.CoilLoaderFactory
import org.ireader.app.di.AppModule
import org.ireader.app.di.DataModule
import ireader.domain.di.localModule
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton
import org.kodein.di.instance


class MyApplication : Application(), ImageLoaderFactory, DIAware, Configuration.Provider {

    override val di: DI by DI.lazy(allowSilentOverride = true) {
        bindSingleton<Context> { this@MyApplication }
        bindSingleton<Application> { this@MyApplication }
        importAll(dataPlatformModule,AppModule, CatalogModule, DataModule, localModule, preferencesInjectModule,
            repositoryInjectModule, UseCasesInject, PresentationModules,DomainServices,DomainModule,presentationPlatformModule)

    }

    val coil: CoilLoaderFactory by instance()


    override fun onCreate() {
        super.onCreate()

    }



    override fun newImageLoader(): ImageLoader {
        return coil.newImageLoader()
    }

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

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.ERROR)
            .build();
    }
}
