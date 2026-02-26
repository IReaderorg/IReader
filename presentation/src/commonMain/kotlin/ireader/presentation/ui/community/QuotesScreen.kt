package ireader.presentation.ui.community

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.quote.LocalQuote
import ireader.domain.models.quote.QuoteCardStyle
import ireader.i18n.Images
import ireader.presentation.ui.quote.MyQuotesViewModel
import ireader.presentation.ui.quote.QuoteStoryEditorScreen

/**
 * Instagram-style quote cards gallery with masonry layout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesScreen(vm: MyQuotesViewModel, onBack: () -> Unit, onCreateQuote: () -> Unit = {}, modifier: Modifier = Modifier) {
    val quotes by vm.quotes.collectAsState()
    val preferredStyle by vm.preferredQuoteStyle.collectAsState()
    
    // Show story editor in full screen
    if (vm.showCreateDialog) {
        QuoteStoryEditorScreen(
            onDismiss = { vm.dismissCreateDialog() },
            onSave = { text, bookTitle, author, style ->
                vm.createQuote(text, bookTitle, author)
            },
            onShare = { text, bookTitle, author, style ->
                vm.showShareConfirmation(text, bookTitle, author, style)
            },
            preferredStyle = preferredStyle,
            onStyleChanged = { vm.savePreferredStyle(it) },
            showShareConfirmDialog = vm.showShareConfirmDialog,
            onDismissShareConfirm = { vm.dismissShareConfirmation() },
            onConfirmShare = { vm.confirmShare() },
            isSharing = vm.isSharing
        )
        return
    }
    
    // Show edit dialog for existing quote
    if (vm.selectedQuote != null) {
        QuoteStoryEditorScreen(
            initialQuote = vm.selectedQuote,
            onDismiss = { vm.clearSelectedQuote() },
            onSave = { text, bookTitle, author, style ->
                vm.updateQuote(vm.selectedQuote!!.id, text, bookTitle, author)
            },
            onShare = { text, bookTitle, author, style ->
                vm.showShareConfirmation(text, bookTitle, author, style)
            },
            preferredStyle = preferredStyle,
            onStyleChanged = { vm.savePreferredStyle(it) },
            showShareConfirmDialog = vm.showShareConfirmDialog,
            onDismissShareConfirm = { vm.dismissShareConfirmation() },
            onConfirmShare = { vm.confirmShare() },
            isSharing = vm.isSharing
        )
        return
    }
    
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { vm.showCreateDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(Icons.Default.Add, "Create", Modifier.size(28.dp))
            }
        }
    ) { pv ->
        Column(Modifier.fillMaxSize().padding(pv)) {
            // Modern header
            InstagramHeader(
                quoteCount = quotes.size,
                onBack = onBack,
                onSearch = { vm.updateSearchQuery(it) },
                searchQuery = vm.searchQuery
            )
            
            when {
                vm.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { 
                    CircularProgressIndicator() 
                }
                quotes.isEmpty() -> InstagramEmptyState(onCreate = { vm.showCreateDialog() })
                else -> InstagramQuoteGrid(
                    quotes = quotes,
                    onQuoteClick = { vm.selectQuote(it) },
                    onDelete = { vm.showDeleteConfirmation(it) },
                    onShare = { vm.showShareConfirmation(it) }
                )
            }
        }
    }
    
    // Dialogs
    if (vm.showDeleteDialog) {
        ModernDeleteDialog(
            onDismiss = { vm.dismissDeleteDialog() },
            onConfirm = { vm.deleteQuote() }
        )
    }
    
    if (vm.showShareDialog) {
        ModernShareDialog(
            validation = vm.shareValidation,
            isSharing = vm.isSharing,
            onDismiss = { vm.dismissShareDialog() },
            onShare = { style, username -> vm.shareQuoteToDiscord(style, username) }
        )
    }
}

@Composable
private fun InstagramHeader(
    quoteCount: Int,
    onBack: () -> Unit,
    onSearch: (String) -> Unit,
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    var showSearch by remember { mutableStateOf(false) }
    
    Column(modifier.fillMaxWidth()) {
        // Top bar
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "My Quotes",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row {
                IconButton(onClick = { showSearch = !showSearch }) {
                    Icon(if (showSearch) Icons.Default.Close else Icons.Default.Search, "Search")
                }
            }
        }
        
        // Search bar
        AnimatedVisibility(
            visible = showSearch,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearch,
                placeholder = { Text("Search quotes...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )
        }
        
        // Stats bar
        if (quoteCount > 0) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        "$quoteCount ${if (quoteCount == 1) "quote" else "quotes"}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun InstagramQuoteGrid(
    quotes: List<LocalQuote>,
    onQuoteClick: (LocalQuote) -> Unit,
    onDelete: (LocalQuote) -> Unit,
    onShare: (LocalQuote) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(160.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp
    ) {
        items(quotes, key = { it.id }) { quote ->
            InstagramQuoteCard(
                quote = quote,
                onClick = { onQuoteClick(quote) },
                onDelete = { onDelete(quote) },
                onShare = { onShare(quote) }
            )
        }
    }
}

@Composable
private fun InstagramQuoteCard(
    quote: LocalQuote,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    
    // Random gradient for visual variety
    val gradients = remember {
        listOf(
            listOf(Color(0xFFFF6B6B), Color(0xFFFFE66D)),
            listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
            listOf(Color(0xFF11998E), Color(0xFF38EF7D)),
            listOf(Color(0xFFDA22FF), Color(0xFF9733EE)),
            listOf(Color(0xFF2C3E50), Color(0xFF4CA1AF)),
            listOf(Color(0xFFF093FB), Color(0xFFF5576C)),
            listOf(Color(0xFFFA709A), Color(0xFFFEE140)),
            listOf(Color(0xFF30CFD0), Color(0xFF330867))
        )
    }
    val gradient = remember(quote.id) { gradients[(quote.id % gradients.size).toInt()] }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(gradient))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // IReader logo at top
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Images.infinity(),
                            contentDescription = "IReader",
                            modifier = Modifier.size(20.dp),
                            tint = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = "IReader",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        Icons.Default.FormatQuote,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White.copy(alpha = 0.3f)
                    )
                }
                
                Spacer(Modifier.height(4.dp))
                
                // Quote text
                Text(
                    text = "\"${quote.text}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontStyle = FontStyle.Italic,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                
                Spacer(Modifier.height(8.dp))
                
                // Book info
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = quote.bookTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!quote.author.isNullOrBlank()) {
                        Text(
                            text = "by ${quote.author}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            // Menu button
            Box(Modifier.align(Alignment.TopEnd)) {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = Color.White
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = { showMenu = false; onClick() },
                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Share") },
                        onClick = { showMenu = false; onShare() },
                        leadingIcon = { Icon(Icons.Default.Share, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = { showMenu = false; onDelete() },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                    )
                }
            }
        }
    }
}

@Composable
private fun InstagramEmptyState(
    onCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Animated icon
            val infiniteTransition = rememberInfiniteTransition(label = "float")
            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -20f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "float"
            )
            
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .offset(y = offsetY.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.FormatQuote,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Text(
                "No quotes yet",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "Create beautiful quote cards\nfrom your favorite books",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            
            Button(
                onClick = onCreate,
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    "Create Your First Quote",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ModernDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        icon = {
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("Delete Quote?", fontWeight = FontWeight.Bold) },
        text = { Text("This quote will be permanently deleted.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Delete", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ModernShareDialog(
    validation: ireader.domain.models.quote.ShareValidation?,
    isSharing: Boolean,
    onDismiss: () -> Unit,
    onShare: (QuoteCardStyle, String) -> Unit
) {
    var selectedStyle by remember { mutableStateOf(QuoteCardStyle.GRADIENT_SUNSET) }
    var username by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        icon = {
            Icon(
                Icons.Default.Share,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("Share to Discord", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                when {
                    validation?.tooShort == true -> {
                        Text("Quote too short (minimum 10 characters).")
                    }
                    else -> {
                        // Style selector
                        Box {
                            OutlinedButton(
                                onClick = { expanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(selectedStyle.displayName)
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                            }
                            DropdownMenu(expanded, { expanded = false }) {
                                QuoteCardStyle.entries.forEach { style ->
                                    DropdownMenuItem(
                                        text = { Text(style.displayName) },
                                        onClick = { selectedStyle = style; expanded = false }
                                    )
                                }
                            }
                        }
                        
                        // Username input
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username (optional)") },
                            placeholder = { Text("Anonymous") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (validation?.canShare == true) {
                Button(
                    onClick = { onShare(selectedStyle, username.ifBlank { "Anonymous" }) },
                    enabled = !isSharing,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSharing) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Share", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
