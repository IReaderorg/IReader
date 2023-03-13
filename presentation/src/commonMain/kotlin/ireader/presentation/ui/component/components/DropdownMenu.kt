package ireader.presentation.ui.component.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Composable
expect fun DropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    offset: DpOffset,
    content: @Composable() (ColumnScope.() -> Unit)
)
@Composable
expect fun DropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? ,
    trailingIcon: @Composable (() -> Unit)? ,
    enabled: Boolean ,
    colors: MenuItemColors,
    contentPadding: PaddingValues ,
    interactionSource: MutableInteractionSource,
)

@Composable
fun IDropdownMenu(
    expanded: Boolean = false,
    onDismissRequest: () -> Unit = {},
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    content: @Composable() (ColumnScope.() -> Unit) = {}
) {
    DropdownMenu(expanded, onDismissRequest, modifier, offset, content)
}
@Composable
fun IDropdownMenuItem(
    text: @Composable () -> Unit = {},
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: MenuItemColors = MenuDefaults.itemColors(),
    contentPadding: PaddingValues = MenuDefaults.DropdownMenuItemContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    DropdownMenuItem(
        text, onClick, modifier, leadingIcon, trailingIcon, enabled, colors, contentPadding,interactionSource
    )
}