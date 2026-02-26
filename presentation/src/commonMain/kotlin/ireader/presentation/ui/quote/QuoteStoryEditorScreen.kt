package ireader.presentation.ui.quote

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ireader.domain.models.quote.QuoteCardStyle

/**
 * Instagram story-style quote editor screen.
 * Users edit quote text with live preview and can save locally or share to Discord.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteStoryEditorScreen(
    selectedStyle: QuoteCardStyle,
    initialQuoteText: String = "",
    initialBookTitle: String = "",
    initialAuthor: String = "",
    onSaveLocally: (String, String, String) -> Unit,
    onShareToDiscord: (String, String, String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var quoteText by remember { mutableStateOf(initialQuoteText) }
    var bookTitle by remember { mutableStateOf(initialBookTitle) }
    var author by remember { mutableStateOf(initialAuthor) }
    var showInputs by remember { mutableStateOf(false) }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Live preview background
        QuoteLivePreview(
            quoteText = quoteText,
            bookTitle = bookTitle,
            author = author,
            style = selectedStyle,
            modifier = Modifier.fillMaxSize()
        )
        
        // Semi-transparent overlay for inputs
        if (showInputs) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
            )
        }
        
        // Top bar
        TopAppBar(
            title = { Text(selectedStyle.displayName) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        "Back",
                        tint = getStyleTextColor(selectedStyle)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = getStyleTextColor(selectedStyle),
                navigationIconContentColor = getStyleTextColor(selectedStyle)
            ),
            modifier = Modifier.align(Alignment.TopCenter)
        )
        
        // Input fields (shown when tapped)
        if (showInputs) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = quoteText,
                    onValueChange = { quoteText = it },
                    label = { Text("Quote Text") },
                    placeholder = { Text("Enter your quote...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 8,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                
                OutlinedTextField(
                    value = bookTitle,
                    onValueChange = { bookTitle = it },
                    label = { Text("Book Title") },
                    placeholder = { Text("Enter book title...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author") },
                    placeholder = { Text("Enter author name...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                
                Text(
                    text = "${quoteText.length} characters",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                Button(
                    onClick = { showInputs = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }
        }
        
        // Bottom action buttons
        if (!showInputs) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tap to edit hint
                TextButton(
                    onClick = { showInputs = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Tap to edit",
                        color = getStyleTextColor(selectedStyle).copy(alpha = 0.7f)
                    )
                }
                
                // Save locally button
                Button(
                    onClick = { onSaveLocally(quoteText, bookTitle, author) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = quoteText.isNotBlank()
                ) {
                    Text("Save Locally")
                }
                
                // Share to Discord button
                FilledTonalButton(
                    onClick = { onShareToDiscord(quoteText, bookTitle, author) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = quoteText.isNotBlank() && bookTitle.isNotBlank()
                ) {
                    Text("Share to Discord")
                }
            }
        }
    }
}

private fun getStyleTextColor(style: QuoteCardStyle): Color {
    return when (style) {
        QuoteCardStyle.MINIMAL_LIGHT,
        QuoteCardStyle.PAPER_TEXTURE -> Color.Black
        else -> Color.White
    }
}
