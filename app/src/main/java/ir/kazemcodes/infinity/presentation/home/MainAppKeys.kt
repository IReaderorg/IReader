package ir.kazemcodes.infinity.presentation.home

import android.content.Context
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.fragment.app.Fragment
import com.zhuinden.simplestack.Backstack
import com.zhuinden.simplestack.ServiceBinder
import com.zhuinden.simplestackcomposeintegration.core.BackstackProvider
import com.zhuinden.simplestackextensions.fragmentsktx.backstack
import com.zhuinden.simplestackextensions.servicesktx.add
import com.zhuinden.simplestackextensions.servicesktx.lookup
import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.domain.models.remote.Chapter
import ir.kazemcodes.infinity.domain.use_cases.local.LocalUseCase
import ir.kazemcodes.infinity.domain.use_cases.preferences.PreferencesUseCase
import ir.kazemcodes.infinity.domain.use_cases.remote.RemoteUseCase
import ir.kazemcodes.infinity.presentation.book_detail.BookDetailScreen
import ir.kazemcodes.infinity.presentation.book_detail.BookDetailViewModel
import ir.kazemcodes.infinity.presentation.browse.BrowseViewModel
import ir.kazemcodes.infinity.presentation.browse.BrowserScreen
import ir.kazemcodes.infinity.presentation.chapter_detail.ChapterDetailScreen
import ir.kazemcodes.infinity.presentation.chapter_detail.ChapterDetailViewModel
import ir.kazemcodes.infinity.presentation.extension.ExtensionScreen
import ir.kazemcodes.infinity.presentation.home.core.ComposeFragment
import ir.kazemcodes.infinity.presentation.home.core.FragmentKey
import ir.kazemcodes.infinity.presentation.library.LibraryViewModel
import ir.kazemcodes.infinity.presentation.reader.ReaderScreenViewModel
import ir.kazemcodes.infinity.presentation.reader.ReadingScreen
import ir.kazemcodes.infinity.presentation.setting.SettingViewModel
import ir.kazemcodes.infinity.presentation.setting.dns.DnsOverHttpScreen
import ir.kazemcodes.infinity.presentation.setting.extension_creator.ExtensionCreatorScreen
import ir.kazemcodes.infinity.presentation.theme.InfinityTheme
import ir.kazemcodes.infinity.presentation.webview.WebPageScreen
import ir.kazemcodes.infinity.util.SourceMapper
import ir.kazemcodes.infinity.util.findAppCompatAcivity
import kotlinx.parcelize.Parcelize


class MainScreenFragment() : ComposeFragment() {
    @Composable
    override fun FragmentComposable(backstack: Backstack) {

        val viewModel = remember { backstack.lookup<MainViewModel>() }
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
            add(LibraryViewModel(lookup<LocalUseCase>(), lookup<PreferencesUseCase>()))
            add(MainViewModel())
        }
    }

    override fun instantiateFragment(): Fragment = MainScreenFragment()
}


class BrowserScreenFragment() : ComposeFragment() {

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
data class BrowserScreenKey(val sourceName: String, val isLatestUpdateMode: Boolean = true) :
    FragmentKey() {
    override fun instantiateFragment(): Fragment = BrowserScreenFragment()
    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(
                BrowseViewModel(lookup<LocalUseCase>(),
                    lookup<RemoteUseCase>(),
                    preferencesUseCase = lookup<PreferencesUseCase>(),
                    source = lookup<SourceMapper>().mappingSourceNameToSource(sourceName),
                    isLatestUpdateMode = isLatestUpdateMode),
            )
        }

    }

}

class BookDetailFragment() : ComposeFragment() {

    @Composable
    override fun FragmentComposable(backstack: Backstack) {
        BackstackProvider(backstack = backstack) {
            InfinityTheme() {
                BookDetailScreen()
            }
        }
    }
}

@Parcelize
data class BookDetailKey(val book: Book, val sourceName: String) : FragmentKey() {

    override fun instantiateFragment(): Fragment = BookDetailFragment()

    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(BookDetailViewModel(lookup<LocalUseCase>(),
                lookup<RemoteUseCase>(),
                source = lookup<SourceMapper>().mappingSourceNameToSource(sourceName),
                book = book,
                lookup<PreferencesUseCase>()))
        }
    }
}

class WebViewFragment(val url: String) : ComposeFragment() {
    @Composable
    override fun FragmentComposable(backstack: Backstack) {
        BackstackProvider(backstack = backstack) {
            InfinityTheme() {
                WebPageScreen(url)

            }

        }
    }
}

@Parcelize
data class WebViewKey(val url: String) : FragmentKey() {

    override fun instantiateFragment(): Fragment = WebViewFragment(url = url)
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
    val sourceName: String
) : FragmentKey() {
    override fun instantiateFragment(): Fragment = ChapterDetailFragment()
    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add<ChapterDetailViewModel>(ChapterDetailViewModel(
                lookup<LocalUseCase>(),
                source = lookup<SourceMapper>().mappingSourceNameToSource(sourceName),
                book = book,
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
        viewModel.readOrientation(context=context)
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
    val book: Book,
    val chapterIndex: Int,
    val chapter: Chapter,
    val chapters: List<Chapter>,
    val sourceName: String,
) : FragmentKey() {

    override fun instantiateFragment(): Fragment = ReaderScreenFragment()

    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(ReaderScreenViewModel(
                localUseCase = lookup<LocalUseCase>(),
                remoteUseCase = lookup<RemoteUseCase>(),
                preferencesUseCase = lookup<PreferencesUseCase>(),
                source = lookup<SourceMapper>().mappingSourceNameToSource(sourceName),
                book = book,
                chapter = chapter,
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
                ExtensionScreenKey()

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