package ireader.presentation.ui.quote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.component.IScaffold

/**
 * Screen for creating a new quote from copied text
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteCreationScreen(
    vm: QuoteCreationViewModel,
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    IScaffold(
        topBar = {
            TopAppBar(
                title = { Text("Save Quote") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Book info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Book title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = vm.params.bookTitle,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Chapter title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = vm.params.chapterTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Author (if available)
                    vm.params.author?.let { author ->
                        if (author.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                                Text(
                                    text = author,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // Quote text input
            OutlinedTextField(
                value = vm.quoteText,
                onValueChange = { vm.updateQuoteText(it) },
                label = { Text("Paste your quote here") },
                placeholder = { Text("Select and copy text from the chapter, then paste it here...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                minLines = 5,
                maxLines = 10
            )
            
            // Character count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${vm.characterCount} characters",
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        vm.characterCount > vm.maxCommunityLength -> MaterialTheme.colorScheme.error
                        vm.characterCount < vm.minCommunityLength && vm.characterCount > 0 -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                
                vm.shareValidation?.let { validation ->
                    if (!validation.canShare && vm.characterCount > 0) {
                        Text(
                            text = validation.reason ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // Context backup toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Include Context Backup",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Save surrounding chapters for context",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = vm.includeContextBackup,
                        onCheckedChange = { vm.toggleContextBackup() }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Save locally button
                Button(
                    onClick = { vm.saveQuoteLocally(onSaveSuccess) },
                    enabled = !vm.isSaving && !vm.isSharing && vm.quoteText.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    if (vm.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp).width(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save")
                }
                
                // Share to community button
                OutlinedButton(
                    onClick = { vm.shareQuoteToCommunity(onSaveSuccess) },
                    enabled = !vm.isSaving && !vm.isSharing && vm.quoteText.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    if (vm.isSharing) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp).width(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share")
                }
            }
            
            // Info text
            Text(
                text = "💡 Save locally for unlimited length quotes. Share to community for quotes between 10-1000 characters.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    // Truncation dialog
    if (vm.showTruncationDialog) {
        AlertDialog(
            onDismissRequest = { vm.dismissTruncationDialog() },
            title = { Text("Quote Too Long") },
            text = {
                Column {
                    Text("Your quote has ${vm.characterCount} characters, but community quotes are limited to ${vm.maxCommunityLength} characters.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Would you like to truncate it or keep it local only?")
                }
            },
            confirmButton = {
                TextButton(onClick = { vm.submitToCommunity(truncate = true, onSuccess = onSaveSuccess) }) {
                    Text("Truncate & Share")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { vm.dismissTruncationDialog() }) {
                        Text("Cancel")
                    }
                    TextButton(onClick = { 
                        vm.dismissTruncationDialog()
                        vm.saveQuoteLocally(onSaveSuccess)
                    }) {
                        Text("Save Locally")
                    }
                }
            }
        )
    }
}
