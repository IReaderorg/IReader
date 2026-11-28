package ireader.presentation.core

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu as ComposeDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import ireader.presentation.ui.component.AnimationPolicy

/**
 * Drop-in replacement for Material3's [DropdownMenu] that pops open instantly in max performance
 * mode. Material3 animates scale/alpha internally via its own motion scheme, so bypass it entirely
 * when animations are disabled.
 */
@Composable
fun DropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    shape: Shape = MenuDefaults.shape,
    containerColor: Color = MenuDefaults.containerColor,
    tonalElevation: Dp = MenuDefaults.TonalElevation,
    shadowElevation: Dp = MenuDefaults.ShadowElevation,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!AnimationPolicy.animationsDisabled) {
        ComposeDropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            offset = offset,
            shape = shape,
            containerColor = containerColor,
            tonalElevation = tonalElevation,
            shadowElevation = shadowElevation,
            content = content,
        )
        return
    }

    if (!expanded) return

    val scrollState = rememberScrollState()
    val positionProvider = object : PopupPositionProvider {
        override fun calculatePosition(
            anchorBounds: IntRect,
            windowSize: IntSize,
            layoutDirection: LayoutDirection,
            popupContentSize: IntSize,
        ): IntOffset {
            val x = if (layoutDirection == LayoutDirection.Ltr) {
                anchorBounds.left + offset.x.value.toInt()
            } else {
                anchorBounds.right - popupContentSize.width - offset.x.value.toInt()
            }
            val y = anchorBounds.bottom + if (popupContentSize.height + anchorBounds.bottom > windowSize.height) {
                anchorBounds.top - popupContentSize.height - offset.y.value.toInt()
            } else {
                offset.y.value.toInt()
            }
            return IntOffset(x, y)
        }
    }

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
    ) {
        Surface(
            modifier = modifier.heightIn(max = 360.dp),
            shape = shape,
            color = containerColor,
            tonalElevation = tonalElevation,
            shadowElevation = shadowElevation,
        ) {
            Column(
                modifier = Modifier.verticalScroll(scrollState),
                content = content,
            )
        }
    }
}

/**
 * Performance-aware counterpart of Material3's exposed dropdown menu. The Material3 version uses
 * an animated [androidx.compose.animation.core.MutableTransitionState], so bypass it entirely in
 * max performance mode.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdownMenuBoxScope.PerformanceExposedDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    shape: Shape = MenuDefaults.shape,
    containerColor: Color = MenuDefaults.containerColor,
    tonalElevation: Dp = MenuDefaults.TonalElevation,
    shadowElevation: Dp = MenuDefaults.ShadowElevation,
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!AnimationPolicy.animationsDisabled) {
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            scrollState = scrollState,
            shape = shape,
            containerColor = containerColor,
            tonalElevation = tonalElevation,
            shadowElevation = shadowElevation,
            border = border,
            content = content,
        )
        return
    }

    if (!expanded) return

    val positionProvider = object : PopupPositionProvider {
        override fun calculatePosition(
            anchorBounds: IntRect,
            windowSize: IntSize,
            layoutDirection: LayoutDirection,
            popupContentSize: IntSize,
        ): IntOffset {
            val x = if (layoutDirection == LayoutDirection.Ltr) {
                anchorBounds.left
            } else {
                anchorBounds.right - popupContentSize.width
            }
            val y = if (popupContentSize.height + anchorBounds.bottom > windowSize.height) {
                anchorBounds.top - popupContentSize.height
            } else {
                anchorBounds.bottom
            }
            return IntOffset(x, y)
        }
    }

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
    ) {
        Surface(
            modifier = modifier.heightIn(max = 360.dp),
            shape = shape,
            color = containerColor,
            tonalElevation = tonalElevation,
            shadowElevation = shadowElevation,
            border = border,
        ) {
            Column(
                modifier = Modifier.verticalScroll(scrollState),
                content = content,
            )
        }
    }
}
