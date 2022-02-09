package org.ireader.presentation.feature_library.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable


@Composable
fun CheckBoxWithText(title: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = isChecked, onCheckedChange = onCheckedChange)
        MidSizeTextComposable(title = title)
    }
}

