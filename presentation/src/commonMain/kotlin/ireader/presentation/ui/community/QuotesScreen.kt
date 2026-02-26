package ireader.presentation.ui.community

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.quote.LocalQuote
import ireader.domain.models.quote.ShareValidation
import ireader.presentation.ui.quote.MyQuotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesScreen(vm: MyQuotesViewModel, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val quotes by vm.quotes.collectAsState()
    val clip = LocalClipboardManager.current
    Scaffold(modifier = modifier) { pv ->
        LazyColumn(Modifier.fillMaxSize().padding(pv), contentPadding = PaddingValues(bottom = 24.dp)) {
            item { QHeader(quotes.size, onBack) }
            item { QStats(quotes) }
            item { QSearch(vm.searchQuery, { vm.updateSearchQuery(it) }, Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) }
            if (quotes.isNotEmpty()) { item { QFilters(vm.selectedFilter, { vm.setFilter(it) }, Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) } }
            when {
                vm.isLoading -> item { Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                quotes.isEmpty() -> item { QEmpty(vm.searchQuery, Modifier.fillMaxWidth().padding(32.dp)) }
                else -> items(quotes, key = { it.id }) { q ->
                    QCard(q, { clip.setText(AnnotatedString(q.text)); vm.showSnackBar(ireader.i18n.UiText.DynamicString("Copied")) },
                        { vm.showDeleteConfirmation(q) }, { vm.showShareConfirmation(q) }, Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                }
            }
        }
    }
    QDeleteDlg(vm.showDeleteDialog, { vm.dismissDeleteDialog() }, { vm.deleteQuote() })
    QShareDlg(vm.showShareDialog, vm.shareValidation, vm.isSharing, { vm.dismissShareDialog() }, { style, username -> vm.shareQuoteToDiscord(style, username) })
}

@Composable
private fun QHeader(count: Int, onBack: () -> Unit) {
    Box(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.background))).padding(top = 8.dp, bottom = 24.dp)) {
        Column {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            }
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(80.dp).clip(CircleShape).background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary))), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.FormatQuote, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(Modifier.height(16.dp))
                Text("My Quotes", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(if (count == 0) "Start collecting memorable passages" else "$count saved quote${if (count != 1) "s" else ""}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}


@Composable
private fun QStats(quotes: List<LocalQuote>) {
    if (quotes.isEmpty()) return
    val books = remember(quotes) { quotes.map { it.bookTitle }.distinct().size }
    val ctx = remember(quotes) { quotes.count { it.hasContextBackup } }
    LazyRow(Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        item { QStatChip("📚", "$books", "Books") }
        item { QStatChip("💬", "${quotes.size}", "Quotes") }
        item { QStatChip("📖", "$ctx", "With Context") }
    }
}

@Composable
private fun QStatChip(emoji: String, value: String, label: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(emoji, fontSize = 20.sp)
            Column { Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun QSearch(query: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
        TextField(query, onChange, placeholder = { Text("Search quotes...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            trailingIcon = { AnimatedVisibility(query.isNotEmpty(), enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) { IconButton({ onChange("") }) { Icon(Icons.Default.Close, "Clear") } } },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent))
    }
}

@Composable
private fun QFilters(selected: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    LazyRow(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(listOf("All", "With Context", "Recent")) { f ->
            FilterChip(selected == f, { onChange(f) }, { Text(f) }, shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer))
        }
    }
}


@Composable
private fun QCard(quote: LocalQuote, onCopy: () -> Unit, onDelete: () -> Unit, onShare: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(20.dp)) {
            Text("", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), fontWeight = FontWeight.Bold, modifier = Modifier.offset(y = (-8).dp))
            Text(quote.text, style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic, lineHeight = 26.sp, modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 16.dp))
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.secondaryContainer))), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.MenuBook, null, Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(quote.bookTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(quote.chapterTitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            if (quote.hasContextBackup) {
                Spacer(Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Outlined.BookmarkBorder, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onTertiaryContainer)
                        Text("Context saved", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                QBtn(Icons.Default.ContentCopy, "Copy", onCopy, MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(4.dp))
                QBtn(Icons.Default.Share, "Share", onShare, MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(4.dp))
                QBtn(Icons.Default.Delete, "Delete", onDelete, MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun QBtn(icon: ImageVector, label: String, onClick: () -> Unit, tint: Color) {
    Surface(shape = RoundedCornerShape(10.dp), color = tint.copy(alpha = 0.1f), onClick = onClick) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, label, Modifier.size(18.dp), tint = tint)
            Text(label, style = MaterialTheme.typography.labelMedium, color = tint, fontWeight = FontWeight.Medium)
        }
    }
}


@Composable
private fun QEmpty(searchQuery: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(Modifier.size(140.dp).clip(CircleShape).background(Brush.radialGradient(listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.background))), contentAlignment = Alignment.Center) {
            Text(if (searchQuery.isBlank()) "📚" else "🔍", fontSize = 48.sp)
        }
        Spacer(Modifier.height(24.dp))
        Text(if (searchQuery.isBlank()) "No quotes yet" else "No quotes found", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(if (searchQuery.isBlank()) "Save memorable passages from your reading.\nUse Copy Quote in the reader settings." else "Try a different search term",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, lineHeight = 22.sp)
        if (searchQuery.isBlank()) {
            Spacer(Modifier.height(24.dp))
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("💡", fontSize = 24.sp)
                    Column { Text("Quick Tip", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold); Text("Open any book → Settings → Copy Quote", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
    }
}

@Composable
private fun QDeleteDlg(show: Boolean, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    if (!show) return
    AlertDialog(onDismissRequest = onDismiss, shape = RoundedCornerShape(24.dp),
        title = { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error); Text("Delete Quote?") } },
        text = { Text("This quote will be permanently deleted.", style = MaterialTheme.typography.bodyMedium) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable
private fun QShareDlg(show: Boolean, v: ShareValidation?, loading: Boolean, onDismiss: () -> Unit, onShare: (ireader.domain.models.quote.QuoteCardStyle, String) -> Unit) {
    if (!show) return
    var selectedStyle by remember { mutableStateOf(ireader.domain.models.quote.QuoteCardStyle.GRADIENT_SUNSET) }
    var username by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    AlertDialog(onDismissRequest = onDismiss, shape = RoundedCornerShape(24.dp),
        title = { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) { Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.primary); Text("Share to Discord") } },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when {
                    v?.tooShort == true -> Text("Quote too short (min 10 chars).", style = MaterialTheme.typography.bodyMedium)
                    else -> {
                        Text("Choose a visual style for your quote card:", style = MaterialTheme.typography.bodyMedium)
                        
                        // Style selector dropdown
                        Box {
                            Surface(
                                onClick = { expanded = true },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(selectedStyle.displayName, style = MaterialTheme.typography.bodyMedium)
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                            }
                            DropdownMenu(expanded, { expanded = false }) {
                                ireader.domain.models.quote.QuoteCardStyle.entries.forEach { style ->
                                    DropdownMenuItem(
                                        text = { Text(style.displayName) },
                                        onClick = { selectedStyle = style; expanded = false }
                                    )
                                }
                            }
                        }
                        
                        // Username input
                        TextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Your username (optional)") },
                            placeholder = { Text("Anonymous") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        },
        confirmButton = { 
            if (v?.canShare == true) { 
                TextButton({ onShare(selectedStyle, username.ifBlank { "Anonymous" }) }, enabled = !loading) { 
                    if (loading) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp) 
                    else Text("Share") 
                } 
            } 
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}
