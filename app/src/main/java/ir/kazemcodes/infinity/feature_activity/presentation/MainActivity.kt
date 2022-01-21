package ir.kazemcodes.infinity.feature_activity.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.work.WorkManager
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
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
import ir.kazemcodes.infinity.core.utils.moshi
import ir.kazemcodes.infinity.feature_activity.core.FragmentStateChanger
import ir.kazemcodes.infinity.feature_sources.sources.AvailableSources
import ir.kazemcodes.infinity.feature_sources.sources.models.SourceTower
import timber.log.Timber


@AndroidEntryPoint
@ActivityScoped
class MainActivity : AppCompatActivity(), SimpleStateChanger.NavigationHandler {

    private lateinit var fragmentStateChanger: FragmentStateChanger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val source = AvailableSources(context = this).realLightWebNovel
        val moshi: Moshi = moshi
        val jsonAdapter: JsonAdapter<SourceTower> = moshi.adapter<SourceTower>(SourceTower::class.java)

        val json: String = """{"_baseUrl":"https://readlightnovels.net","_lang":"en","_name":"RealLightWebNovel","creator":"@Kazem","_supportsMostPopular":false,"_supportsSearch":true,"_supportsLatest":true,"latest":{"endpoint":"/latest/page/{page}","isGetRequestType":true,"isHtmlType":true,"selector":"div.row div.home-truyendecu","addBaseUrlToLink":false,"openInWebView":false,"nextPageSelector":"ul.pagination>li:nth-child(6)>a","nextPageValue":"Last","linkSelector":"a","linkAtt":"href","nameSelector":"h3","coverSelector":"img","coverAtt":"src","supportPageList":false},"detail":{"isGetRequestType":true,"isHtmlType":true,"addBaseUrlToLink":false,"openInWebView":false,"nameSelector":"h2.single_title","coverSelector":"div.book img","coverAtt":"src","descriptionSelector":"div.desc-text p","authorBookSelector":"div.info>div:nth-child(1)>a","categorySelector":"div.info>div:nth-child(2)"},"search":{"endpoint":"/page/{page}?s={query}","isGetRequestType":true,"isHtmlType":true,"selector":"div.row div.home-truyendecu","addBaseUrlToLink":false,"openInWebView":false,"nextPageSelector":"ul.pagination>li:nth-child(6)>a","nextPageValue":"Last","linkSelector":"a","linkAtt":"href","linkSearchedSubString":false,"nameSelector":"h3","coverSelector":"img","coverAtt":"src"},"chapters":{"endpoint":"action=tw_ajax&type=pagination&id=390165&page={page}","isGetRequestType":false,"isHtmlType":true,"selector":"div.col-xs-12 ul.list-chapter li","addBaseUrlToLink":false,"openInWebView":false,"isDownloadable":false,"chaptersEndpointWithoutPage":".html","isChapterStatsFromFirst":true,"nameSelector":"span.chapter-text","linkSelector":"a","linkAtt":"href","supportNextPagesList":true},"content":{"isHtmlType":true,"isGetRequestType":true,"addBaseUrlToLink":false,"openInWebView":false,"selector":"div.chapter-content","pageTitleSelector":"a.chapter-title","pageContentSelector":"p"},"nextChapterListLink":""}
"""
        val sourcea = jsonAdapter.fromJson(json)

        if (sourcea != null) {
            Timber.e(sourcea._baseUrl)
        }

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

