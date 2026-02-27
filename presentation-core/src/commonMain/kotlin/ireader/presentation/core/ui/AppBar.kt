package ireader.presentation.core.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * IReader AppBar following Mihon's Material Design 3 patterns.
 * Provides consistent app bar styling with proper scroll behavior and theming.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit) = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    applyInsets: Boolean = false,
) {
    TopAppBar(
        title = title,
        modifier = if (applyInsets) modifier.statusBarsPadding() else modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(),
        scrollBehavior = scrollBehavior
    )
}

/**
 * Medium-sized IReader AppBar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediumAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit) = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    applyInsets: Boolean = false,
) {
    MediumTopAppBar(
        title = title,
        modifier = if (applyInsets) modifier.statusBarsPadding() else modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(),
        scrollBehavior = scrollBehavior
    )
}

/**
 * Large-sized IReader AppBar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LargeAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit) = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    applyInsets: Boolean = false,
) {
    LargeTopAppBar(
        title = title,
        modifier = if (applyInsets) modifier.statusBarsPadding() else modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(),
        scrollBehavior = scrollBehavior
    )
}

/**
 * Search-enabled IReader AppBar
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchAppBar(
    title: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit) = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    applyInsets: Boolean = false,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var isSearchActive by remember { mutableStateOf(false) }

    AppBar(
        title = {
            if (isSearchActive) {
                // Search TextField implementation would go here
                // For now, we'll use a placeholder
            } else {
                androidx.compose.material3.Text(title)
            }
        },
        modifier = modifier,
        navigationIcon = {
            if (isSearchActive) {
                ActionButton(
                    title = "Back",
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = {
                        isSearchActive = false
                        onSearchQueryChange("")
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                )
            } else {
                navigationIcon()
            }
        },
        actions = {
            if (isSearchActive) {
                ActionButton(
                    title = "Clear",
                    icon = Icons.Default.Close,
                    onClick = {
                        onSearchQueryChange("")
                    }
                )
            } else {
                ActionButton(
                    title = "Search",
                    icon = Icons.Default.Search,
                    onClick = {
                        isSearchActive = true
                    }
                )
                actions()
            }
        },
        scrollBehavior = scrollBehavior,
        applyInsets = applyInsets
    )
}