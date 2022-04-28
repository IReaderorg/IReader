package org.ireader.app.components

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
import org.ireader.core_ui.ui.Colour.iconColor
import org.ireader.components.reusable_composable.MidSizeTextComposable


@Composable
fun TextIcon(title: String, icon: ImageVector, isEnable: Boolean, onClick: () -> Unit) {
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
        MidSizeTextComposable(text = title)
    }
}
