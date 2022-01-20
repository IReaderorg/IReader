package ir.kazemcodes.infinity.feature_activity.presentation

import android.content.Context
import android.content.pm.ActivityInfo
import android.view.WindowManager
import android.webkit.WebView
import androidx.compose.animation.ExperimentalAnimationApi
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
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.repository.LocalBookRepository
import ir.kazemcodes.infinity.core.domain.repository.LocalChapterRepository
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
import ir.kazemcodes.infinity.feature_settings.presentation.webview.WebPageScreen
import ir.kazemcodes.infinity.feature_settings.presentation.webview.WebViewPageModel
import ir.kazemcodes.infinity.feature_sources.presentation.extension.ExtensionScreen
import kotlinx.parcelize.Parcelize


class MainScreenFragment() : ComposeFragment() {
    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    override fun FragmentComposable(backstack: Backstack) {
        BackstackProvider(backstack = backstack) {
            InfinityTheme() {
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
        }
    }

    override fun instantiateFragment(): Fragment = MainScreenFragment()
}


class BrowserScreenFragment() : ComposeFragment() {

    @OptIn(ExperimentalPagingApi::class)
    @Composable
    override fun FragmentComposable(backstack: Backstack) {

        BackstackProvider(backstack = backstack) {
            InfinityTheme() {
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

class BookDetailFragment() : ComposeFragment() {

    @Composable
    override fun FragmentComposable(backstack: Backstack) {
        BackstackProvider(backstack = backstack) {
            InfinityTheme() {
                ProvideWindowInsets() {
                    BookDetailScreen()
                }
            }
        }
    }
}

@Parcelize
data class BookDetailKey(val book: Book, val sourceName: String, val isLocal: Boolean) :
    FragmentKey() {

    override fun instantiateFragment(): Fragment = BookDetailFragment()

    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(
                BookDetailViewModel(
                    source = mappingSourceNameToSource(sourceName),
                    book = book,
                    lookup<PreferencesUseCase>(),
                    isLocal = isLocal,
                    localBookRepository = lookup<LocalBookRepository>(),
                    remoteRepository = lookup<RemoteRepository>(),
                    localChapterRepository = lookup<LocalChapterRepository>()
                ),
            )
        }
    }
}

class WebViewFragment() : ComposeFragment() {
    @Composable
    override fun FragmentComposable(backstack: Backstack) {
        BackstackProvider(backstack = backstack) {
            InfinityTheme() {
                WebPageScreen()
            }
        }
    }
}

@Parcelize
data class WebViewKey(val url: String, val sourceName: String, val fetchType: Int,val book: Book?=null,val chapter:Chapter?=null) : FragmentKey() {

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
                bookName = book?.bookName,
                chapterTitle = chapter?.title,
            ))
        }
    }

}

class ChapterDetailFragment() : ComposeFragment() {


    @Composable
    override fun FragmentComposable(backstack: Backstack) {
        BackstackProvider(backstack = backstack) {
            InfinityTheme() {
                ChapterDetailScreen()
            }
        }
    }


}

@Parcelize
data class ChapterDetailKey(
    val book: Book,
    val sourceName: String,
) : FragmentKey() {
    override fun instantiateFragment(): Fragment = ChapterDetailFragment()
    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add<ChapterDetailViewModel>(ChapterDetailViewModel(
                source = mappingSourceNameToSource(sourceName),
                book = book,
                localChapterRepository = lookup<LocalChapterRepository>()

            ))
        }
    }
}

class ReaderScreenFragment() : ComposeFragment() {
    @Composable
    override fun FragmentComposable(backstack: Backstack) {
        BackstackProvider(backstack = backstack) {
            InfinityTheme() {
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
        val activity = context?.findAppCompatAcivity()!!
        val window = activity.window
        val layoutParams: WindowManager.LayoutParams = window.attributes
        layoutParams.screenBrightness = -1f
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        window.attributes = layoutParams
    }


}

@Parcelize
data class ReaderScreenKey(
    val bookName: String,
    val sourceName: String,
    val chapterName: String,
    val chapterIndex: Int,
) : FragmentKey() {

    override fun instantiateFragment(): Fragment = ReaderScreenFragment()

    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(ReaderScreenViewModel(
                preferencesUseCase = lookup<PreferencesUseCase>(),
                source = mappingSourceNameToSource(sourceName),
                bookName = bookName,
                chapterName = chapterName,
                chapterIndex = chapterIndex,
                localBookRepository = lookup<LocalBookRepository>(),
                remoteRepository = lookup<RemoteRepository>(),
                localChapterRepository = lookup<LocalChapterRepository>()
            ))
        }
    }

}

class ExtensionScreenFragment() : ComposeFragment() {
    @Composable
    override fun FragmentComposable(backstack: Backstack) {
        BackstackProvider(backstack = backstack) {
            InfinityTheme() {
                ExtensionScreen()

            }

        }
    }
}


@Parcelize
data class ExtensionScreenKey(val noArgs: String = "") : FragmentKey() {
    override fun instantiateFragment(): Fragment = ExtensionScreenFragment()
}

class DownloadScreenFragment() : ComposeFragment() {
    @Composable
    override fun FragmentComposable(backstack: Backstack) {
        BackstackProvider(backstack = backstack) {
            InfinityTheme() {
                DownloaderScreen()
            }
        }
    }
}


@Parcelize
data class DownloadScreenKey(val noArgs: String = "") : FragmentKey() {
    override fun instantiateFragment(): Fragment = DownloadScreenFragment()
}

class ExtensionCreatorScreenFragment() : ComposeFragment() {
    @Composable
    override fun FragmentComposable(backstack: Backstack) {
        BackstackProvider(backstack = backstack) {
            InfinityTheme() {
                ExtensionCreatorScreen()

            }

        }
    }
}


@Parcelize
data class ExtensionCreatorScreenKey(val noArgs: String = "") : FragmentKey() {

    override fun instantiateFragment(): Fragment = ExtensionCreatorScreenFragment()
}

class DnsOverHttpScreenFragment() : ComposeFragment() {
    @Composable
    override fun FragmentComposable(backstack: Backstack) {
        BackstackProvider(backstack = backstack) {
            InfinityTheme() {
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
class AboutScreenFragment() : ComposeFragment() {
    @Composable
    override fun FragmentComposable(backstack: Backstack) {
        BackstackProvider(backstack = backstack) {
            InfinityTheme() {
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