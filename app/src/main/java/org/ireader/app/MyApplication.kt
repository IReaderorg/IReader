package org.ireader.app

import android.app.Application
import android.os.Build
import android.os.Looper
import coil.ImageLoader
import coil.ImageLoaderFactory
import ireader.domain.di.DomainModules
import ireader.domain.di.DomainServices
import ireader.domain.utils.WebViewUtil
import ireader.presentation.core.di.uiModules
import ireader.presentation.imageloader.coil.CoilLoaderFactory
import org.ireader.app.di.*
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext.startKoin
import org.koin.ksp.generated.module

class MyApplication : Application(), KoinComponent, ImageLoaderFactory {


    val coil: CoilLoaderFactory by inject()


    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApplication)
            androidLogger()
            modules(
                DataModule,
                CatalogModule().module,
                DatabaseInject().module,
                LocalModule().module,
                PreferencesInject().module,
                RepositoryInject().module,
                UseCasesInject().module,
                DomainModules().module,
                AppModule,
            )

            modules(uiModules)
            modules(DomainServices)
            workManagerFactory()
        }

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
}
