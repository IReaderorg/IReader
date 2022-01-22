package ir.kazemcodes.infinity.feature_library.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ir.kazemcodes.infinity.core.presentation.reusable_composable.MidSizeTextComposable
import ir.kazemcodes.infinity.core.presentation.theme.Colour.iconColor


@Composable
fun IconWithText(title: String, icon: ImageVector, isEnable: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon,
            contentDescription = "$title icon",
            tint = if (isEnable) MaterialTheme.colors.iconColor else Color.Transparent)
        Spacer(modifier = Modifier.width(8.dp))
        MidSizeTextComposable(title = title)
    }
}
