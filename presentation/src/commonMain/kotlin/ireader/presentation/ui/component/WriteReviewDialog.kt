package ireader.presentation.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.remote.Badge
import ireader.presentation.ui.component.badges.ReviewBadgeDisplay
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

@Composable
fun WriteReviewDialog(
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, reviewText: String) -> Unit,
    initialRating: Int = 0,
    initialReviewText: String = "",
    title: String = "Write Review",
    userName: String? = null,
    primaryBadge: Badge? = null
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var rating by remember { mutableStateOf(initialRating) }
    var reviewText by remember { mutableStateOf(initialReviewText) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Rating", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Star ${index + 1}",
                            tint = if (index < rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { rating = index + 1 }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    label = { Text(localizeHelper.localize(Res.string.review)) },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    maxLines = 5
                )
                
                // Show preview of how the review will appear
                if (userName != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Your review will appear as:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Preview section showing username + badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleSmall
                        )
                        ReviewBadgeDisplay(badge = primaryBadge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (rating > 0 && reviewText.isNotBlank()) {
                        onSubmit(rating, reviewText)
                        onDismiss()
                    }
                },
                enabled = rating > 0 && reviewText.isNotBlank()
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
