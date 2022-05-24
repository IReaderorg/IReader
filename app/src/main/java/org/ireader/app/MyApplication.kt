package org.ireader.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import org.ireader.app.initiators.AppInitializers
import org.ireader.image_loader.coil.CoilLoaderFactory
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider, ImageLoaderFactory {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var initializers: AppInitializers

    @Inject
    lateinit var coil: CoilLoaderFactory

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    override fun newImageLoader(): ImageLoader {
        return coil.newImageLoader()
    }
}
