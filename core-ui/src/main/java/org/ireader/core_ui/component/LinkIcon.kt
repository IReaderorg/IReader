package org.ireader.core_ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import org.ireader.core_ui.theme.AppColors

@Composable
fun LinkIcon(
    modifier: Modifier = Modifier,
    label: String,
    painter: Painter? = null,
    icon:ImageVector? =null,
    url: String,
) {
    val uriHandler = LocalUriHandler.current
    LinkIcon(modifier, label, painter,icon) { uriHandler.openUri(url) }
}

@Composable
fun LinkIcon(
    modifier: Modifier = Modifier,
    label: String,
    painter: Painter?=null,
    icon:ImageVector?=null,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = modifier.padding(4.dp),
        onClick = onClick,
    ) {
        if (painter != null) {
            Icon(
                painter = painter,
                tint = AppColors.current.primary,
                contentDescription = label,
            )
        }else if (icon != null) {
            Icon(
                imageVector = icon,
                tint = AppColors.current.primary,
                contentDescription = label,
            )
        }

    }
}
