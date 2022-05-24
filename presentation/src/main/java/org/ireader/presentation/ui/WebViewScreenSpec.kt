package org.ireader.presentation.ui

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.web.WebContent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.ireader.domain.ui.NavigationArgs
import org.ireader.web.WebPageBottomLayout
import org.ireader.web.WebPageEvents
import org.ireader.web.WebPageScreen
import org.ireader.web.WebPageTopBar
import org.ireader.web.WebViewPageModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalCoroutinesApi::class, ExperimentalMaterial3Api::class
)
object WebViewScreenSpec : ScreenSpec {

    override val navHostRoute: String =
        "web_page_route/{url}/{sourceId}/{bookId}/{chapterId}"

    override val arguments: List<NamedNavArgument> = listOf(
        navArgument("url") {
            type = NavType.StringType
            defaultValue = "No_Url"
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
    ): String {
        return "web_page_route/${
            URLEncoder.encode(
                url,
                StandardCharsets.UTF_8.name()
            )
        }/${sourceId ?: 0}/${bookId ?: 0}/${chapterId ?: 0}".trim()
    }

    @Composable
    override fun Content(
        controller: ScreenSpec.Controller
    ) {
        val vm: WebViewPageModel = hiltViewModel   (controller.navBackStackEntry)
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
            onFetchChapters = {

            },
            onFetchChapter = {

            },
            onFetchBook = {
                val book = vm.stateBook
                val source = vm.source
                if (source != null) {
                    vm.getDetails(
                        book = book,
                        webView = it,
                    )
                }
            },
            source = vm.source,
            snackBarHostState = controller.snackBarHostState,
            scaffoldPadding = controller.scaffoldPadding
        )
    }

    @Composable
    override fun TopBar(
        controller: ScreenSpec.Controller
    ) {
        val vm: WebViewPageModel = hiltViewModel   (controller.navBackStackEntry)
        val webView = vm.webView
        val source = vm.source
        WebPageTopBar(
            urlToRender = vm.url,
            onGo = {
                vm.webViewState?.content = WebContent.Url(vm.url)
                // webView.value?.loadUrl(viewModel.state.url)
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
                vm.updateUrl(it)
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

    @Composable
    override fun BottomModalSheet(
        controller: ScreenSpec.Controller
    ) {
        val vm: WebViewPageModel = hiltViewModel   (controller.navBackStackEntry)
        val webView = vm.webView
        val scope = rememberCoroutineScope()
        WebPageBottomLayout(
            onConfirm = {
                scope.launch {
                    controller.sheetState.hide()
                }
                webView?.let { webview ->
                    val book = vm.webBook
                    val chapter = vm.webChapter
                    val chapters = vm.webChapters
                    if (book != null) {
                        vm.insertBook(book.copy(favorite = true))
                    }
                    if (chapter != null) {
                        vm.insertChapter(chapter)
                    }
                    if (book != null) {
                        vm.insertChapters(chapters)
                    }
                }
            },
            onCancel = {
                scope.launch {
                    controller.sheetState.hide()
                }
                vm.onEvent(WebPageEvents.Cancel)
            },
            state = vm,
            onBook = { bookId ->
                vm.source?.let { source ->
                    controller.navController.navigate(
                        BookDetailScreenSpec.buildRoute(
                            source.id,
                            bookId = bookId
                        )
                    )
                }
            }
        )
    }
}
