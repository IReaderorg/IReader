package ireader.presentation.ui.reader.components

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
import ireader.domain.models.remote.ChapterReview
import ireader.domain.models.remote.UserBadge
import ireader.presentation.ui.component.RatingStars
import ireader.presentation.ui.component.ReviewCard
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Bottom sheet modal for displaying all chapter reviews
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterReviewsBottomSheet(
    chapterName: String,
    reviews: List<ChapterReview>,
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
                    text = localizeHelper.localize(Res.string.chapter_reviews),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = chapterName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    text = localizeHelper.localize(Res.string.no_reviews_for_this_chapter_yet),
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
                    ReviewCard(
                        userName = "User ${review.userId.take(8)}",
                        rating = review.rating,
                        reviewText = review.reviewText,
                        createdAt = review.createdAt,
                        userBadges = userBadgesMap[review.userId] ?: emptyList()
                    )
                }
            }
        }
    }
}
