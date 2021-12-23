package ir.kazemcodes.infinity.presentation.book_detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun ButtonWithIconAndText(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    text: String,
    contentDescription: String = "an Icon",
    onClick : () -> Unit
) {
    Button(modifier = modifier,onClick = { onClick() },colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.background)) {
        Column(verticalArrangement = Arrangement.Center,horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = MaterialTheme.colors.onBackground
            )

            Text(
                text = text, style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onBackground
            )
        }
    }
}