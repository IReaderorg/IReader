package org.ireader.components.text_related

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import org.ireader.components.reusable_composable.MidSizeTextComposable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextCheckBox(title: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = isChecked, onCheckedChange = onCheckedChange)
        MidSizeTextComposable(text = title)
    }
}
