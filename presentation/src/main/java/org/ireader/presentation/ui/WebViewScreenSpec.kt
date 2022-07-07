package org.ireader.presentation.ui

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.web.WebContent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.ireader.domain.ui.NavigationArgs
import org.ireader.web.WebPageScreen
import org.ireader.web.WebPageTopBar
import org.ireader.web.WebViewPageModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import org.ireader.Controller

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
        }/${sourceId ?: 0}/${bookId ?: 0}/${chapterId ?: 0}/${enableChapterFetch}/${enableChaptersFetch}/${enableBookFetch}".trim()
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
        val vm: WebViewPageModel = hiltViewModel(controller.navBackStackEntry)
        val scope = rememberCoroutineScope()
        WebPageScreen(
            viewModel = vm,
            onPopBackStack = {
                controller.navController.popBackStack()
            },
            onModalBottomSheetHide = {
                scope.launch {
                    controller.sheetState.hide()
                }
            },
            onModalBottomSheetShow = {
                scope.launch {
                    controller.sheetState.show()
                }
            },
            source = vm.source,
            snackBarHostState = controller.snackBarHostState,
            scaffoldPadding = controller.scaffoldPadding
        )
    }

    @Composable
    override fun TopBar(
        controller: Controller
    ) {
        val vm: WebViewPageModel = hiltViewModel(controller.navBackStackEntry)
        val webView = vm.webView
        val url by derivedStateOf { vm.webUrl }
        val source = vm.source

        WebPageTopBar(
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
