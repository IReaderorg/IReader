package ireader.presentation.ui.component

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import ireader.i18n.UiText
import ireader.i18n.asString
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

@Composable
fun ClickableTextIcon(
        modifier: Modifier = Modifier,
        icon: @Composable ColumnScope.() -> Unit,
        text: UiText,
        contentDescription: String = "an Icon",
        onClick: () -> Unit,
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
                text = text.asString(localizeHelper),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground,
                overflow = TextOverflow.Visible,
                maxLines = 1,
                textAlign = TextAlign.Center,
            )
        }
    }
}
