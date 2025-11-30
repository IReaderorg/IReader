package ireader.presentation.ui.plugins.details.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

/**
 * Dialog for writing a review with rating picker and text input
 * Requirements: 13.1, 13.2, 13.3
 */
@Composable
fun WriteReviewDialog(
    onSubmit: (rating: Float, reviewText: String) -> Unit,
    onDismiss: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var rating by remember { mutableStateOf(0f) }
    var reviewText by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Write a Review",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Rating picker
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Your Rating",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    RatingPicker(
                        rating = rating,
                        onRatingChange = { rating = it }
                    )
                }
                
                // Review text input
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    label = { Text(localizeHelper.localize(Res.string.your_review_optional)) },
                    placeholder = { Text(localizeHelper.localize(Res.string.share_your_experience_with_this_plugin)) },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(rating, reviewText) },
                enabled = rating > 0
            ) {
                Text(localizeHelper.localize(Res.string.submit))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        }
    )
}

/**
 * Interactive rating picker with stars
 */
@Composable
private fun RatingPicker(
    rating: Float,
    onRatingChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(5) { index ->
            val starRating = (index + 1).toFloat()
            Icon(
                imageVector = if (starRating <= rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = "Rate $starRating stars",
                tint = if (starRating <= rating) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onRatingChange(starRating) }
            )
        }
    }
}
