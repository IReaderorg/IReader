package org.ireader.components.components

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
fun TitleText(
    text: String,
    color: ColorScheme? = null,
    style: FontStyle? = null,
    fontWeight: FontWeight? = null,
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onBackground,
        style =MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold
    )
}
