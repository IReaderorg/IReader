package ireader.presentation.core.ui

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.web.WebContent
import ireader.presentation.core.ui.util.NavigationArgs
import ireader.presentation.ui.component.Controller
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
object WebViewScreenSpec : ScreenSpec {

    override val navHostRoute: String =
        "web_page_route/{url}/{sourceId}/{bookId}/{chapterId}/{enableChapterFetch}/{enableChaptersFetch}/{enableBookFetch}"

    override val arguments: List<NamedNavArgument> = listOf(
        navArgument("url") {
            type = NavType.StringType
            defaultValue = "No_Url"
        },
        navArgument("enableChapterFetch") {
            type = NavType.BoolType
            defaultValue = false
        },
        navArgument("enableChaptersFetch") {
            type = NavType.BoolType
            defaultValue = false
        },
        navArgument("enableBookFetch") {
            type = NavType.BoolType
            defaultValue = false
        },
        NavigationArgs.sourceId,
        NavigationArgs.chapterId,
        NavigationArgs.bookId,
        NavigationArgs.showModalSheet,
    )

    fun buildRoute(
        url: String? = null,
        sourceId: Long? = null,
        bookId: Long? = null,
        chapterId: Long? = null,
        enableChapterFetch: Boolean = false,
        enableChaptersFetch: Boolean = false,
        enableBookFetch: Boolean = false,
    ): String {
        return "web_page_route/${
        URLEncoder.encode(
            url,
            StandardCharsets.UTF_8.name()
        )
        }/${sourceId ?: 0}/${bookId ?: 0}/${chapterId ?: 0}/$enableChapterFetch/$enableChaptersFetch/$enableBookFetch".trim()
    }

    private fun Boolean.encodeBoolean(): String {
        return if (this) "true" else "false"
    }
    private fun String.decodeBoolean(): Boolean {
        return this == "true"
    }

    @Composable
    override fun Content(
        controller: Controller
    ) {
        val vm: WebViewPageModel = getViewModel(owner = controller.navBackStackEntry, parameters = {
            org.koin.core.parameter.parametersOf(
                WebViewPageModel.createParam(controller)
            )
        })
        val scope = rememberCoroutineScope()
        WebPageScreen(
            viewModel = vm,
            source = vm.source,
            snackBarHostState = controller.snackBarHostState,
            scaffoldPadding = controller.scaffoldPadding
        )
    }

    @Composable
    override fun TopBar(
        controller: Controller
    ) {
        val vm: WebViewPageModel = getViewModel(owner = controller.navBackStackEntry, parameters = {
            org.koin.core.parameter.parametersOf(
                WebViewPageModel.createParam(controller)
            )
        })
        val webView = vm.webViewManager.webView
        val url by remember {
            derivedStateOf { vm.webUrl }
        }
        val source = vm.source

        WebPageTopBar(
            scrollBehavior = controller.scrollBehavior,
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
                controller.navController.popBackStack()
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
}
