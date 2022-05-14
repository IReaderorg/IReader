package org.ireader.core_ui.ui_components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@Composable
fun CardTile(
    modifier: Modifier = Modifier,
    title: String = "",
    subtitle: String = "",
    trailing: @Composable RowScope.() -> Unit = {},
    onClick: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = modifier.clickable {
            onClick()
        }
    ) {
        Text(
            modifier = modifier,
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = subtitle, color = MaterialTheme.colorScheme.onBackground,
                style =MaterialTheme.typography.bodyMedium
            )
            trailing()
        }
    }
}
