package ireader.presentation.ui.quote

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.quote.QuoteCardStyle
import ireader.domain.models.quote.QuoteCardStyleColors
import ireader.i18n.Images
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Instagram Story-style quote editor with live preview
 * Swipe through styles, tap to edit text, beautiful animations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteStoryEditorScreen(
    onDismiss: () -> Unit,
    onSave: (text: String, bookTitle: String, author: String, style: QuoteCardStyle) -> Unit,
    onShare: ((text: String, bookTitle: String, author: String, style: QuoteCardStyle) -> Unit)? = null,
    initialQuote: ireader.domain.models.quote.LocalQuote? = null,
    preferredStyle: QuoteCardStyle = QuoteCardStyle.GRADIENT_SUNSET,
    onStyleChanged: (QuoteCardStyle) -> Unit = {},
    showShareConfirmDialog: Boolean = false,
    onDismissShareConfirm: () -> Unit = {},
    onConfirmShare: () -> Unit = {},
    isSharing: Boolean = false,
    modifier: Modifier = Modifier
) {
    var quoteText by remember { mutableStateOf(initialQuote?.text ?: "") }
    var bookTitle by remember { mutableStateOf(initialQuote?.bookTitle ?: "") }
    var author by remember { mutableStateOf(initialQuote?.author ?: "") }
    var showEditDialog by remember { mutableStateOf(false) }
    var editMode by remember { mutableStateOf<EditMode>(EditMode.Quote) }
    
    val styles = remember { QuoteCardStyle.entries.toList() }
    val initialPage = remember { styles.indexOf(preferredStyle).coerceAtLeast(0) }
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { styles.size }
    )
    val currentStyle = styles[pagerState.currentPage]
    
    // Save preferred style when page changes
    LaunchedEffect(pagerState.currentPage) {
        onStyleChanged(currentStyle)
    }
    
    val scope = rememberCoroutineScope()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Style preview pager (full screen)
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            QuoteStylePreview(
                style = styles[page],
                quoteText = quoteText.ifBlank { "Tap to add your quote..." },
                bookTitle = bookTitle.ifBlank { "Book Title" },
                author = author.ifBlank { "Author" },
                onTapQuote = { 
                    editMode = EditMode.Quote
                    showEditDialog = true 
                },
                onTapBook = { 
                    editMode = EditMode.Book
                    showEditDialog = true 
                },
                onTapAuthor = { 
                    editMode = EditMode.Author
                    showEditDialog = true 
                }
            )
        }
        
        // Navigation arrows for desktop (left/right)
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (pagerState.currentPage > 0) {
                IconButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "Previous style",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (pagerState.currentPage < styles.size - 1) {
                IconButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Next style",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        
        // Top bar with close and save
        TopActionBar(
            onClose = onDismiss,
            onSave = {
                if (quoteText.length >= QuoteCardConstants.MIN_QUOTE_LENGTH && bookTitle.isNotBlank()) {
                    onSave(quoteText, bookTitle, author, currentStyle)
                }
            },
            onShare = onShare?.let { shareCallback ->
                {
                    if (quoteText.length >= QuoteCardConstants.MIN_QUOTE_LENGTH && bookTitle.isNotBlank()) {
                        shareCallback(quoteText, bookTitle, author, currentStyle)
                    }
                }
            },
            canSave = quoteText.length >= QuoteCardConstants.MIN_QUOTE_LENGTH && bookTitle.isNotBlank(),
            modifier = Modifier.align(Alignment.TopCenter)
        )
        
        // Style indicator dots
        StyleIndicator(
            currentPage = pagerState.currentPage,
            pageCount = styles.size,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
        )
        
        // Style name badge
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 140.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.7f)
            ) {
                Text(
                    text = currentStyle.displayName,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    
    // Edit dialog
    if (showEditDialog) {
        EditTextDialog(
            mode = editMode,
            currentText = when (editMode) {
                EditMode.Quote -> quoteText
                EditMode.Book -> bookTitle
                EditMode.Author -> author
            },
            onDismiss = { showEditDialog = false },
            onSave = { text ->
                when (editMode) {
                    EditMode.Quote -> quoteText = text
                    EditMode.Book -> bookTitle = text
                    EditMode.Author -> author = text
                }
                showEditDialog = false
            }
        )
    }
    
    // Share confirmation dialog
    if (showShareConfirmDialog) {
        ShareConfirmationDialog(
            onDismiss = onDismissShareConfirm,
            onConfirm = onConfirmShare,
            isSharing = isSharing
        )
    }
}

enum class EditMode {
    Quote, Book, Author
}

@Composable
private fun QuoteStylePreview(
    style: QuoteCardStyle,
    quoteText: String,
    bookTitle: String,
    author: String,
    onTapQuote: () -> Unit,
    onTapBook: () -> Unit,
    onTapAuthor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradients = remember {
        QuoteCardStyle.entries.associate { style ->
            val (startColor, endColor) = QuoteCardStyleColors.getGradientColors(style)
            style to listOf(startColor, endColor)
        }
    }
    
    val textColor = QuoteCardStyleColors.getTextColor(style)
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradients[style] ?: listOf(Color.Gray, Color.DarkGray)))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // IReader logo at top
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Icon(
                    imageVector = Images.infinity(),
                    contentDescription = "IReader",
                    modifier = Modifier.size(28.dp),
                    tint = textColor.copy(alpha = 0.9f)
                )
                Text(
                    text = "IReader",
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Quote icon
            Icon(
                Icons.Default.FormatQuote,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = textColor.copy(alpha = 0.3f)
            )
            
            Spacer(Modifier.height(32.dp))
            
            // Quote text (tappable)
            Text(
                text = "\"$quoteText\"",
                style = MaterialTheme.typography.headlineMedium,
                color = textColor,
                textAlign = TextAlign.Center,
                fontStyle = FontStyle.Italic,
                lineHeight = 40.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onTapQuote)
                    .padding(16.dp)
            )
            
            Spacer(Modifier.height(48.dp))
            
            // Book title (tappable)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clickable(onClick = onTapBook)
                    .padding(8.dp)
            ) {
                Icon(
                    Icons.Default.Book,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = textColor.copy(alpha = 0.7f)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = bookTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Author (tappable)
            Text(
                text = "by $author",
                style = MaterialTheme.typography.bodyLarge,
                color = textColor.copy(alpha = 0.7f),
                modifier = Modifier
                    .clickable(onClick = onTapAuthor)
                    .padding(8.dp)
            )
        }
        
        // Tap hint for empty quote
        if (quoteText.isBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-50).dp)
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )
                
                Icon(
                    Icons.Default.TouchApp,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .scale(scale),
                    tint = textColor.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun TopActionBar(
    onClose: () -> Unit,
    onSave: () -> Unit,
    onShare: (() -> Unit)? = null,
    canSave: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Close button
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .size(48.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Share button (if callback provided)
            if (onShare != null) {
                Button(
                    onClick = onShare,
                    enabled = canSave,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5865F2), // Discord blue
                        contentColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.3f),
                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Share",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            
            // Save button
            Button(
                onClick = onSave,
                enabled = canSave,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    disabledContainerColor = Color.White.copy(alpha = 0.3f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                ),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Save",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun StyleIndicator(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(pageCount) { index ->
            val isSelected = currentPage == index
            val width by animateDpAsState(
                targetValue = if (isSelected) 24.dp else 8.dp,
                animationSpec = spring(dampingRatio = 0.8f),
                label = "width"
            )
            
            Box(
                modifier = Modifier
                    .width(width)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isSelected) Color.White else Color.White.copy(alpha = 0.3f)
                    )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTextDialog(
    mode: EditMode,
    currentText: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(currentText) }
    
    val (title, placeholder, minLength) = when (mode) {
        EditMode.Quote -> Triple("Edit Quote", "Enter your quote...", 10)
        EditMode.Book -> Triple("Edit Book Title", "Enter book title...", 1)
        EditMode.Author -> Triple("Edit Author", "Enter author name...", 0)
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text(placeholder) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = if (mode == EditMode.Quote) 3 else 1,
                    maxLines = if (mode == EditMode.Quote) 6 else 1,
                    shape = RoundedCornerShape(16.dp),
                    supportingText = if (mode == EditMode.Quote) {
                        {
                            Text(
                                "${text.length} characters (min $minLength)",
                                color = if (text.length < minLength && text.isNotEmpty()) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    } else null
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(text) },
                enabled = text.length >= minLength && (mode != EditMode.Book || text.isNotBlank()),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isSharing: Boolean,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { if (!isSharing) onDismiss() },
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = null,
                    tint = Color(0xFF5865F2), // Discord blue
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    "Share to Discord?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "This will share your quote to the IReader community Discord channel.",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "⏱️ Rate Limit: 30 seconds between shares",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            "🎨 Your selected style will be used",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                
                if (isSharing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Sharing...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isSharing,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5865F2), // Discord blue
                    contentColor = Color.White
                )
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Share", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSharing
            ) {
                Text("Cancel")
            }
        }
    )
}
