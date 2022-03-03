package org.ireader.presentation.feature_detail.presentation.book_detail.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable

@Composable
fun ButtonWithIconAndText(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    text: String,
    contentDescription: String = "an Icon",
    onClick: () -> Unit,
) {
    Button(modifier = modifier,
        onClick = { onClick() },
        border = BorderStroke(0.dp, MaterialTheme.colors.background),
        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.background)) {
        Column(verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = MaterialTheme.colors.onBackground
            )
            MidSizeTextComposable(
                text = text, style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onBackground,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}