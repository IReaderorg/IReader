package org.ireader.presentation.feature_detail.presentation.book_detail

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.ireader.presentation.presentation.reusable_composable.TopAppBarBackButton

@Composable
fun BookDetailTopAppBar(
    modifier: Modifier = Modifier,
    isWebViewEnable: Boolean,
    navController: NavController,
    onWebView: () -> Unit,
    onRefresh: () -> Unit,
    onFetch: () -> Unit,
) {
    TopAppBar(
        title = {},
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        backgroundColor = Color.Transparent,
        contentColor = MaterialTheme.colors.onBackground,
        elevation = 0.dp,
        actions = {
            if (isWebViewEnable) {
                IconButton(onClick = {
                    onFetch()
                }) {
                    Icon(
                        imageVector = Icons.Default.TrackChanges,
                        contentDescription = "Get from webview",
                        tint = MaterialTheme.colors.onBackground,
                    )
                }
            }

            IconButton(onClick = {
                onRefresh()
            }) {
                Icon(
                    imageVector = Icons.Default.Autorenew,
                    contentDescription = "Refresh",
                    tint = MaterialTheme.colors.onBackground,
                )
            }
            IconButton(onClick = {
                onWebView()
            }) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = "WebView",
                    tint = MaterialTheme.colors.onBackground,
                )
            }
        },
        navigationIcon = {
            TopAppBarBackButton(navController = navController)
        }
    )
}