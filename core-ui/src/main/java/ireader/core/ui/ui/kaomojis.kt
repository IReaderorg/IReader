package ireader.core.ui.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.core.ui.theme.ContentAlpha

/** All credit belongs to tachiyomi**/
val kaomojis = listOf(
    "(･o･;)",
    "Σ(ಠ_ಠ)",
    "ಥ_ಥ",
    "(˘･_･˘)",
    "(；￣Д￣)",
    "(･Д･。"
)

@Composable
fun EmptyScreen(
    modifier: Modifier = Modifier,
    text: String,
) {
    val kaomoji = remember { kaomojis.random() }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = kaomoji,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = LocalContentColor.current.copy(alpha = ContentAlpha.medium()),
                fontSize = 48.sp
            ),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = LocalContentColor.current.copy(alpha = ContentAlpha.medium())
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp)
        )
    }
}
