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
import ireader.i18n.resources.MR
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
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    Toolbar(
        scrollBehavior = scrollBehavior,
        title = {
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
                onValueChange = {
                    onValueChange(it)
                },
                onValueConfirm = {
                    onGo()
                }
            )
        },
        navigationIcon = {
            TopAppBarBackButton(onClick = onPopBackStack)
        },
        actions = {
            val list =
                mutableListOf<DropDownMenuItem>(
                    DropDownMenuItem(
                        localize(MR.strings.go)
                    ) {
                        onGo()
                    },
                    DropDownMenuItem(
                        localize(MR.strings.refresh)
                    ) {
                        refresh()
                    },
                    DropDownMenuItem(
                        localize(MR.strings.go_back)
                    ) {
                        goBack()
                    },
                    DropDownMenuItem(
                        localize(MR.strings.go_forward)
                    ) {
                        goForward()
                    },
                )
            // Fetch buttons are always enabled regardless of page load state
            if (source != null && source.getCommands().findInstance<Command.Detail.Fetch>() != null && state.enableBookFetch) {
                val fetchBookLabel = when (state.fetchBookState) {
                    is FetchButtonState.Fetching -> "${localize(MR.strings.fetch_book)} (Loading...)"
                    is FetchButtonState.Success -> "${localize(MR.strings.fetch_book)} ✓"
                    is FetchButtonState.Error -> "${localize(MR.strings.fetch_book)} ✗"
                    else -> localize(MR.strings.fetch_book)
                }
                list.add(
                    DropDownMenuItem(fetchBookLabel) {
                        onFetchBook()
                    }
                )
            }
            if (source != null && source.getCommands().findInstance<Command.Content.Fetch>() != null && state.stateChapter != null && state.enableChapterFetch) {
                val fetchChapterLabel = when (state.fetchChapterState) {
                    is FetchButtonState.Fetching -> "${localize(MR.strings.fetch_chapter)} (Loading...)"
                    is FetchButtonState.Success -> "${localize(MR.strings.fetch_chapter)} ✓"
                    is FetchButtonState.Error -> "${localize(MR.strings.fetch_chapter)} ✗"
                    else -> localize(MR.strings.fetch_chapter)
                }
                list.add(
                    DropDownMenuItem(fetchChapterLabel) {
                        onFetchChapter()
                    }
                )
            }
            if (source != null && source.getCommands().findInstance<Command.Chapter.Fetch>() != null && state.stateBook != null && state.enableChaptersFetch) {
                val fetchChaptersLabel = when (state.fetchChaptersState) {
                    is FetchButtonState.Fetching -> "${localize(MR.strings.fetch_chapters)} (Loading...)"
                    is FetchButtonState.Success -> "${localize(MR.strings.fetch_chapters)} ✓"
                    is FetchButtonState.Error -> "${localize(MR.strings.fetch_chapters)} ✗"
                    else -> localize(MR.strings.fetch_chapters)
                }
                list.add(
                    DropDownMenuItem(fetchChaptersLabel) {
                        onFetchChapters()
                    }
                )
            }
            BuildDropDownMenu(list)
        },
    )
}
