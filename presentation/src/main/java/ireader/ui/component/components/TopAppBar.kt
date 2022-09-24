package ireader.ui.component.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ireader.presentation.R
import ireader.ui.component.reusable_composable.AppIconButton
import ireader.ui.component.reusable_composable.AppTextField
import ireader.ui.component.reusable_composable.BigSizeTextComposable
import ireader.ui.component.reusable_composable.TopAppBarBackButton
import ireader.ui.core.theme.AppColors


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Toolbar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit) = {},
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = AppColors.current.bars,
    contentColor: Color = AppColors.current.onBars,
    elevation: Dp = 0.dp,
    applyInsets: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    Surface(
        modifier = modifier,
        color = backgroundColor,
        contentColor = contentColor,
        shadowElevation = elevation,
    ) {
        SmallTopAppBar(
            title = title,
            modifier = if (applyInsets) modifier.statusBarsPadding() else modifier,
            navigationIcon = navigationIcon,
            actions = actions,
            colors = TopAppBarDefaults.smallTopAppBarColors(
                containerColor = backgroundColor,
                titleContentColor = contentColor,
                scrolledContainerColor = backgroundColor,
                actionIconContentColor = contentColor,
                navigationIconContentColor =contentColor
            ),
            scrollBehavior = scrollBehavior,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitleToolbar(
    title: String,
    navController: NavController?,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    Toolbar(
        title = {
            BigSizeTextComposable(text = title)
        },
        navigationIcon = {
            if (navController != null) {
                TopAppBarBackButton(onClick = { navController.popBackStack() })
            } else {
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MidSizeToolbar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit) = {},
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = AppColors.current.bars,
    contentColor: Color = AppColors.current.onBars,
    elevation: Dp = 0.dp,
    applyInsets: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {

    Surface(
        modifier = modifier,
        color = backgroundColor,
        contentColor = contentColor,
        shadowElevation = elevation,
    ) {
        MediumTopAppBar(
            scrollBehavior = scrollBehavior,
            modifier = if (applyInsets) Modifier.statusBarsPadding() else Modifier,
            title = title,
            navigationIcon = navigationIcon,
            actions = actions,
            colors = TopAppBarDefaults.mediumTopAppBarColors(
                containerColor = backgroundColor,
                titleContentColor = contentColor,
            ),
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchToolbar(
    title: String,
    onSearch: ((String) -> Unit)? = null,
    onValueChange: ((String) -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit?)? = null,
    onPopBackStack: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var isSearchModeEnable by remember {
        mutableStateOf(false)
    }
    var query by remember {
        mutableStateOf("")
    }
    Toolbar(
        scrollBehavior = scrollBehavior,
        title = {
            if (!isSearchModeEnable) {
                BigSizeTextComposable(text = title)
            } else {
                AppTextField(
                    query = query,
                    onValueChange = { value ->
                        query = value
                        if (onValueChange != null) {
                            onValueChange(query)
                        }
                    },
                    onConfirm = {
                        if (onSearch != null) {
                            onSearch(query)
                        }
                        if (onValueChange != null) {
                            onValueChange(query)
                        }
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },

                )
            }
        },
        actions = {
            if (isSearchModeEnable) {
                AppIconButton(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.close),
                    onClick = {
                        isSearchModeEnable = false
                        query = ""
                        if (onValueChange != null) {
                            onValueChange(query)
                        }
                    },
                )
            } else {
                AppIconButton(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.search),
                    onClick = {
                        isSearchModeEnable = true
                        query = ""
                        if (onValueChange != null) {
                            onValueChange(query)
                        }
                    },
                )
                if (actions != null) {
                    actions()
                }
            }
        },
        navigationIcon = {
            if (isSearchModeEnable) {
                AppIconButton(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.toggle_search_mode_off),
                    onClick = {
                        isSearchModeEnable = false
                        query = ""
                    }
                )
            } else {
                if (onPopBackStack != null) {
                    AppIconButton(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.toggle_search_mode_off),
                        onClick = {
                            onPopBackStack()
                        }
                    )
                }
            }
        }
    )
}
