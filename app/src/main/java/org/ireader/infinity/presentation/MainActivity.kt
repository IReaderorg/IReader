package org.ireader.infinity.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.ActivityScoped
import org.ireader.domain.feature_services.DownloaderService.DownloadService
import org.ireader.domain.feature_services.updater_service.UpdateService


@AndroidEntryPoint
@ActivityScoped
class MainActivity : ComponentActivity() {

    private val updateRequest = OneTimeWorkRequestBuilder<UpdateService>().build()

    @OptIn(ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        //WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContent {
            InfinityTheme {
                Surface(color = MaterialTheme.colors.background
                ) {
                    ScreenContent()
                }
            }
            val manager = WorkManager.getInstance(applicationContext)
            manager.cancelAllWorkByTag(DownloadService.DOWNLOADER_SERVICE_NAME)
            manager.enqueue(updateRequest)
        }
    }

    override fun onDestroy() {
        WorkManager.getInstance(this).cancelAllWork()
        super.onDestroy()
    }


}

