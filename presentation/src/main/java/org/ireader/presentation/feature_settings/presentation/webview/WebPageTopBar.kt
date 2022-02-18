package org.ireader.presentation.feature_settings.presentation.webview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.ireader.domain.models.source.FetchType
import org.ireader.domain.models.source.Source
import org.ireader.presentation.R
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.TopAppBarActionButton
import org.ireader.presentation.presentation.reusable_composable.TopAppBarBackButton

@Composable
fun WebPageTopBar(
    navController: NavController,
    urlToRender: String,
    onTrackButtonClick: () -> Unit,
    fetchType: FetchType,
    source: Source? = null,
    refresh: () -> Unit,
    goBack: () -> Unit,
    goForward: () -> Unit,
    fetchBook: () -> Unit,
    fetchBooks: () -> Unit,
    fetchChapter: () -> Unit,
) {
    var isMenuExpanded by remember {
        mutableStateOf(false)
    }
    TopAppBar(
        title = {
            CustomTextField(modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxHeight(.7f)
                .fillMaxWidth()
                .background(
                    color = Color(0xffb9b9b9),
                    shape = CircleShape
                ), initValue = urlToRender, onValueConfirm = {

            })
        },
        navigationIcon = {
            TopAppBarBackButton(navController = navController)
        },
        actions = {
            if (fetchType == FetchType.DetailFetchType && source != null) {
                TopAppBarActionButton(imageVector = Icons.Default.TrackChanges,
                    title = "Menu",
                    onClick = {
                        onTrackButtonClick()

                    })
            }
            TopAppBarActionButton(
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
                DropdownMenuItem(onClick = {
                    isMenuExpanded = false
                    fetchBooks()
                }) {
                    MidSizeTextComposable(text = "Fetch Books")
                }
                DropdownMenuItem(onClick = {
                    isMenuExpanded = false
                    fetchBook()
                }) {
                    MidSizeTextComposable(text = "Fetch Book")
                }
                DropdownMenuItem(onClick = {
                    isMenuExpanded = false
                    fetchChapter()
                }) {
                    MidSizeTextComposable(text = "Fetch Chapter")
                }
            }
        },
        backgroundColor = MaterialTheme.colors.background,

        )
}