package org.ireader.core_ui.ui_components

import androidx.compose.foundation.layout.*
import androidx.compose.material.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ireader.components.reusable_composable.MidSizeTextComposable

@Composable
fun RadioButton(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit = {},
) {
    Row(modifier = modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start) {
        RadioButton(selected = selected, onClick = { onClick() })
        Spacer(modifier = modifier.width(2.dp))
        MidSizeTextComposable(text = text)
    }
}