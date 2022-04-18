package org.ireader.presentation.presentation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.material.Surface
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.ireader.core_ui.theme.AppColors
import org.ireader.presentation.presentation.components.NoElevationOverlay


@Composable
fun Toolbar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = AppColors.current.bars,
    contentColor: Color = AppColors.current.onBars,
    elevation: Dp = 0.dp,
    applyInsets: Boolean = true,
) {
    CompositionLocalProvider(LocalElevationOverlay provides NoElevationOverlay) {
        // Wrap in another Surface to avoid drawing elevation between status bar and toolbar
        Surface(
            modifier = modifier,
            color = backgroundColor,
            contentColor = contentColor,
            elevation = elevation
        ) {
            TopAppBar(
                modifier = if (applyInsets) Modifier.statusBarsPadding() else Modifier,
                title = title,
                navigationIcon = navigationIcon,
                actions = actions,
                backgroundColor = backgroundColor,
                contentColor = contentColor,
                elevation = elevation,
            )
        }
    }
}
