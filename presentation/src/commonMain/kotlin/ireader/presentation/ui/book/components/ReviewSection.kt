package ireader.presentation.ui.book.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.remote.BookReview
import ireader.domain.models.remote.UserBadge
import ireader.presentation.ui.component.RatingStars
import ireader.presentation.ui.component.ReviewCard
import ireader.presentation.ui.component.WriteReviewDialog
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

@Composable
fun ReviewSection(
    reviews: List<BookReview>,
    averageRating: Float,
    onWriteReview: (rating: Int, reviewText: String) -> Unit,
    modifier: Modifier = Modifier,
    userBadgesMap: Map<String, List<UserBadge>> = emptyMap()
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var showWriteDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = localizeHelper.localize(Res.string.reviews),
                    style = MaterialTheme.typography.titleLarge
                )
                if (reviews.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RatingStars(rating = averageRating.toInt())
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = String.format("%.1f (%d reviews)", averageRating, reviews.size),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            FilledTonalButton(
                onClick = { showWriteDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint =  MaterialTheme.colorScheme.background)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Write Review", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (reviews.isEmpty()) {
            Text(
                text = localizeHelper.localize(Res.string.no_reviews_yet_be_the_first_to_review),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            reviews.forEach { review ->
                ReviewCard(
                    userName = "User ${review.userId.take(8)}",
                    rating = review.rating,
                    reviewText = review.reviewText,
                    createdAt = review.createdAt,
                    userBadges = userBadgesMap[review.userId] ?: emptyList(),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
    
    if (showWriteDialog) {
        WriteReviewDialog(
            onDismiss = { showWriteDialog = false },
            onSubmit = onWriteReview
        )
    }
}
