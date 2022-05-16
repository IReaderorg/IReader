package org.ireader.components.reusable_composable

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.ireader.common_resources.UiText

@Composable
fun BuildDropDownMenu(
    items: List<DropDownMenuItem>,
    enable: Boolean = false,
    onEnable: (Boolean) -> Unit = {},
) {

    DropdownMenu(
        modifier = Modifier,
        expanded = enable,
        onDismissRequest = {
            onEnable(false)
        },
    ) {
        items.forEachIndexed { _, item ->
            DropdownMenuItem(onClick = {
                item.onItem()
                onEnable(false)
            },
                text = { MidSizeTextComposable(text = item.text)},
            )
        }
    }
}

data class DropDownMenuItem(
    val text: UiText,
    val onItem: () -> Unit,
)
