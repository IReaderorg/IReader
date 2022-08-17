package org.ireader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import org.ireader.common_resources.UiText

@Composable
fun ClickableTextIcon(
    modifier: Modifier = Modifier,
    icon: @Composable ColumnScope.() -> Unit,
    text: UiText,
    contentDescription: String = "an Icon",
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        onClick = { onClick() },
        border = null,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
        shape = MaterialTheme.shapes.small,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon(this)
            Text(
                text = text.asString(LocalContext.current),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground,
                overflow = TextOverflow.Visible,
                maxLines = 1,
                textAlign = TextAlign.Center,

            )
        }
    }
}
