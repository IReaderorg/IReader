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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.ireader.core.ChapterParse
import org.ireader.core.ChaptersParse
import org.ireader.core.DetailParse
import org.ireader.domain.FetchType
import org.ireader.presentation.R
import org.ireader.presentation.presentation.Toolbar
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
    fetchChapters: () -> Unit,
) {
    var isMenuExpanded by remember {
        mutableStateOf(false)
    }
    Toolbar(
        title = {
            CustomTextField(modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxHeight(.7f)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colors.onBackground.copy(.2f),
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
                if (source is HttpSource && source.getListings().map { it.name }
                        .contains(DetailParse().name)) {
                    DropdownMenuItem(onClick = {
                        isMenuExpanded = false
                        fetchBook()
                    }) {
                        MidSizeTextComposable(text = "Fetch Book")
                    }
                }
                if (source is HttpSource && source.getListings().map { it.name }
                        .contains(ChaptersParse().name)) {
                    DropdownMenuItem(onClick = {
                        isMenuExpanded = false
                        fetchChapters()
                    }) {
                        MidSizeTextComposable(text = "Fetch Chapters")
                    }
                }
                if (source is HttpSource && source.getListings().map { it.name }
                        .contains(ChapterParse().name)) {
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