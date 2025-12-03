package ireader.presentation.ui.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.remote.ChapterReview
import ireader.domain.models.remote.UserBadge
import ireader.presentation.ui.component.RatingStars
import ireader.presentation.ui.component.ReviewCard
import ireader.presentation.ui.component.WriteReviewDialog
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.core.utils.formatRatingShort
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

@Composable
fun ChapterReviewSection(
    reviews: List<ChapterReview>,
    averageRating: Float,
    onWriteReview: (rating: Int, reviewText: String) -> Unit,
    modifier: Modifier = Modifier,
    userBadgesMap: Map<String, List<UserBadge>> = emptyMap()
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var showWriteDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.RateReview,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = localizeHelper.localize(Res.string.chapter_reviews),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                FilledTonalButton(
                    onClick = { showWriteDialog = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("Review", style = MaterialTheme.typography.labelMedium)
                }
            }
            
            if (reviews.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingStars(rating = averageRating.toInt(), size = 16.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatRatingShort(averageRating, reviews.size),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Hide Reviews" else "Show Reviews")
                }
                
                if (expanded) {
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
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = localizeHelper.localize(Res.string.no_reviews_for_this_chapter_yet_1),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    if (showWriteDialog) {
        WriteReviewDialog(
            onDismiss = { showWriteDialog = false },
            onSubmit = onWriteReview,
            title = localizeHelper.localize(Res.string.review_chapter)
        )
    }
}
