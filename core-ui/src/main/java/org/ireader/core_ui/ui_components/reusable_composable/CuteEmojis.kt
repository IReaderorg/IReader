package org.ireader.core_ui.ui_components.reusable_composable

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun NotImplementedText() {
    Column(modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text(
            text = "＼( °□° )／",
            style = MaterialTheme.typography.h3
            //fontSize = 200.dp
        )
        Spacer(modifier = Modifier.height(25.dp))
        Text(
            text = "Not Implemented yet.",
            style = MaterialTheme.typography.subtitle1
            //fontSize = 200.dp
        )
    }
}

@Composable
fun ErrorTextWithEmojis(modifier: Modifier = Modifier, error: String, textColor: Color? = null) {
    val sad_emojis = listOf<String>("ಥ_ಥ", "(╥﹏╥)", "(╥︣﹏᷅╥᷅)")
    Column(modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text(
            text = sad_emojis.random(),
            style = MaterialTheme.typography.h3,
            //fontSize = 200.dp
            textAlign = TextAlign.Center,
            color = textColor ?: MaterialTheme.colors.onBackground
        )
        Spacer(modifier = Modifier.height(25.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.subtitle1,
            textAlign = TextAlign.Center,
            color = textColor ?: MaterialTheme.colors.onBackground,
            overflow = TextOverflow.Ellipsis,
            maxLines = 3
            //fontSize = 200.dp
        )
    }
}

class CuteEmojis {
    val catty_emoji = "≧◉ᴥ◉≦"
    val happy_emoji = listOf<String>("≧◉ᴥ◉≦", "(ɔ◔‿◔)ɔ")
    val sad_emojis = listOf<String>("☉_☉", "(ㆆ_ㆆ)", "(╥︣﹏᷅╥)")
}
