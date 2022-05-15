package org.ireader.presentation.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
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
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        snackBarHostState: SnackbarHostState,
        scaffoldPadding: PaddingValues,
        sheetState: ModalBottomSheetState,
        drawerState: DrawerState
    ) {
        val vm: WebViewPageModel = hiltViewModel(navBackStackEntry)
        val scope = rememberCoroutineScope()
        val bottomSheetState =
            rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
        WebPageScreen(
            viewModel = vm,
            onPopBackStack = {
                navController.popBackStack()
            },
            onModalBottomSheetHide = {
                scope.launch {
                    bottomSheetState.hide()

                }
            },
            onModalBottomSheetShow = {
                scope.launch {
                    bottomSheetState.show()
                }
            },
            modalBottomSheetState = bottomSheetState,
            onBookNavigation = { bookId ->
                vm.source?.let { source ->
                    navController.navigate(
                        BookDetailScreenSpec.buildRoute(
                            source.id,
                            bookId = bookId
                        )
                    )
                }
            },
            onModalSheetConfirm = { webview ->
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
            snackBarHostState = snackBarHostState
        )
    }
@Composable
    override fun TopBar(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        snackBarHostState: SnackbarHostState,
        sheetState: ModalBottomSheetState,
        drawerState: DrawerState
    ) {
    val vm: WebViewPageModel = hiltViewModel(navBackStackEntry)
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
                         navController.popBackStack()
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
                }}
        },
        onFetchChapter = {
            webView?.let {  val chapter = vm.stateChapter

                if (chapter != null && source != null) {
                    vm.getContentFromWebView(
                        chapter = chapter,
                        webView = it,
                    )
                } }
        },
        onFetchChapters = {
            webView?.let { val book = vm.stateBook
                val source = vm.source
                if (book != null && source != null) {
                    vm.getChapters(
                        book = book,
                        webView = it,
                    )
                } }
        },
        state = vm,
    )
    }
}
