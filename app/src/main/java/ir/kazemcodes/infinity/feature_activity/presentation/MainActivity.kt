package ir.kazemcodes.infinity.feature_activity.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.WorkManager
import com.zhuinden.simplestack.History
import com.zhuinden.simplestack.SimpleStateChanger
import com.zhuinden.simplestack.StateChange
import com.zhuinden.simplestack.navigator.Navigator
import com.zhuinden.simplestackextensions.navigatorktx.androidContentFrame
import com.zhuinden.simplestackextensions.services.DefaultServiceProvider
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.ActivityScoped
import ir.kazemcodes.infinity.MyApplication
import ir.kazemcodes.infinity.R
import ir.kazemcodes.infinity.feature_activity.core.FragmentStateChanger


@AndroidEntryPoint
@ActivityScoped
class MainActivity : AppCompatActivity(), SimpleStateChanger.NavigationHandler {

    private lateinit var fragmentStateChanger: FragmentStateChanger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val app = application as MyApplication
        val globalServices = app.globalServices


        fragmentStateChanger = FragmentStateChanger(supportFragmentManager, R.id.container)

        Navigator.configure()
            .setStateChanger(SimpleStateChanger(this))
            .setScopedServices(DefaultServiceProvider())
            .setGlobalServices(globalServices)
            .install(this, androidContentFrame, History.of(MainScreenKey()))
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
        WorkManager.getInstance(this).cancelAllWork()
        super.onDestroy()
    }

    /**
     * I created this in order to support the full power of kodein, because i use
     * simple stack i can't use the kodein compose directly
     * **/
//    @Composable
//    fun Main(content: @Composable () -> Unit) = withDI(di) {
//        content()
//    }
    override fun onNavigationEvent(stateChange: StateChange) {
        fragmentStateChanger.handleStateChange(stateChange)
    }


}

