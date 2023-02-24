package ireader.presentation.core.ui

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.accompanist.web.WebContent
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.core.ui.util.NavigationArgs
import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.web.WebPageScreen
import ireader.presentation.ui.web.WebPageTopBar
import ireader.presentation.ui.web.WebViewPageModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.compose.getViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalCoroutinesApi::class, ExperimentalMaterial3Api::class
)
data class WebViewScreenSpec(
    val url: String?,
    val sourceId: Long?,
    val bookId: Long?,
    val chapterId: Long?,
    val enableBookFetch: Boolean = false,
    val enableChapterFetch: Boolean = false,
    val enableChaptersFetch: Boolean = false,
) : VoyagerScreen() {


    @Composable
    override fun Content() {
        val vm: WebViewPageModel = getIViewModel(parameters = {
            org.koin.core.parameter.parametersOf(
                WebViewPageModel.Param(url,bookId,sourceId,chapterId,enableChapterFetch,enableChaptersFetch,enableBookFetch)
            )
        })
        val navigator = LocalNavigator.currentOrThrow

        val scope = rememberCoroutineScope()
        val host = SnackBarListener(vm)
        IScaffold(
            snackbarHostState = host,
            topBar = { scrollBehavior ->
                val webView = vm.webViewManager.webView
                val url by remember {
                    derivedStateOf { vm.webUrl }
                }
                val source = vm.source

                WebPageTopBar(
                    scrollBehavior = scrollBehavior,
                    urlToRender = url ?: vm.url,
                    onGo = {
                        vm.webViewState?.content = WebContent.Url(vm.webUrl)
                        vm.updateWebUrl(vm.url)
                    },
                    refresh = {
                        webView?.reload()
                    },
                    goBack = {
                        webView?.goBack()
                    },
                    goForward = {
                        webView?.goForward()
                    },
                    onValueChange = {
                        vm.webUrl = it
                    },
                    onPopBackStack = {
                        popBackStack(navigator)
                    },
                    source = source,
                    onFetchBook = {
                        webView?.let {
                            val book = vm.stateBook

                            if (source != null) {
                                vm.getDetails(
                                    book = book,
                                    webView = it,
                                )
                            }
                        }
                    },
                    onFetchChapter = {
                        webView?.let {
                            val chapter = vm.stateChapter

                            if (chapter != null && source != null) {
                                vm.getContentFromWebView(
                                    chapter = chapter,
                                    webView = it,
                                )
                            }
                        }
                    },
                    onFetchChapters = {
                        webView?.let {
                            val book = vm.stateBook
                            val source = vm.source
                            if (book != null && source != null) {
                                vm.getChapters(
                                    book = book,
                                    webView = it,
                                )
                            }
                        }
                    },
                    state = vm,
                )
            }
        ) { scaffoldPadding ->
            WebPageScreen(
                viewModel = vm,
                source = vm.source,
                snackBarHostState = host,
                scaffoldPadding = scaffoldPadding
            )
        }

    }
}
