package ireader.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.remote.UserBadge

@Composable
fun BadgeChip(
    badge: UserBadge,
    modifier: Modifier = Modifier,
    showDescription: Boolean = false
) {
    val backgroundColor = when (badge.badgeRarity) {
        "legendary" -> Color(0xFFFFD700) // Gold
        "epic" -> Color(0xFF9C27B0) // Purple
        "rare" -> Color(0xFF2196F3) // Blue
        else -> Color(0xFF4CAF50) // Green
    }
    
    val textColor = when (badge.badgeRarity) {
        "legendary" -> Color(0xFFB8860B) // Dark gold for better contrast
        "epic" -> Color(0xFF7B1FA2) // Dark purple
        "rare" -> Color(0xFF1976D2) // Dark blue
        else -> Color(0xFF388E3C) // Dark green
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, backgroundColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = badge.badgeIcon,
                fontSize = 16.sp
            )
            
            Column {
                Text(
                    text = badge.badgeName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                
                if (showDescription) {
                    Text(
                        text = badge.badgeDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun BadgeRow(
    badges: List<UserBadge>,
    modifier: Modifier = Modifier,
    maxVisible: Int = 3
) {
    if (badges.isEmpty()) return
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        badges.take(maxVisible).forEach { badge ->
            BadgeChip(badge = badge)
        }
        
        if (badges.size > maxVisible) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "+${badges.size - maxVisible}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
