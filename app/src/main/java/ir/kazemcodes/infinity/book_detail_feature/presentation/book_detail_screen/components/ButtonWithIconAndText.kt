package ir.kazemcodes.infinity.book_detail_feature.presentation.book_detail_screen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun ButtonWithIconAndText(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    text: String,
    contentDescription: String = "an Icon"
) {

    Column(modifier = modifier, verticalArrangement = Arrangement.Center,horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            modifier = modifier,
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