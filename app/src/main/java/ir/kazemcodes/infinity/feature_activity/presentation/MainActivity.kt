package ir.kazemcodes.infinity.feature_activity.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
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
import ir.kazemcodes.infinity.feature_services.updater_service.UpdateService


@AndroidEntryPoint
@ActivityScoped
class MainActivity : AppCompatActivity(), SimpleStateChanger.NavigationHandler {

    private lateinit var fragmentStateChanger: FragmentStateChanger
    private val updateRequest = OneTimeWorkRequestBuilder<UpdateService>().build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

//        val source = AvailableSources(context = this).realLightWebNovel
//        val moshi: Moshi = moshi
//        val jsonAdapter: JsonAdapter<SourceTower> = moshi.adapter<SourceTower>(SourceTower::class.java)
//
//        Timber.e(jsonAdapter.toJson(source))

        val app = application as MyApplication
        val globalServices = app.globalServices


        fragmentStateChanger = FragmentStateChanger(supportFragmentManager, R.id.container)

        Navigator.configure()
            .setStateChanger(SimpleStateChanger(this))
            .setScopedServices(DefaultServiceProvider())
            .setGlobalServices(globalServices)
            .install(this, androidContentFrame, History.of(MainScreenKey()))


        val manager = WorkManager.getInstance(applicationContext)
        
        manager.enqueue(updateRequest)
        
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

    override fun onNavigationEvent(stateChange: StateChange) {
        fragmentStateChanger.handleStateChange(stateChange)
    }


}

