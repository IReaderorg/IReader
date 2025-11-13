package ireader.presentation.ui.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.models.remote.ChapterReview
import ireader.domain.models.remote.UserBadge
import ireader.domain.usecases.badge.GetUserBadgesUseCase
import ireader.domain.usecases.review.GetChapterReviewsUseCase
import ireader.domain.usecases.review.SubmitChapterReviewUseCase
import ireader.presentation.ui.component.RatingStars
import ireader.presentation.ui.component.ReviewCard
import ireader.presentation.ui.component.WriteReviewDialog
import ireader.presentation.ui.component.getDisplayName
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Full sheet for chapter reviews - combines viewing and writing in one place
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterReviewsFullSheet(
    bookTitle: String,
    chapterName: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    getChapterReviewsUseCase: GetChapterReviewsUseCase = koinInject(),
    submitChapterReviewUseCase: SubmitChapterReviewUseCase = koinInject(),
    getUserBadgesUseCase: GetUserBadgesUseCase = koinInject()
) {
    var reviews by remember { mutableStateOf<List<ChapterReview>>(emptyList()) }
    var averageRating by remember { mutableStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var userBadgesMap by remember { mutableStateOf<Map<String, List<UserBadge>>>(emptyMap()) }
    var showWriteDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    // Load reviews
    LaunchedEffect(bookTitle, chapterName) {
        isLoading = true
        errorMessage = null
        
        getChapterReviewsUseCase(bookTitle).onSuccess { reviewList ->
            // Filter reviews for this specific chapter
            val chapterReviews = reviewList.filter { it.chapterName == chapterName }
            reviews = chapterReviews
            averageRating = if (chapterReviews.isNotEmpty()) {
                chapterReviews.map { it.rating }.average().toFloat()
            } else 0f
            
            // Fetch badges for all reviewers
            val badgesMap = mutableMapOf<String, List<UserBadge>>()
            chapterReviews.forEach { review ->
                getUserBadgesUseCase(review.userId).onSuccess { badges ->
                    badgesMap[review.userId] = badges
                }
            }
            userBadgesMap = badgesMap
            
            isLoading = false
        }.onFailure { error ->
            errorMessage = error.message ?: "Failed to load chapter reviews"
            isLoading = false
        }
    }
    
    // Refresh function
    fun refreshReviews() {
        scope.launch {
            getChapterReviewsUseCase(bookTitle).onSuccess { reviewList ->
                val chapterReviews = reviewList.filter { it.chapterName == chapterName }
                reviews = chapterReviews
                averageRating = if (chapterReviews.isNotEmpty()) {
                    chapterReviews.map { it.rating }.average().toFloat()
                } else 0f
                
                // Refresh badges
                val badgesMap = mutableMapOf<String, List<UserBadge>>()
                chapterReviews.forEach { review ->
                    getUserBadgesUseCase(review.userId).onSuccess { badges ->
                        badgesMap[review.userId] = badges
                    }
                }
                userBadgesMap = badgesMap
            }
        }
    }
    
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
                    text = "Chapter Reviews",
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
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = { showWriteDialog = true },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Write",color = MaterialTheme.colorScheme.onPrimary)
                }
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        
        Divider()
        
        // Content
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Error loading reviews",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (reviews.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No reviews for this chapter yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Be the first to review!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reviews) { review ->
                    ReviewCard(
                        userName = review.getDisplayName(),
                        rating = review.rating,
                        reviewText = review.reviewText,
                        createdAt = review.createdAt,
                        userBadges = userBadgesMap[review.userId] ?: emptyList()
                    )
                }
            }
        }
    }
    
    // Write review dialog
    if (showWriteDialog) {
        WriteReviewDialog(
            onDismiss = { showWriteDialog = false },
            onSubmit = { rating, reviewText ->
                scope.launch {
                    submitChapterReviewUseCase(bookTitle, chapterName, rating, reviewText).onSuccess {
                        refreshReviews()
                    }.onFailure { error ->
                        errorMessage = error.message ?: "Failed to submit review"
                    }
                }
            },
            title = "Review Chapter"
        )
    }
}
