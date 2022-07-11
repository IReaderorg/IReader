package org.ireader.components.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.Switch
import androidx.compose.material.SwitchColors
import androidx.compose.material.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun MaterialSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: SwitchColors = SwitchDefaults.colors()
) {
    Switch(
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier,
        colors = colors,
        checked = checked,
        onCheckedChange = onCheckedChange

    )
}
