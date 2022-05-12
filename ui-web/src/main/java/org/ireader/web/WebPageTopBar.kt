package org.ireader.web

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ireader.common_resources.UiText
import org.ireader.components.components.Toolbar
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.BuildDropDownMenu
import org.ireader.components.reusable_composable.DropDownMenuItem
import org.ireader.components.reusable_composable.TopAppBarBackButton
import org.ireader.core_api.source.CatalogSource
import org.ireader.core_api.source.model.Command
import org.ireader.explore.webview.CustomTextField

@Composable
fun WebPageTopBar(
    state:WebViewPageState,
    urlToRender: String,
    onValueChange: (text: String) -> Unit,
    onGo: () -> Unit,
    refresh: () -> Unit,
    goBack: () -> Unit,
    goForward: () -> Unit,
    onPopBackStack: () -> Unit,
    source: CatalogSource?,
    onFetchBook:() -> Unit,
    onFetchChapter:() -> Unit,
    onFetchChapters:() -> Unit,
) {

    var isMenuExpanded by remember {
        mutableStateOf(false)
    }
    Toolbar(
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
            AppIconButton(
                imageVector = Icons.Default.Menu,
                text =   UiText.StringResource(org.ireader.core.R.string.menu),
                onClick = {
                    isMenuExpanded = true
                },
            )
            val list =
                mutableListOf<DropDownMenuItem>(
                    DropDownMenuItem(
                        UiText.StringResource(org.ireader.core.R.string.Go)
                    ) {
                        onGo()
                    },
                    DropDownMenuItem(
                        UiText.StringResource(org.ireader.core.R.string.refresh)
                    ) {
                        refresh()
                    },
                    DropDownMenuItem(
                        UiText.StringResource(org.ireader.core.R.string.go_back)
                    ) {
                        goBack()
                    },
                    DropDownMenuItem(
                        UiText.StringResource(org.ireader.core.R.string.go_forward)
                    ) {
                        goForward()
                    },
                )
            if (source != null && source.getCommands().find { it is Command.Detail.Fetch } != null && state.stateBook == null ) {
                list.add(
                    DropDownMenuItem(
                        UiText.StringResource(org.ireader.core.R.string.fetch_book)
                    ) {
                        onFetchBook()
                    }
                )
            }
            if (source != null && source.getCommands().find { it is Command.Content.Fetch } != null && state.stateChapter != null) {
                list.add(
                    DropDownMenuItem(
                        UiText.StringResource(org.ireader.core.R.string.fetch_chapter)
                    ) {
                        onFetchChapter()
                    }
                )
            }
            if (source != null && source.getCommands().find { it is Command.Chapter.Fetch} != null&& state.stateBook != null) {
                list.add(
                    DropDownMenuItem(
                        UiText.StringResource(org.ireader.core.R.string.fetch_chapters)
                    ) {
                        onFetchChapters()
                    }
                )
            }
            BuildDropDownMenu(list, enable = isMenuExpanded, onEnable = { isMenuExpanded = it })
        },
    )
}
