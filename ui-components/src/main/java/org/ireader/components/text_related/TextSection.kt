package org.ireader.components.text_related

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import org.ireader.common_resources.UiText
import org.ireader.core_ui.theme.ContentAlpha

@Composable
fun TextSection(
    text: UiText,
    toUpper: Boolean = true,
    padding: PaddingValues = PaddingValues(16.dp),
    style: TextStyle = MaterialTheme.typography.displayMedium,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(padding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            if (toUpper) text.asString(LocalContext.current).uppercase() else text.asString(
                LocalContext.current),
            style = style,
            color = LocalContentColor.current.copy(alpha = ContentAlpha.medium())
        )
    }
}
