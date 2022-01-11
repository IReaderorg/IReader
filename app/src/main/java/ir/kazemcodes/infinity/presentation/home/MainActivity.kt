package ir.kazemcodes.infinity.presentation.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.work.WorkManager
import com.zhuinden.simplestack.AsyncStateChanger
import com.zhuinden.simplestack.GlobalServices
import com.zhuinden.simplestack.History
import com.zhuinden.simplestack.navigator.Navigator
import com.zhuinden.simplestackcomposeintegration.core.BackstackProvider
import com.zhuinden.simplestackcomposeintegration.core.ComposeStateChanger
import com.zhuinden.simplestackextensions.navigatorktx.androidContentFrame
import com.zhuinden.simplestackextensions.services.DefaultServiceProvider
import com.zhuinden.simplestackextensions.servicesktx.add
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.ActivityScoped
import ir.kazemcodes.infinity.base_feature.navigation.MainScreenKey
import ir.kazemcodes.infinity.domain.use_cases.local.LocalUseCase
import ir.kazemcodes.infinity.domain.use_cases.preferences.PreferencesUseCase
import ir.kazemcodes.infinity.domain.use_cases.remote.RemoteUseCase
import ir.kazemcodes.infinity.presentation.theme.InfinityTheme
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.compose.withDI
import javax.inject.Inject


@AndroidEntryPoint
@ActivityScoped
class MainActivity : ComponentActivity(),DIAware {


    @Inject
    lateinit var localUseCase: LocalUseCase

    @Inject
    lateinit var remoteUseCase: RemoteUseCase

    @Inject
    lateinit var preferencesUseCase: PreferencesUseCase

    private val composeStateChanger = ComposeStateChanger()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



       val globalServices :GlobalServices  = GlobalServices.builder()
            .add(localUseCase)
            .add(remoteUseCase)
            .add(preferencesUseCase)
            .build()

        val backstack = Navigator.configure()
            .setGlobalServices(globalServices)
            .setScopedServices(DefaultServiceProvider())
            .setStateChanger(AsyncStateChanger(composeStateChanger))
            .install(this, androidContentFrame, History.of(MainScreenKey()))

        setContent {
            BackstackProvider(backstack)   {
                InfinityTheme {
                    Surface(color = MaterialTheme.colors.background) {
                        Main {
                            Box(modifier = Modifier.fillMaxSize())  {
                                composeStateChanger.RenderScreen()
                            }
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
    /**
     * I created this in order to support the full power of kodein, because i use
     * simple stack i can't use the kodein compose directly
     * **/
    @Composable
    fun Main(content: @Composable () -> Unit,) = withDI(di) {
        content()
    }

    override val di: DI by closestDI(this)


}

