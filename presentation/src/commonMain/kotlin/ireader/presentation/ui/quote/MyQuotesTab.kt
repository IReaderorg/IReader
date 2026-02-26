package ireader.presentation.ui.quote

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.models.quote.LocalQuote

/**
 * My Quotes tab content for Reading Buddy screen
 */
@Composable
fun MyQuotesTab(
    vm: MyQuotesViewModel,
    modifier: Modifier = Modifier
) {
    val quotes by vm.quotes.collectAsState()
    
    Column(modifier = modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = vm.searchQuery,
            onValueChange = { vm.updateSearchQuery(it) },
            placeholder = { Text("Search quotes...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true
        )
        
        when {
            vm.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            quotes.isEmpty() -> {
                EmptyQuotesState(
                    searchQuery = vm.searchQuery,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                ) {
                    items(
                        items = quotes,
                        key = { it.id }
                    ) { quote ->
                        LocalQuoteItem(
                            quote = quote,
                            onClick = { vm.selectQuote(quote) },
                            onDelete = { vm.showDeleteConfirmation(quote) },
                            onShare = { vm.showShareConfirmation(quote) }
                        )
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (vm.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { vm.dismissDeleteDialog() },
            title = { Text("Delete Quote?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { vm.deleteQuote() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.dismissDeleteDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Share dialog
    if (vm.showShareDialog) {
        val validation = vm.shareValidation
        AlertDialog(
            onDismissRequest = { vm.dismissShareDialog() },
            title = { Text("Share to Community") },
            text = {
                Column {
                    when {
                        validation?.tooShort == true -> {
                            Text("This quote is too short to share (minimum 10 characters).")
                        }
                        validation?.needsTruncation == true -> {
                            Text("This quote has ${validation.currentLength} characters. Community quotes are limited to ${validation.maxLength} characters.")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Would you like to truncate it?")
                        }
                        else -> {
                            Text("Share this quote with the community?")
                        }
                    }
                }
            },
            confirmButton = {
                if (validation?.canShare == true || validation?.needsTruncation == true) {
                    TextButton(
                        onClick = { 
                            // Share to Discord with default style and username
                            vm.shareQuoteToDiscord(
                                style = ireader.domain.models.quote.QuoteCardStyle.GRADIENT_SUNSET,
                                username = "Anonymous"
                            )
                        },
                        enabled = !vm.isSharing
                    ) {
                        if (vm.isSharing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        } else {
                            Text("Share")
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.dismissShareDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun LocalQuoteItem(
    quote: LocalQuote,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Quote text preview
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.FormatQuote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = quote.text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Book info
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = quote.bookTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Chapter info
            Text(
                text = quote.chapterTitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // Context backup indicator
                if (quote.hasContextBackup) {
                    Text(
                        text = "📚 Context saved",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                
                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyQuotesState(
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FormatQuote,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = if (searchQuery.isBlank()) "No quotes yet" else "No quotes found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (searchQuery.isBlank()) 
                    "Use Copy Mode in the reader to save quotes" 
                else 
                    "Try a different search term",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
