package org.ireader.web

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.ireader.components.components.Toolbar
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.BuildDropDownMenu
import org.ireader.components.reusable_composable.DropDownMenuItem
import org.ireader.components.reusable_composable.TopAppBarBackButton
import org.ireader.explore.webview.CustomTextField

@Composable
fun WebPageTopBar(
    urlToRender: String,
    onValueChange: (text: String) -> Unit,
    onGo: () -> Unit,
    refresh: () -> Unit,
    goBack: () -> Unit,
    goForward: () -> Unit,
    onPopBackStack: () -> Unit,
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
                        color = MaterialTheme.colors.onBackground.copy(.2f),
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
                title = "Menu Icon",
                onClick = {
                    isMenuExpanded = true
                },
            )
            val list =
                listOf<DropDownMenuItem>(
                    DropDownMenuItem(
                        stringResource(org.ireader.core.R.string.Go)
                    ) {
                        onGo()
                    },
                    DropDownMenuItem(
                        stringResource(org.ireader.core.R.string.refresh)
                    ) {
                        refresh()
                    },
                    DropDownMenuItem(
                        stringResource(org.ireader.core.R.string.go_back)
                    ) {
                        goBack()
                    },
                    DropDownMenuItem(
                        stringResource(org.ireader.core.R.string.go_forward)
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
