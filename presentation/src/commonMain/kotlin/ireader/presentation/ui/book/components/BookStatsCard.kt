package ireader.presentation.ui.book.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.entities.Book

@Composable
fun BookStatsCard(
    book: Book,
    chapterCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Chapters stat
            StatItem(
                icon = Icons.Default.MenuBook,
                value = chapterCount.toString(),
                label = "Chapters",
                iconTint = MaterialTheme.colorScheme.primary
            )
            
            VerticalDivider(
                modifier = Modifier.height(45.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                thickness = 1.dp
            )
            
            // Status stat
            StatItem(
                icon = when {
                    book.favorite -> Icons.Default.Favorite
                    else -> Icons.Default.FavoriteBorder
                },
                value = if (book.favorite) "In Library" else "Not Added",
                label = "Status",
                iconTint = if (book.favorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            VerticalDivider(
                modifier = Modifier.height(45.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                thickness = 1.dp
            )
            
            // Genres stat
            StatItem(
                icon = Icons.Default.Category,
                value = book.genres.size.toString(),
                label = "Genres",
                iconTint = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = iconTint
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            fontSize = 11.sp
        )
    }
}
