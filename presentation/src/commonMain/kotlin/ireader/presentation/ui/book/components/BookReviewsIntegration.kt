package ireader.presentation.ui.book.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ireader.domain.models.remote.BookReview
import ireader.domain.models.remote.UserBadge
import ireader.domain.usecases.badge.GetUserBadgesUseCase
import ireader.domain.usecases.review.GetBookReviewsUseCase
import ireader.domain.usecases.review.SubmitBookReviewUseCase
import ireader.presentation.ui.component.WriteReviewDialog
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Integration component for book reviews with bottom sheet
 * Shows compact summary in main screen, full reviews in bottom sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookReviewsIntegration(
    bookTitle: String,
    modifier: Modifier = Modifier,
    getBookReviewsUseCase: GetBookReviewsUseCase = koinInject(),
    submitBookReviewUseCase: SubmitBookReviewUseCase = koinInject(),
    getUserBadgesUseCase: GetUserBadgesUseCase = koinInject()
) {
    var reviews by remember { mutableStateOf<List<BookReview>>(emptyList()) }
    var averageRating by remember { mutableStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var userBadgesMap by remember { mutableStateOf<Map<String, List<UserBadge>>>(emptyMap()) }
    var showReviewsSheet by remember { mutableStateOf(false) }
    var showWriteDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    
    // Load reviews
    LaunchedEffect(bookTitle) {
        isLoading = true
        errorMessage = null
        
        getBookReviewsUseCase(bookTitle).onSuccess { reviewList ->
            reviews = reviewList
            averageRating = if (reviewList.isNotEmpty()) {
                reviewList.map { it.rating }.average().toFloat()
            } else 0f
            
            // Fetch badges for all reviewers (only if reviews exist)
            if (reviewList.isNotEmpty()) {
                val badgesMap = mutableMapOf<String, List<UserBadge>>()
                reviewList.forEach { review ->
                    getUserBadgesUseCase(review.userId).onSuccess { badges ->
                        badgesMap[review.userId] = badges
                    }
                }
                userBadgesMap = badgesMap
            }
            
            isLoading = false
        }.onFailure { error ->
            // Check if this is a non-critical error (maintenance, network, etc.)
            val message = error.message ?: "Failed to load reviews"
            val isNonCritical = message.contains("maintenance", ignoreCase = true) ||
                               message.contains("unavailable", ignoreCase = true) ||
                               message.contains("cancelled", ignoreCase = true) ||
                               message.contains("Supabase", ignoreCase = true) ||
                               message.contains("not configured", ignoreCase = true) ||
                               message.contains("timeout", ignoreCase = true)
            
            if (isNonCritical) {
                // Non-critical error - silently hide reviews, app continues normally
                errorMessage = null
                reviews = emptyList()
            } else {
                // Critical error - show to user
                errorMessage = message
            }
            isLoading = false
        }
    }
    
    // Refresh function
    fun refreshReviews() {
        scope.launch {
            getBookReviewsUseCase(bookTitle).onSuccess { reviewList ->
                reviews = reviewList
                averageRating = if (reviewList.isNotEmpty()) {
                    reviewList.map { it.rating }.average().toFloat()
                } else 0f
                
                // Refresh badges (only if reviews exist)
                if (reviewList.isNotEmpty()) {
                    val badgesMap = mutableMapOf<String, List<UserBadge>>()
                    reviewList.forEach { review ->
                        getUserBadgesUseCase(review.userId).onSuccess { badges ->
                            badgesMap[review.userId] = badges
                        }
                    }
                    userBadgesMap = badgesMap
                }
            }.onFailure { error ->
                // Handle non-critical errors gracefully (maintenance, network, etc.)
                val message = error.message ?: "Failed to load reviews"
                val isNonCritical = message.contains("maintenance", ignoreCase = true) ||
                                   message.contains("unavailable", ignoreCase = true) ||
                                   message.contains("cancelled", ignoreCase = true) ||
                                   message.contains("Supabase", ignoreCase = true) ||
                                   message.contains("not configured", ignoreCase = true) ||
                                   message.contains("timeout", ignoreCase = true)
                
                if (isNonCritical) {
                    reviews = emptyList()
                } else {
                    errorMessage = message
                }
            }
        }
    }
    
    if (!isLoading) {
        // Compact summary card
        ReviewSummaryCard(
            reviewCount = reviews.size,
            averageRating = averageRating,
            onOpenReviews = { showReviewsSheet = true },
            onWriteReview = { showWriteDialog = true },
            modifier = modifier
        )
        
        // Bottom sheet with all reviews
        if (showReviewsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showReviewsSheet = false },
                sheetState = sheetState
            ) {
                ReviewsBottomSheet(
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
                        submitBookReviewUseCase(bookTitle, rating, reviewText).onSuccess {
                            showWriteDialog = false
                            refreshReviews()
                        }.onFailure { error ->
                            val message = error.message ?: "Failed to submit review"
                            // Show user-friendly message for non-critical errors
                            val isNonCritical = message.contains("maintenance", ignoreCase = true) ||
                                               message.contains("unavailable", ignoreCase = true) ||
                                               message.contains("cancelled", ignoreCase = true) ||
                                               message.contains("Supabase", ignoreCase = true) ||
                                               message.contains("not configured", ignoreCase = true) ||
                                               message.contains("timeout", ignoreCase = true)
                            
                            errorMessage = if (isNonCritical) {
                                "Reviews are temporarily unavailable. Please try again later."
                            } else {
                                message
                            }
                            showWriteDialog = false
                        }
                    }
                }
            )
        }
    }
}
