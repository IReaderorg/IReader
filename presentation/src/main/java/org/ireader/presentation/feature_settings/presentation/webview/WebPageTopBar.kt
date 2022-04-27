package org.ireader.presentation.feature_settings.presentation.webview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.ireader.presentation.R
import org.ireader.presentation.presentation.Toolbar
import org.ireader.core_ui.ui_components.reusable_composable.AppIconButton
import org.ireader.core_ui.ui_components.reusable_composable.BuildDropDownMenu
import org.ireader.core_ui.ui_components.reusable_composable.DropDownMenuItem
import org.ireader.core_ui.ui_components.reusable_composable.TopAppBarBackButton

@Composable
fun WebPageTopBar(
    navController: NavController,
    urlToRender: String,
    onValueChange: (text: String) -> Unit,
    onGo: () -> Unit,
    refresh: () -> Unit,
    goBack: () -> Unit,
    goForward: () -> Unit,
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
            val list =
                listOf<DropDownMenuItem>(
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
            BuildDropDownMenu(list, enable = isMenuExpanded, onEnable = { isMenuExpanded = it })
//            DropdownMenu(
//                modifier = Modifier.background(MaterialTheme.colors.background),
//                expanded = isMenuExpanded,//viewModel.state.isMenuExpanded,
//                onDismissRequest = {
//                    isMenuExpanded = false
//                },
//            ) {
//                DropdownMenuItem(onClick = {
//                    isMenuExpanded = false
//                    onGo()
//                }) {
//                    MidSizeTextComposable(text = stringResource(R.string.Go))
//                }
//                DropdownMenuItem(onClick = {
//                    isMenuExpanded = false
//                    refresh()
//                }) {
//                    MidSizeTextComposable(text = stringResource(R.string.refresh))
//                }
//                DropdownMenuItem(onClick = {
//                    isMenuExpanded = false
//                    goBack()
//                }) {
//                    MidSizeTextComposable(text = stringResource(R.string.go_back))
//                }
//                DropdownMenuItem(onClick = {
//                    isMenuExpanded = false
//                    goForward()
//                }) {
//                    MidSizeTextComposable(text = stringResource(R.string.go_forward))
//                }
//            }
        },
        )
}