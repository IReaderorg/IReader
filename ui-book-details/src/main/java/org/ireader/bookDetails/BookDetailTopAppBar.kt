package org.ireader.bookDetails

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.ireader.components.components.Toolbar
import org.ireader.components.reusable_composable.TopAppBarBackButton
import org.ireader.core_api.source.CatalogSource
import org.ireader.core_api.source.HttpSource
import org.ireader.core_api.source.Source
import org.ireader.core_api.source.model.Command
import org.ireader.core_api.source.model.CommandList
import org.ireader.ui_book_details.R

@Composable
fun BookDetailTopAppBar(
    modifier: Modifier = Modifier,
    source: Source?,
    onWebView: () -> Unit,
    onRefresh: () -> Unit,
    onPopBackStack: () -> Unit,
    onCommand: () -> Unit,
    onShare:() -> Unit
) {
    Toolbar(
        title = {},
        backgroundColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        elevation = 0.dp,
        actions = {
            IconButton(onClick = {
                onRefresh()
            }) {
                Icon(
                    imageVector = Icons.Default.Autorenew,
                    contentDescription = stringResource(id = R.string.refresh),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            IconButton(onClick = {
                onShare()
            }) {
                Icon(
                    imageVector = Icons.Default.IosShare,
                    contentDescription = stringResource(id = R.string.share),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            if (source is CatalogSource && source.getCommands().any { it !is Command.Fetchers }) {
                IconButton(onClick = {
                    onCommand()
                }) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = stringResource(id = R.string.advance_commands),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }

            if (source is HttpSource) {
                IconButton(onClick = {
                    onWebView()
                }) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "WebView",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }

        },
        navigationIcon = {
            TopAppBarBackButton(onClick = onPopBackStack)
        }
    )
}

fun CommandList.emptyCommands(): Boolean {
    return this.none {
        it !is Command.Detail.Fetch ||
            it !is Command.Chapter.Fetch ||
            it !is Command.Content.Fetch
    }
}