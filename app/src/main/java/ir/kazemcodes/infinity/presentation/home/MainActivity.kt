package ir.kazemcodes.infinity.presentation.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.work.WorkManager
import com.zhuinden.simplestack.AsyncStateChanger
import com.zhuinden.simplestack.History
import com.zhuinden.simplestack.navigator.Navigator
import com.zhuinden.simplestackcomposeintegration.core.BackstackProvider
import com.zhuinden.simplestackcomposeintegration.core.ComposeStateChanger
import com.zhuinden.simplestackextensions.navigatorktx.androidContentFrame
import com.zhuinden.simplestackextensions.services.DefaultServiceProvider
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.ActivityScoped
import ir.kazemcodes.infinity.MyApplication
import ir.kazemcodes.infinity.base_feature.navigation.MainScreenKey
import ir.kazemcodes.infinity.presentation.theme.InfinityTheme


@AndroidEntryPoint
@ActivityScoped
class MainActivity : ComponentActivity(){


    private val composeStateChanger = ComposeStateChanger()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as MyApplication

        val backstack = Navigator.configure()
            .setGlobalServices(app.globalServices)
            .setScopedServices(DefaultServiceProvider())
            .setStateChanger(AsyncStateChanger(composeStateChanger))
            .install(this, androidContentFrame, History.of(MainScreenKey()))





        setContent {
            BackstackProvider(backstack) {
                InfinityTheme {
                    Surface(color = MaterialTheme.colors.background) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            composeStateChanger.RenderScreen()
                        }
                    }
                }

            }
        }
    }

    override fun onBackPressed() {
        if (!Navigator.onBackPressed(this)) {
            super.onBackPressed()
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
        const val SHORTCUT_MANGA = "ir.kazemcodes.Infinity.SHOW_MANGA"
        const val SHORTCUT_EXTENSIONS = "ir.kazemcodes.Infinity.EXTENSIONS"

        const val INTENT_SEARCH = "ir.kazemcodes.Infinity.SEARCH"
        const val INTENT_SEARCH_QUERY = "query"
        const val INTENT_SEARCH_FILTER = "filter"
    }

    override fun onDestroy() {
        super.onDestroy()
        WorkManager.getInstance(this).cancelAllWork()
    }

}
