package org.ireader.components.reusable_composable

import androidx.compose.foundation.background
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BuildDropDownMenu(
    items: List<DropDownMenuItem>,
    enable: Boolean = false,
    onEnable: (Boolean) -> Unit = {},
) {

    DropdownMenu(
        modifier = Modifier.background(MaterialTheme.colors.surface),
        expanded = enable,
        onDismissRequest = {
            onEnable(false)
        },
    ) {
        items.forEachIndexed { _, item ->
            DropdownMenuItem(onClick = {
                item.onItem()
                onEnable(false)
            }) {
                MidSizeTextComposable(text = item.text)
            }
        }
    }
}

data class DropDownMenuItem(
    val text: String,
    val onItem: () -> Unit,
)
