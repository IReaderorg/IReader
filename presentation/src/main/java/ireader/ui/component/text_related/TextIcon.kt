package ireader.ui.component.text_related

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ireader.ui.component.reusable_composable.MidSizeTextComposable
import ireader.core.ui.ui.Colour.iconColor

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
        Icon(
            imageVector = icon,
            contentDescription = "$title icon",
            tint = if (isEnable) MaterialTheme.colorScheme.iconColor else Color.Transparent
        )
        Spacer(modifier = Modifier.width(8.dp))
        MidSizeTextComposable(text = title)
    }
}
