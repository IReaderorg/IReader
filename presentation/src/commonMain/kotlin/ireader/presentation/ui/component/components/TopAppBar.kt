package ireader.presentation.ui.component.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.AppTextField
import ireader.presentation.ui.component.reusable_composable.BigSizeTextComposable
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import ireader.presentation.ui.core.theme.AppColors


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
    // Enhanced with better Material Design 3 patterns
    TopAppBar(
        title = title,
        modifier = if (applyInsets) modifier.statusBarsPadding() else modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor,
            titleContentColor = contentColor,
            scrolledContainerColor = backgroundColor,
            actionIconContentColor = contentColor,
            navigationIconContentColor = contentColor
        ),
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitleToolbar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    popBackStack: (() -> Unit)?
) {
    Toolbar(
        title = {
            BigSizeTextComposable(text = title)
        },
        navigationIcon = {
            if (popBackStack != null) {
                TopAppBarBackButton(onClick = { popBackStack() })
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
                    contentDescription = localize(Res.string.close),
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
                    contentDescription = localize(Res.string.search),
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
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = localize(Res.string.toggle_search_mode_off),
                    onClick = {
                        isSearchModeEnable = false
                        query = ""
                    }
                )
            } else {
                if (onPopBackStack != null) {
                    AppIconButton(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = localize(Res.string.toggle_search_mode_off),
                        onClick = {
                            onPopBackStack()
                        }
                    )
                }
            }
        }
    )
}
