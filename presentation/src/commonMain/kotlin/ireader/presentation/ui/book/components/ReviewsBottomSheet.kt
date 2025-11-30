package ireader.presentation.ui.book.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.models.remote.BookReview
import ireader.domain.models.remote.UserBadge
import ireader.presentation.ui.component.RatingStars
import ireader.presentation.ui.component.ReviewCard
import ireader.presentation.ui.component.getDisplayName
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

/**
 * Bottom sheet modal for displaying all book reviews
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsBottomSheet(
    reviews: List<BookReview>,
    averageRating: Float,
    userBadgesMap: Map<String, List<UserBadge>>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Reviews",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                if (reviews.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RatingStars(rating = averageRating.toInt(), size = 18.dp)
                        Text(
                            text = String.format("%.1f (%d reviews)", averageRating, reviews.size),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = localizeHelper.localize(Res.string.close)
                )
            }
        }
        
        Divider()
        
        // Reviews list
        if (reviews.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No reviews yet. Be the first to review!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reviews) { review ->
                    val userBadges = userBadgesMap[review.userId] ?: emptyList()
                    val primaryBadge = userBadges.find { it.isPrimary }?.let { userBadge ->
                        ireader.domain.models.remote.Badge(
                            id = userBadge.badgeId,
                            name = userBadge.badgeName,
                            description = userBadge.badgeDescription,
                            icon = userBadge.badgeIcon,
                            category = userBadge.badgeCategory,
                            rarity = userBadge.badgeRarity,
                            imageUrl = userBadge.imageUrl ?: userBadge.badgeIcon
                        )
                    }
                    
                    ReviewCard(
                        userName = review.getDisplayName(),
                        rating = review.rating,
                        reviewText = review.reviewText,
                        createdAt = review.createdAt,
                        userBadges = userBadges,
                        reviewerBadge = primaryBadge
                    )
                }
            }
        }
    }
}
