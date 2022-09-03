package org.ireader.web

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.ireader.components.CustomTextField
import org.ireader.components.components.Toolbar
import org.ireader.components.reusable_composable.BuildDropDownMenu
import org.ireader.components.reusable_composable.DropDownMenuItem
import org.ireader.components.reusable_composable.TopAppBarBackButton
import org.ireader.core_api.source.CatalogSource
import org.ireader.core_api.source.findInstance
import org.ireader.core_api.source.model.Command
import org.ireader.ui_web.R

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
    source: CatalogSource?,
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
                        color = MaterialTheme.colorScheme.onBackground.copy(.2f),
                        shape = CircleShape
                    ),
                value = urlToRender,
                onValueChange = {
                    onValueChange(it)
                },
                onValueConfirm = {
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
                        stringResource(R.string.Go)
                    ) {
                        onGo()
                    },
                    DropDownMenuItem(
                        stringResource(R.string.refresh)
                    ) {
                        refresh()
                    },
                    DropDownMenuItem(
                        stringResource(R.string.go_back)
                    ) {
                        goBack()
                    },
                    DropDownMenuItem(
                        stringResource(R.string.go_forward)
                    ) {
                        goForward()
                    },
                )
            if (source != null && source.getCommands().findInstance<Command.Detail.Fetch>() != null && state.enableBookFetch) {
                list.add(
                    DropDownMenuItem(
                        stringResource(R.string.fetch_book)
                    ) {
                        onFetchBook()
                    }
                )
            }
            if (source != null && source.getCommands().findInstance<Command.Content.Fetch>() != null && state.stateChapter != null && state.enableChapterFetch) {
                list.add(
                    DropDownMenuItem(
                        stringResource(R.string.fetch_chapter)
                    ) {
                        onFetchChapter()
                    }
                )
            }
            if (source != null && source.getCommands().findInstance<Command.Chapter.Fetch>() != null && state.stateBook != null && state.enableChaptersFetch) {
                list.add(
                    DropDownMenuItem(
                        stringResource(R.string.fetch_chapters)
                    ) {
                        onFetchChapters()
                    }
                )
            }
            BuildDropDownMenu(list)
        },
    )
}
