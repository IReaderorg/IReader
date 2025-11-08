package ireader.presentation.ui.component.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.core.theme.AppColors


@Composable
fun LinkIcon(
    modifier: Modifier = Modifier,
    label: String,
    painter: Painter? = null,
    icon: ImageVector? = null,
    url: String,
) {
    val uriHandler = LocalUriHandler.current
    LinkIcon(modifier, label, painter, icon) { uriHandler.openUri(url) }
}

@Composable
fun LinkIcon(
    modifier: Modifier = Modifier,
    label: String,
    painter: Painter? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    // Enhanced IconButton with larger touch target and better visual feedback
    IconButton(
        modifier = modifier
            .padding(8.dp) // Increased padding for better spacing
            .size(56.dp), // Explicit size for consistent touch targets (minimum 48dp)
        onClick = onClick,
    ) {
        // Background circle for better visual feedback
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
                ),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            if (painter != null) {
                Icon(
                    painter = painter,
                    tint = AppColors.current.primary,
                    contentDescription = label,
                    modifier = Modifier.size(28.dp) // Increased icon size
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    tint = AppColors.current.primary,
                    contentDescription = label,
                    modifier = Modifier.size(28.dp) // Increased icon size
                )
            }
        }
    }
}
