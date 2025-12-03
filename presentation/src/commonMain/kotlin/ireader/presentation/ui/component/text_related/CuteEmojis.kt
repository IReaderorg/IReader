package ireader.presentation.ui.component.text_related

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun ErrorTextWithEmojis(modifier: Modifier = Modifier, error: String, textColor: Color? = null) {
    val sad_emojis = listOf<String>("ಥ_ಥ", "(╥﹏╥)", "(╥︣﹏᷅╥᷅)")
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = sad_emojis.random(),
            style = MaterialTheme.typography.headlineMedium,
            // fontSize = 200.dp
            textAlign = TextAlign.Center,
            color = textColor ?: MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(25.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = textColor ?: MaterialTheme.colorScheme.onBackground,
            overflow = TextOverflow.Ellipsis,
            maxLines = 3
            // fontSize = 200.dp
        )
    }
}
