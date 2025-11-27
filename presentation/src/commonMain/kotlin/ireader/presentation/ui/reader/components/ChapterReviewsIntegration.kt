package ireader.presentation.ui.reader.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.remote.ChapterReview
import ireader.domain.models.remote.UserBadge
import ireader.domain.usecases.badge.GetUserBadgesUseCase
import ireader.domain.usecases.review.GetChapterReviewsUseCase
import ireader.domain.usecases.review.SubmitChapterReviewUseCase
import ireader.presentation.ui.component.RatingStars
import ireader.presentation.ui.component.WriteReviewDialog
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Integration component for chapter reviews with bottom sheet
 * Shows compact button in reader, full reviews in bottom sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterReviewsIntegration(
    bookTitle: String,
    chapterName: String,
    modifier: Modifier = Modifier,
    getChapterReviewsUseCase: GetChapterReviewsUseCase = koinInject(),
    submitChapterReviewUseCase: SubmitChapterReviewUseCase = koinInject(),
    getUserBadgesUseCase: GetUserBadgesUseCase = koinInject()
) {
    var reviews by remember { mutableStateOf<List<ChapterReview>>(emptyList()) }
    var averageRating by remember { mutableStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var userBadgesMap by remember { mutableStateOf<Map<String, List<UserBadge>>>(emptyMap()) }
    var showReviewsSheet by remember { mutableStateOf(false) }
    var showWriteDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    
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
            
            // Fetch badges for all reviewers (only if reviews exist)
            if (chapterReviews.isNotEmpty()) {
                val badgesMap = mutableMapOf<String, List<UserBadge>>()
                chapterReviews.forEach { review ->
                    getUserBadgesUseCase(review.userId).onSuccess { badges ->
                        badgesMap[review.userId] = badges
                    }
                }
                userBadgesMap = badgesMap
            }
            
            isLoading = false
        }.onFailure { error ->
            // Check if this is a Supabase unavailable error
            val message = error.message ?: "Failed to load chapter reviews"
            if (message.contains("Supabase", ignoreCase = true) || 
                message.contains("not configured", ignoreCase = true)) {
                // Supabase is not configured - don't show as error, just hide reviews
                errorMessage = null
                reviews = emptyList()
            } else {
                errorMessage = message
            }
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
                
                // Refresh badges (only if reviews exist)
                if (chapterReviews.isNotEmpty()) {
                    val badgesMap = mutableMapOf<String, List<UserBadge>>()
                    chapterReviews.forEach { review ->
                        getUserBadgesUseCase(review.userId).onSuccess { badges ->
                            badgesMap[review.userId] = badges
                        }
                    }
                    userBadgesMap = badgesMap
                }
            }.onFailure { error ->
                // Handle Supabase unavailable gracefully
                val message = error.message ?: "Failed to load chapter reviews"
                if (message.contains("Supabase", ignoreCase = true) || 
                    message.contains("not configured", ignoreCase = true)) {
                    reviews = emptyList()
                } else {
                    errorMessage = message
                }
            }
        }
    }
    
    if (!isLoading) {
        // Compact card with review count and buttons
        Card(
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RateReview,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    if (reviews.isNotEmpty()) {
                        RatingStars(rating = averageRating.toInt(), size = 16.dp)
                        Text(
                            text = "${reviews.size}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = "No reviews",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (reviews.isNotEmpty()) {
                        TextButton(onClick = { showReviewsSheet = true }) {
                            Text("View")
                        }
                    }
                    
                    FilledTonalButton(
                        onClick = { showWriteDialog = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Review", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
        
        // Bottom sheet with all reviews
        if (showReviewsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showReviewsSheet = false },
                sheetState = sheetState
            ) {
                ChapterReviewsBottomSheet(
                    chapterName = chapterName,
                    reviews = reviews,
                    averageRating = averageRating,
                    userBadgesMap = userBadgesMap,
                    onDismiss = {
                        scope.launch {
                            sheetState.hide()
                            showReviewsSheet = false
                        }
                    }
                )
            }
        }
        
        // Write review dialog
        if (showWriteDialog) {
            WriteReviewDialog(
                onDismiss = { showWriteDialog = false },
                onSubmit = { rating, reviewText ->
                    scope.launch {
                        submitChapterReviewUseCase(bookTitle, chapterName, rating, reviewText).onSuccess {
                            showWriteDialog = false
                            refreshReviews()
                        }.onFailure { error ->
                            val message = error.message ?: "Failed to submit review"
                            // Show user-friendly message for Supabase unavailable
                            errorMessage = if (message.contains("Supabase", ignoreCase = true) || 
                                message.contains("not configured", ignoreCase = true)) {
                                "Reviews are not available. Please configure Supabase in Settings."
                            } else {
                                message
                            }
                            showWriteDialog = false
                        }
                    }
                },
                title = "Review Chapter"
            )
        }
    }
}
