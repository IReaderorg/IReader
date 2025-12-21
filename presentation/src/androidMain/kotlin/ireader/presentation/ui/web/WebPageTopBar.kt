package ireader.presentation.ui.web

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.core.source.findInstance
import ireader.core.source.model.Command
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.CustomTextField
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.reusable_composable.BuildDropDownMenu
import ireader.presentation.ui.component.reusable_composable.DropDownMenuItem
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebPageTopBar(
    state: WebViewPageState,
    urlToRender: String,
    onValueChange: (text: String) -> Unit,
    onGo: () -> Unit,
    refresh: () -> Unit,
    goBack: () -> Unit,
    goForward: () -> Unit,
    onPopBackStack: () -> Unit,
    source: ireader.core.source.CatalogSource?,
    onFetchBook: () -> Unit,
    onFetchChapter: () -> Unit,
    onFetchChapters: () -> Unit,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    Toolbar(
        scrollBehavior = scrollBehavior,
        title = {
            WebPageUrlField(
                urlToRender = urlToRender,
                onValueChange = onValueChange,
                onGo = onGo
            )
        },
        navigationIcon = {
            TopAppBarBackButton(onClick = onPopBackStack)
        },
        actions = {
            val menuItems = buildWebPageMenuItems(
                state = state,
                source = source,
                onGo = onGo,
                refresh = refresh,
                goBack = goBack,
                goForward = goForward,
                onFetchBook = onFetchBook,
                onFetchChapter = onFetchChapter,
                onFetchChapters = onFetchChapters
            )
            BuildDropDownMenu(menuItems)
        },
        modifier = modifier
    )
}

@Composable
private fun WebPageUrlField(
    urlToRender: String,
    onValueChange: (String) -> Unit,
    onGo: () -> Unit
) {
    CustomTextField(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxHeight(.7f)
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = CircleShape
            ),
        value = urlToRender,
        onValueChange = onValueChange,
        onValueConfirm = { _ -> onGo() }
    )
}

@Composable
private fun buildWebPageMenuItems(
    state: WebViewPageState,
    source: ireader.core.source.CatalogSource?,
    onGo: () -> Unit,
    refresh: () -> Unit,
    goBack: () -> Unit,
    goForward: () -> Unit,
    onFetchBook: () -> Unit,
    onFetchChapter: () -> Unit,
    onFetchChapters: () -> Unit
): List<DropDownMenuItem> {
    val baseMenuItems = listOf(
        DropDownMenuItem(localize(Res.string.go)) { onGo() },
        DropDownMenuItem(localize(Res.string.refresh)) { refresh() },
        DropDownMenuItem(localize(Res.string.go_back)) { goBack() },
        DropDownMenuItem(localize(Res.string.go_forward)) { goForward() }
    )
    
    val fetchMenuItems = buildList {
        if (canShowFetchBook(source, state)) {
            val fetchBookLabel = getFetchButtonLabel(
                baseLabel = localize(Res.string.fetch_book),
                fetchState = state.fetchBookState
            )
            add(DropDownMenuItem(fetchBookLabel) { onFetchBook() })
        }
        
        if (canShowFetchChapter(source, state)) {
            val fetchChapterLabel = getFetchButtonLabel(
                baseLabel = localize(Res.string.fetch_chapter),
                fetchState = state.fetchChapterState
            )
            add(DropDownMenuItem(fetchChapterLabel) { onFetchChapter() })
        }
        
        if (canShowFetchChapters(source, state)) {
            val fetchChaptersLabel = getFetchButtonLabel(
                baseLabel = localize(Res.string.fetch_chapters),
                fetchState = state.fetchChaptersState
            )
            add(DropDownMenuItem(fetchChaptersLabel) { onFetchChapters() })
        }
    }
    
    return baseMenuItems + fetchMenuItems
}

private fun canShowFetchBook(
    source: ireader.core.source.CatalogSource?,
    state: WebViewPageState
): Boolean {
    return source != null && 
           source.getCommands().findInstance<Command.Detail.Fetch>() != null && 
           state.enableBookFetch
}

private fun canShowFetchChapter(
    source: ireader.core.source.CatalogSource?,
    state: WebViewPageState
): Boolean {
    return source != null && 
           source.getCommands().findInstance<Command.Content.Fetch>() != null && 
           state.stateChapter != null && 
           state.enableChapterFetch
}

private fun canShowFetchChapters(
    source: ireader.core.source.CatalogSource?,
    state: WebViewPageState
): Boolean {
    return source != null && 
           source.getCommands().findInstance<Command.Chapter.Fetch>() != null && 
           state.stateBook != null && 
           state.enableChaptersFetch
}

@Composable
private fun getFetchButtonLabel(
    baseLabel: String,
    fetchState: FetchButtonState
): String {
    return when (fetchState) {
        is FetchButtonState.Fetching -> "$baseLabel (Loading...)"
        is FetchButtonState.Success -> "$baseLabel ✓"
        is FetchButtonState.Error -> "$baseLabel ✗"
        else -> baseLabel
    }
}
