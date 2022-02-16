package org.ireader.presentation.feature_settings.presentation.setting.downloader

import androidx.compose.foundation.background
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownloadOff
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import org.ireader.presentation.R
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.TopAppBarActionButton


@Composable
fun DownloaderTopAppBar(
    navController: NavController,
    onStopAllDownload: () -> Unit,
    onMenuIcon: () -> Unit,
    onDeleteAllDownload: () -> Unit,
) {
    var isMenuExpanded by remember {
        mutableStateOf(false)
    }
    TopAppBar(
        title = {
            Text(
                text = "Downloads",
                color = MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis
            )
        },
        backgroundColor = MaterialTheme.colors.background,
        actions = {
            TopAppBarActionButton(
                imageVector = Icons.Default.FileDownloadOff,
                title = "Stop Download Icon",
                onClick = {
                    onStopAllDownload()
                },
            )
            TopAppBarActionButton(
                imageVector = Icons.Default.Menu,
                title = "Menu Icon",
                onClick = {
                    onMenuIcon()
                    isMenuExpanded = true
                },
            )
            DropdownMenu(
                modifier = Modifier.background(MaterialTheme.colors.background),
                expanded = isMenuExpanded,//viewModel.state.isMenuExpanded,
                onDismissRequest = {
                    isMenuExpanded = false
                    /**viewModel.toggleExpandMenu(false)**/
                },
            ) {
                DropdownMenuItem(onClick = {
                    isMenuExpanded = false
                    onDeleteAllDownload()
                    //viewModel.deleteAllDownloads()
                }) {
                    MidSizeTextComposable(text = stringResource(R.string.delete_all_downloads))
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "ArrowBack Icon",
                    tint = MaterialTheme.colors.onBackground,
                )
            }
        }
    )
}