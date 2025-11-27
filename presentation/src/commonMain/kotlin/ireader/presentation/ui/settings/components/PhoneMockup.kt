package ireader.presentation.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.theme.Theme
import ireader.presentation.core.toComposeColor

@Composable
fun PhoneNotch(
    modifier: Modifier = Modifier,
    color: Color = Color.Black
) {
    Box(
        modifier = modifier
            .height(24.dp)
            .fillMaxWidth()
    ) {
        // Notch shape
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .width(120.dp)
                .height(24.dp)
                .clip(
                    RoundedCornerShape(
                        bottomStart = 12.dp,
                        bottomEnd = 12.dp
                    )
                )
                .background(color)
        )
    }
}

@Composable
fun PhoneNavigationBar(
    modifier: Modifier = Modifier,
    color: Color
) {
    Box(
        modifier = modifier
            .height(32.dp)
            .fillMaxWidth()
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        // Navigation pill
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.3f))
        )
    }
}

@Composable
fun PhoneStatusBar(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    contentColor: Color
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(backgroundColor)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time
        Text(
            text = "9:41",
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontSize = 10.sp
        )

        // Status icons
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.SignalCellularAlt,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = contentColor
            )
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = contentColor
            )
            Icon(
                imageVector = Icons.Default.BatteryFull,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = contentColor
            )
        }
    }
}

@Composable
fun MiniAppPreview(
    theme: Theme,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Mini toolbar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(theme.materialColors.primary.toComposeColor()),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = theme.materialColors.onPrimary.toComposeColor()
                )
                Text(
                    text = "Library",
                    style = MaterialTheme.typography.labelMedium,
                    color = theme.materialColors.onPrimary.toComposeColor(),
                    fontSize = 10.sp
                )
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = theme.materialColors.onPrimary.toComposeColor()
                )
            }
        }

        // Mini book cards
        repeat(3) { index ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(theme.materialColors.surfaceVariant.toComposeColor()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mini cover
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .fillMaxHeight()
                        .background(theme.materialColors.primary.toComposeColor().copy(alpha = 0.3f))
                )

                // Mini text lines
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(theme.materialColors.onSurface.toComposeColor().copy(alpha = 0.8f))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(theme.materialColors.onSurface.toComposeColor().copy(alpha = 0.5f))
                    )
                }
            }
        }

        // Mini FAB
        Box(
            modifier = Modifier
                .align(Alignment.End)
                .size(32.dp)
                .clip(CircleShape)
                .background(theme.materialColors.secondary.toComposeColor()),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = theme.materialColors.onSecondary.toComposeColor()
            )
        }
    }
}
