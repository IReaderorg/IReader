package ir.kazemcodes.infinity.feature_activity.presentation

import android.content.Context
import android.content.pm.ActivityInfo
import android.view.WindowManager
import android.webkit.WebView
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import androidx.paging.ExperimentalPagingApi
import com.google.accompanist.insets.ProvideWindowInsets
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.ServiceBinder
import com.zhuinden.simplestackcomposeintegration.core.BackstackProvider
import com.zhuinden.simplestackextensions.fragmentsktx.backstack
import com.zhuinden.simplestackextensions.servicesktx.add
import com.zhuinden.simplestackextensions.servicesktx.lookup
import ir.kazemcodes.infinity.core.domain.repository.LocalBookRepository
import ir.kazemcodes.infinity.core.domain.repository.LocalChapterRepository
import ir.kazemcodes.infinity.core.domain.repository.LocalSourceRepository
import ir.kazemcodes.infinity.core.domain.repository.RemoteRepository
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.PreferencesUseCase
import ir.kazemcodes.infinity.core.presentation.theme.InfinityTheme
import ir.kazemcodes.infinity.core.utils.findAppCompatAcivity
import ir.kazemcodes.infinity.core.utils.mappingFetcherTypeWithIndex
import ir.kazemcodes.infinity.core.utils.mappingSourceNameToSource
import ir.kazemcodes.infinity.feature_activity.core.ComposeFragment
import ir.kazemcodes.infinity.feature_activity.core.FragmentKey
import ir.kazemcodes.infinity.feature_detail.presentation.book_detail.BookDetailScreen
import ir.kazemcodes.infinity.feature_detail.presentation.book_detail.BookDetailViewModel
import ir.kazemcodes.infinity.feature_detail.presentation.chapter_detail.ChapterDetailScreen
import ir.kazemcodes.infinity.feature_detail.presentation.chapter_detail.ChapterDetailViewModel
import ir.kazemcodes.infinity.feature_explore.presentation.browse.BrowseViewModel
import ir.kazemcodes.infinity.feature_explore.presentation.browse.BrowserScreen
import ir.kazemcodes.infinity.feature_explore.presentation.browse.ExploreType
import ir.kazemcodes.infinity.feature_library.presentation.LibraryViewModel
import ir.kazemcodes.infinity.feature_reader.presentation.reader.ReaderScreenViewModel
import ir.kazemcodes.infinity.feature_reader.presentation.reader.ReadingScreen
import ir.kazemcodes.infinity.feature_settings.presentation.AboutSettingScreen
import ir.kazemcodes.infinity.feature_settings.presentation.setting.SettingViewModel
import ir.kazemcodes.infinity.feature_settings.presentation.setting.dns.DnsOverHttpScreen
import ir.kazemcodes.infinity.feature_settings.presentation.setting.downloader.DownloaderScreen
import ir.kazemcodes.infinity.feature_settings.presentation.setting.extension_creator.ExtensionCreatorScreen
import ir.kazemcodes.infinity.feature_settings.presentation.setting.extension_creator.ExtensionCreatorViewModel
import ir.kazemcodes.infinity.feature_settings.presentation.webview.WebPageScreen
import ir.kazemcodes.infinity.feature_settings.presentation.webview.WebViewPageModel
import ir.kazemcodes.infinity.feature_sources.presentation.extension.ExtensionScreen
import ir.kazemcodes.infinity.feature_sources.presentation.extension.ExtensionViewModel
import ir.kazemcodes.infinity.feature_sources.sources.Extensions
import kotlinx.parcelize.Parcelize


@ExperimentalMaterialApi
class MainScreenFragment : ComposeFragment() {
    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    override fun FragmentComposable(backstack: Backstack) {
        BackstackProvider(backstack = backstack) {
            InfinityTheme {
                Surface(color = MaterialTheme.colors.background) {
                    MainScreen()
                }
            }

        }
    }

}

@Parcelize
data class MainScreenKey(val noArgument: String = "") : FragmentKey() {
    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(LibraryViewModel(lookup<LocalBookRepository>(), lookup<PreferencesUseCase>()))
            add(MainViewModel())
            add(ExtensionViewModel(lookup<LocalSourceRepository>(),lookup<Extensions>()))
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    override fun instantiateFragment(): Fragment = MainScreenFragment()
}


class BrowserScreenFragment : ComposeFragment() {

    @OptIn(ExperimentalPagingApi::class)
    @Composable
    override fun FragmentComposable(backstack: Backstack) {

        BackstackProvider(backstack = backstack) {
            InfinityTheme {
                BrowserScreen()
            }
        }
    }
}

@Parcelize
data class BrowserScreenKey(val sourceName: String, val exploreType: Int) :
    FragmentKey() {
    override fun instantiateFragment(): Fragment = BrowserScreenFragment()
    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            val exploreType = when (exploreType) {
                0 -> ExploreType.Latest
                else -> ExploreType.Popular
            }
            add(
                BrowseViewModel(
                    preferencesUseCase = lookup<PreferencesUseCase>(),
                    source = mappingSourceNameToSource(sourceName),
                    exploreType = exploreType,
                    localBookRepository = lookup<LocalBookRepository>(),
                    remoteRepository = lookup<RemoteRepository>()

                ),
            )
        }

    }

}

class BookDetailFragment : ComposeFragment() {

    @Composable
    override fun FragmentComposable(backstack: Backstack) {
        BackstackProvider(backstack = backstack) {
            InfinityTheme {
                ProvideWindowInsets {
                    BookDetailScreen()
                }
            }
        }
    }
}

@Parcelize
data class BookDetailKey(val bookId:Int, val sourceName: String) :
    FragmentKey() {

    override fun instantiateFragment(): Fragment = BookDetailFragment()

    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(
                BookDetailViewModel(
                    source = mappingSourceNameToSource(sourceName),
                    bookId = bookId,
                    preferencesUseCase = lookup<PreferencesUseCase>(),
                    localBookRepository = lookup<LocalBookRepository>(),
                    remoteRepository = lookup<RemoteRepository>(),
                    localChapterRepository = lookup<LocalChapterRepository>()
                ),
            )
        }
    }
}

class WebViewFragment : ComposeFragment() {
    @Composable
    override fun FragmentComposable(backstack: Backstack) {
        BackstackProvider(backstack = backstack) {
            InfinityTheme {
                WebPageScreen()
            }
        }
    }
}

@Parcelize
data class WebViewKey(
    val url: String,
    val sourceName: String,
    val fetchType: Int,
    val bookName :  String?=null,
    val chapterName: String? = null,
) : FragmentKey() {

    override fun instantiateFragment(): Fragment = WebViewFragment()

    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add<WebViewPageModel>(WebViewPageModel(
                url,
                webView = lookup<WebView>(),
                source = mappingSourceNameToSource(sourceName),
                fetcher = mappingFetcherTypeWithIndex(fetchType),
                localChapterRepository = lookup<LocalChapterRepository>(),
                localBookRepository = lookup<LocalBookRepository>(),
                bookName = bookName,
                chapterTitle = chapterName,
            ))
        }
    }

}

class ChapterDetailFragment : ComposeFragment() {



    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    override fun FragmentComposable(backstack: Backstack) {
        BackstackProvider(backstack = backstack) {
            InfinityTheme {
                ChapterDetailScreen()
            }
        }
    }


}

@Parcelize
data class ChapterDetailKey(
    val bookName :  String,
    val sourceName: String,
) : FragmentKey() {
    override fun instantiateFragment(): Fragment = ChapterDetailFragment()
    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add<ChapterDetailViewModel>(ChapterDetailViewModel(
                source = mappingSourceNameToSource(sourceName),
                bookName = bookName,
                localChapterRepository = lookup<LocalChapterRepository>()

            ))
        }
    }
}

class ReaderScreenFragment : ComposeFragment() {

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    override fun FragmentComposable(backstack: Backstack) {
        BackstackProvider(backstack = backstack) {
            InfinityTheme {
                ReadingScreen()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val viewModel = backstack.lookup<ReaderScreenViewModel>()
        viewModel.readBrightness(context = context)
        viewModel.readOrientation(context = context)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        val activity = context?.findAppCompatAcivity()
        if (activity !=null) {
            val window = activity.window
            val layoutParams: WindowManager.LayoutParams = window.attributes
            layoutParams.screenBrightness = -1f
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            window.attributes = layoutParams
        }

    }


}

@Parcelize
data class ReaderScreenKey(
    val bookId: Int,
    val sourceName: String,
    val chapterId: Int,
) : FragmentKey() {

    override fun instantiateFragment(): Fragment = ReaderScreenFragment()

    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(ReaderScreenViewModel(
                preferencesUseCase = lookup<PreferencesUseCase>(),
                source = mappingSourceNameToSource(sourceName),
                bookId = bookId,
                chapterId = chapterId,
                localBookRepository = lookup<LocalBookRepository>(),
                remoteRepository = lookup<RemoteRepository>(),
                localChapterRepository = lookup<LocalChapterRepository>(),
            ))
        }
    }

}

class ExtensionScreenFragment : ComposeFragment() {
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun FragmentComposable(backstack: Backstack) {
        BackstackProvider(backstack = backstack) {
            InfinityTheme {
                ExtensionScreen()

            }

        }
    }
}


@Parcelize
data class ExtensionScreenKey(val noArgs: String = "") : FragmentKey() {
    override fun instantiateFragment(): Fragment = ExtensionScreenFragment()


}

class DownloadScreenFragment : ComposeFragment() {
    @Composable
    override fun FragmentComposable(backstack: Backstack) {
        BackstackProvider(backstack = backstack) {
            InfinityTheme {
                DownloaderScreen()
            }
        }
    }
}


@Parcelize
data class DownloadScreenKey(val noArgs: String = "") : FragmentKey() {
    override fun instantiateFragment(): Fragment = DownloadScreenFragment()
}

class ExtensionCreatorScreenFragment : ComposeFragment() {
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun FragmentComposable(backstack: Backstack) {
        BackstackProvider(backstack = backstack) {
            InfinityTheme {
                ExtensionCreatorScreen()

            }

        }
    }
}


@Parcelize
data class ExtensionCreatorScreenKey(val noArgs: String = "") : FragmentKey() {

    override fun instantiateFragment(): Fragment = ExtensionCreatorScreenFragment()

    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(ExtensionCreatorViewModel(

                backstack.lookup<LocalSourceRepository>()
            ))
        }
    }
}

class DnsOverHttpScreenFragment : ComposeFragment() {
    @Composable
    override fun FragmentComposable(backstack: Backstack) {
        BackstackProvider(backstack = backstack) {
            InfinityTheme {
                DnsOverHttpScreen()
            }
        }
    }
}


@Parcelize
data class DnsOverHttpScreenKey(val noArgs: String = "") : FragmentKey() {
    override fun instantiateFragment(): Fragment = DnsOverHttpScreenFragment()
    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(SettingViewModel(
                preferencesUseCase = lookup<PreferencesUseCase>(),
            ))
        }
    }
}

class AboutScreenFragment : ComposeFragment() {
    @Composable
    override fun FragmentComposable(backstack: Backstack) {
        BackstackProvider(backstack = backstack) {
            InfinityTheme {
                AboutSettingScreen()
            }
        }
    }
}


@Parcelize
data class AboutScreenKey(val noArgs: String = "") : FragmentKey() {
    override fun instantiateFragment(): Fragment = AboutScreenFragment()
    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(SettingViewModel(
                preferencesUseCase = lookup<PreferencesUseCase>(),
            ))
        }
    }
}