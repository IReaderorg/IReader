package org.ireader.core_ui.ui_components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun ClickableTextIcon(
    modifier: Modifier = Modifier,
    icon: @Composable ColumnScope.() -> Unit,
    text: String,
    contentDescription: String = "an Icon",
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        onClick = { onClick() },
        border = BorderStroke(0.dp, MaterialTheme.colors.background),
        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.background)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon(this)
            Text(
                text = text,
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onBackground,
                overflow = TextOverflow.Visible,
                maxLines = 1,
                textAlign = TextAlign.Center,

            )
        }
    }
}
