package org.ireader.core_ui.ui_components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun TextSection(
    text: String,
    toUpper: Boolean = true,
    padding: PaddingValues = PaddingValues(16.dp),
    style: TextStyle = MaterialTheme.typography.subtitle2,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(padding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            if (toUpper) text.uppercase() else text,
            style = style,
            color = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
        )
    }
}
