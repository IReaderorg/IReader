package ir.kazemcodes.infinity.feature_activity.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.ActivityScoped
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.apperance.NightMode
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.reader_preferences.PreferencesUseCase
import ir.kazemcodes.infinity.core.presentation.theme.InfinityTheme
import ir.kazemcodes.infinity.feature_services.DownloaderService.DownloadService
import ir.kazemcodes.infinity.feature_services.updater_service.UpdateService
import javax.inject.Inject


@AndroidEntryPoint
@ActivityScoped
class MainActivity : ComponentActivity() {

    private val updateRequest = OneTimeWorkRequestBuilder<UpdateService>().build()

    @Inject
    lateinit var preferencesUseCase: PreferencesUseCase


    @OptIn(ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberAnimatedNavController()
            preferencesUseCase.readNightModePreferences().collectAsState(initial = NightMode.FollowSystem)
            InfinityTheme() {
                Surface(color = MaterialTheme.colors.background) {
//                    SetupNavHost(navController = navController)
                    ScreenContent()
                }
            }
            val manager = WorkManager.getInstance(applicationContext)
            manager.cancelAllWorkByTag(DownloadService.DOWNLOADER_SERVICE_NAME)

            manager.enqueue(updateRequest)
        }
    }

    companion object {
        // Splash screen
        private const val SPLASH_MIN_DURATION = 500 // ms
        private const val SPLASH_MAX_DURATION = 5000 // ms
        private const val SPLASH_EXIT_ANIM_DURATION = 400L // ms

        // Shortcut actions
        const val SHORTCUT_LIBRARY = "ir.kazemcodes.Infinity.SHOW_LIBRARY"
        const val SHORTCUT_RECENTLY_UPDATED = "ir.kazemcodes.Infinity.SHOW_RECENTLY_UPDATED"
        const val SHORTCUT_RECENTLY_READ = "ir.kazemcodes.Infinity.SHOW_RECENTLY_READ"
        const val SHORTCUT_CATALOGUES = "ir.kazemcodes.Infinity.SHOW_CATALOGUES"
        const val SHORTCUT_DOWNLOADS = "ir.kazemcodes.Infinity.SHOW_DOWNLOADS"
        const val SHORTCUT_BOOK = "ir.kazemcodes.Infinity.SHOW_BOOK"
        const val SHORTCUT_EXTENSIONS = "ir.kazemcodes.Infinity.EXTENSIONS"

        const val INTENT_SEARCH = "ir.kazemcodes.Infinity.SEARCH"
        const val INTENT_SEARCH_QUERY = "query"
        const val INTENT_SEARCH_FILTER = "filter"
    }

    override fun onDestroy() {
        WorkManager.getInstance(this).cancelAllWork()
        super.onDestroy()
    }


}

@Composable
fun HandleTheme(mode: NightMode): Boolean {
    return when (mode) {
        is NightMode.Enable -> {
            true
        }
        is NightMode.Disable -> {
            false
        }
        is NightMode.FollowSystem -> {
            isSystemInDarkTheme()
        }
    }
}

