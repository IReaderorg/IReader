package org.ireader.presentation.feature_settings.presentation.webview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.ireader.core.ChaptersParse
import org.ireader.core.DetailParse
import org.ireader.domain.FetchType
import org.ireader.presentation.R
import org.ireader.presentation.presentation.ToolBar
import org.ireader.presentation.presentation.reusable_composable.AppIconButton
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.TopAppBarBackButton
import tachiyomi.source.HttpSource
import tachiyomi.source.Source

@Composable
fun WebPageTopBar(
    navController: NavController,
    urlToRender: String,
    onValueChange: (text: String) -> Unit,
    fetchType: FetchType,
    source: Source? = null,
    onGo: () -> Unit,
    refresh: () -> Unit,
    goBack: () -> Unit,
    goForward: () -> Unit,
    fetchBook: () -> Unit,
    fetchChapter: () -> Unit,
) {
    var isMenuExpanded by remember {
        mutableStateOf(false)
    }
    ToolBar(
        title = {
            CustomTextField(modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxHeight(.7f)
                .fillMaxWidth()
                .background(
                    color = Color(0xFFD5D5D5),
                    shape = CircleShape
                ),
                value = urlToRender,
                onValueChange = {
                    onValueChange(it)
                },
                onValueConfirm = {

                })
        },
        navigationIcon = {
            TopAppBarBackButton(navController = navController)
        },
        actions = {
            AppIconButton(
                imageVector = Icons.Default.Menu,
                title = "Menu Icon",
                onClick = {
                    isMenuExpanded = true
                },
            )
            DropdownMenu(
                modifier = Modifier.background(MaterialTheme.colors.background),
                expanded = isMenuExpanded,//viewModel.state.isMenuExpanded,
                onDismissRequest = {
                    isMenuExpanded = false
                },
            ) {
                DropdownMenuItem(onClick = {
                    isMenuExpanded = false
                    onGo()
                }) {
                    MidSizeTextComposable(text = stringResource(R.string.Go))
                }
                DropdownMenuItem(onClick = {
                    isMenuExpanded = false
                    refresh()
                }) {
                    MidSizeTextComposable(text = stringResource(R.string.refresh))
                }
                DropdownMenuItem(onClick = {
                    isMenuExpanded = false
                    goBack()
                }) {
                    MidSizeTextComposable(text = stringResource(R.string.go_back))
                }
                DropdownMenuItem(onClick = {
                    isMenuExpanded = false
                    goForward()
                }) {
                    MidSizeTextComposable(text = stringResource(R.string.go_forward))
                }
                if (source is HttpSource && source.getListings().contains(DetailParse())) {
                    DropdownMenuItem(onClick = {
                        isMenuExpanded = false
                        fetchBook()
                    }) {
                        MidSizeTextComposable(text = "Fetch Book")
                    }
                }
                if (source is HttpSource && source.getListings().contains(ChaptersParse())) {
                    DropdownMenuItem(onClick = {
                        isMenuExpanded = false
                        fetchChapter()
                    }) {
                        MidSizeTextComposable(text = "Fetch Chapter")
                    }
                }
            }
        },
        backgroundColor = MaterialTheme.colors.background,

        )
}