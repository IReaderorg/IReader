package org.ireader.components.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.ireader.core_ui.theme.AppColors

@Composable
fun Toolbar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit) = {},
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = AppColors.current.bars,
    contentColor: Color = AppColors.current.onBars,
    elevation: Dp = 0.dp,
    applyInsets: Boolean = true,
) {

    Surface(
        modifier = modifier,
        color = backgroundColor,
        contentColor = contentColor,
        shadowElevation = elevation,
    ) {
        SmallTopAppBar(
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

@Composable
fun MidSizeToolbar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit) = {},
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = AppColors.current.bars,
    contentColor: Color = AppColors.current.onBars,
    elevation: Dp = 0.dp,
    applyInsets: Boolean = true,
) {

    Surface(
        modifier = modifier,
        color = backgroundColor,
        contentColor = contentColor,
        shadowElevation = elevation,
    ) {
        MediumTopAppBar(
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
