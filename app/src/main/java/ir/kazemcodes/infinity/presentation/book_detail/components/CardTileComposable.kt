package ir.kazemcodes.infinity.presentation.book_detail.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@Composable
fun CardTileComposable(
    modifier: Modifier = Modifier,
    title: String = "",
    subtitle: String = "",
    trailing: @Composable RowScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.Center) {
        Text(
            text = title,
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onBackground
        )
        Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = subtitle, color = MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.subtitle2
            )
            trailing()
        }
    }
}