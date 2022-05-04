package org.ireader.bookDetails

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.ireader.components.components.Toolbar
import org.ireader.components.reusable_composable.TopAppBarBackButton
import org.ireader.core_api.source.CatalogSource
import org.ireader.core_api.source.HttpSource
import org.ireader.core_api.source.Source

@Composable
fun BookDetailTopAppBar(
    modifier: Modifier = Modifier,
    source:Source?,
    onWebView: () -> Unit,
    onRefresh: () -> Unit,
    onPopBackStack: () -> Unit,
    onCommand:() -> Unit
) {
    Toolbar(
        title = {},
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 0.dp),
        backgroundColor = Color.Transparent,
        contentColor = MaterialTheme.colors.onBackground,
        elevation = 0.dp,
        actions = {
            IconButton(onClick = {
                onRefresh()
            }) {
                Icon(
                    imageVector = Icons.Default.Autorenew,
                    contentDescription = "Refresh",
                    tint = MaterialTheme.colors.onBackground,
                )
            }
            if(source is CatalogSource && source.getCommands().isNotEmpty()) {
                IconButton(onClick = {
                    onCommand()
                }) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Advance Command",
                        tint = MaterialTheme.colors.onBackground,
                    )
                }
            }

            if(source is HttpSource) {
                IconButton(onClick = {
                    onWebView()
                }) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "WebView",
                        tint = MaterialTheme.colors.onBackground,
                    )
                }
            }

        },
        navigationIcon = {
            TopAppBarBackButton(onClick = onPopBackStack)
        }
    )
}
