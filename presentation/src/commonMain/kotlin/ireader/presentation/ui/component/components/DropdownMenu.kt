package ireader.presentation.ui.component.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Composable
fun IDropdownMenu(
    expanded: Boolean = false,
    onDismissRequest: () -> Unit = {},
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    content: @Composable() (ColumnScope.() -> Unit) = {}
) {
    androidx.compose.material3.DropdownMenu(expanded = expanded,onDismissRequest = onDismissRequest, modifier = modifier, offset = offset, content = content)
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
