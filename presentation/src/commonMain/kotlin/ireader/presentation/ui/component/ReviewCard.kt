package ireader.presentation.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.remote.Badge
import ireader.domain.models.remote.UserBadge
import ireader.presentation.ui.component.badges.ReviewBadgeDisplay
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun ReviewCard(
    userName: String,
    rating: Int,
    reviewText: String,
    createdAt: Long,
    modifier: Modifier = Modifier,
    userBadges: List<UserBadge> = emptyList(),
    reviewerBadge: Badge? = null
) {
    val formattedDate = remember(createdAt) {
        try {
            val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(createdAt)
            val localDateTime = instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
            "${localDateTime.date} ${localDateTime.hour}:${localDateTime.minute.toString().padStart(2, '0')}"
        } catch (e: Exception) {
            "Recently"
        }
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Display username with badge next to it
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // Display reviewer's primary badge next to username
                        ReviewBadgeDisplay(badge = reviewerBadge)
                    }
                    if (userBadges.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        BadgeRow(badges = userBadges, maxVisible = 2)
                    }
                }
                RatingStars(rating = rating, size = 16.dp)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = reviewText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
