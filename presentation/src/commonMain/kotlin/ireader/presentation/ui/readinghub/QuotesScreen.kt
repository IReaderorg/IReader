package ireader.presentation.ui.readinghub

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.quote.Quote
import ireader.domain.models.quote.QuoteCardStyle
import ireader.i18n.resources.Res
import ireader.i18n.resources.back
import ireader.i18n.resources.be_the_first_to_share_your_favorite_book_quotes
import ireader.i18n.resources.change_style
import ireader.i18n.resources.choose_style
import ireader.i18n.resources.like
import ireader.i18n.resources.loading
import ireader.i18n.resources.loading_quotes
import ireader.i18n.resources.no_quotes_yet
import ireader.i18n.resources.quote_of_the_day
import ireader.i18n.resources.quotes
import ireader.i18n.resources.rotation
import ireader.i18n.resources.scale
import ireader.i18n.resources.share
import ireader.i18n.resources.submit_your_quote
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Instagram-style Quotes Screen with vertical pager for immersive quote browsing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesScreen(
    quotes: List<Quote>,
    dailyQuote: Quote?,
    selectedStyle: QuoteCardStyle,
    isLoading: Boolean,
    onBack: () -> Unit,
    onToggleLike: (Quote) -> Unit,
    onShare: (Quote) -> Unit,
    onStyleChange: (QuoteCardStyle) -> Unit,
    onSubmitQuote: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allQuotes = remember(dailyQuote, quotes) {
        buildList {
            dailyQuote?.let { add(it.copy(id = "daily_${it.id}")) }
            addAll(quotes)
        }
    }
    
    val pagerState = rememberPagerState(pageCount = { allQuotes.size.coerceAtLeast(1) })
    var showStylePicker by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (isLoading) {
            QuotesLoadingScreen()
        } else if (allQuotes.isEmpty()) {
            EmptyQuotesScreen(onSubmitQuote = onSubmitQuote)
        } else {
            // Vertical Pager - Instagram Reels style
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1
            ) { page ->
                val quote = allQuotes[page]
                val isDailyQuote = quote.id.startsWith("daily_")
                
                InstagramStyleQuoteCard(
                    quote = quote,
                    style = selectedStyle,
                    isDailyQuote = isDailyQuote,
                    onLike = { onToggleLike(quote) },
                    onShare = { onShare(quote) },
                    onDoubleTap = { onToggleLike(quote) }
                )
            }
            
            // Top Bar Overlay
            QuotesTopBar(
                onBack = onBack,
                onStylePicker = { showStylePicker = true },
                currentPage = pagerState.currentPage + 1,
                totalPages = allQuotes.size
            )
            
            // Bottom Actions Overlay
            QuotesBottomBar(
                onSubmit = onSubmitQuote,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
            
            // Page Indicator
            if (allQuotes.size > 1) {
                VerticalPageIndicator(
                    currentPage = pagerState.currentPage,
                    totalPages = allQuotes.size,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                )
            }
        }
        
        // Style Picker Bottom Sheet
        if (showStylePicker) {
            StylePickerBottomSheet(
                selectedStyle = selectedStyle,
                onStyleChange = {
                    onStyleChange(it)
                    showStylePicker = false
                },
                onDismiss = { showStylePicker = false }
            )
        }
    }
}

@Composable
private fun QuotesLoadingScreen() {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1a1a2e),
                        Color(0xFF16213e),
                        Color(0xFF0f3460)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated book icon
            val infiniteTransition = rememberInfiniteTransition(label = localizeHelper.localize(Res.string.loading))
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = localizeHelper.localize(Res.string.scale)
            )
            val rotation by infiniteTransition.animateFloat(
                initialValue = -5f,
                targetValue = 5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = localizeHelper.localize(Res.string.rotation)
            )
            
            Text(
                text = "📚",
                fontSize = 64.sp,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        rotationZ = rotation
                    }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = localizeHelper.localize(Res.string.loading_quotes),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Shimmer dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { index ->
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = index * 200),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = alpha))
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyQuotesScreen(onSubmitQuote: () -> Unit) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF2d3436),
                        Color(0xFF000000)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("📖", fontSize = 72.sp)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = localizeHelper.localize(Res.string.no_quotes_yet),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = localizeHelper.localize(Res.string.be_the_first_to_share_your_favorite_book_quotes),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onSubmitQuote,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Submit Quote", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun InstagramStyleQuoteCard(
    quote: Quote,
    style: QuoteCardStyle,
    isDailyQuote: Boolean,
    onLike: () -> Unit,
    onShare: () -> Unit,
    onDoubleTap: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var showHeart by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    val backgroundGradient = getQuoteGradient(style)
    val textColor = getQuoteTextColor(style)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onDoubleTap()
                        showHeart = true
                        coroutineScope.launch {
                            delay(1000)
                            showHeart = false
                        }
                    }
                )
            }
    ) {
        // Quote Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 100.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Daily Quote Badge
            if (isDailyQuote) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✨", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = localizeHelper.localize(Res.string.quote_of_the_day),
                            style = MaterialTheme.typography.labelMedium,
                            color = textColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            // Opening Quote
            Text(
                text = "❝",
                fontSize = 56.sp,
                color = textColor.copy(alpha = 0.4f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Quote Text
            Text(
                text = quote.text,
                style = MaterialTheme.typography.headlineSmall,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                color = textColor,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Closing Quote
            Text(
                text = "❞",
                fontSize = 56.sp,
                color = textColor.copy(alpha = 0.4f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Divider
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(2.dp)
                    .background(textColor.copy(alpha = 0.3f))
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Book Info
            Text(
                text = quote.bookTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )
            
            if (quote.author.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "by ${quote.author}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor.copy(alpha = 0.8f)
                )
            }
        }
        
        // Side Actions (Instagram style)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Like Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onLike,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
                ) {
                    Icon(
                        imageVector = if (quote.isLikedByUser) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = localizeHelper.localize(Res.string.like),
                        tint = if (quote.isLikedByUser) Color(0xFFFF6B6B) else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${quote.likesCount}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            // Share Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onShare,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = localizeHelper.localize(Res.string.share),
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = localizeHelper.localize(Res.string.share),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
        
        // Submitter Info (Bottom)
        if (quote.submitterUsername.isNotBlank()) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 130.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = quote.submitterUsername.first().uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = quote.submitterUsername,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Double-tap heart animation
        AnimatedVisibility(
            visible = showHeart,
            enter = scaleIn(initialScale = 0.5f) + fadeIn(),
            exit = scaleOut(targetScale = 1.5f) + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(
                text = "❤️",
                fontSize = 100.sp
            )
        }
        
        // App Branding
        Text(
            text = "📚 IReader",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 140.dp)
        )
    }
}

@Composable
private fun QuotesTopBar(
    onBack: () -> Unit,
    onStylePicker: () -> Unit,
    currentPage: Int,
    totalPages: Int
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                )
            )
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = localizeHelper.localize(Res.string.back),
                tint = Color.White
            )
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = localizeHelper.localize(Res.string.quotes),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            if (totalPages > 0) {
                Text(
                    text = "$currentPage of $totalPages",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
        
        IconButton(onClick = onStylePicker) {
            Icon(
                Icons.Outlined.Palette,
                contentDescription = localizeHelper.localize(Res.string.change_style),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun QuotesBottomBar(
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                )
            )
            .padding(24.dp)
            .navigationBarsPadding()
    ) {
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = localizeHelper.localize(Res.string.submit_your_quote),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun VerticalPageIndicator(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(totalPages.coerceAtMost(10)) { index ->
            val isSelected = index == currentPage
            Box(
                modifier = Modifier
                    .width(if (isSelected) 4.dp else 3.dp)
                    .height(if (isSelected) 24.dp else 12.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isSelected) Color.White
                        else Color.White.copy(alpha = 0.4f)
                    )
            )
        }
        if (totalPages > 10) {
            Text(
                text = "...",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StylePickerBottomSheet(
    selectedStyle: QuoteCardStyle,
    onStyleChange: (QuoteCardStyle) -> Unit,
    onDismiss: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = localizeHelper.localize(Res.string.choose_style),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(QuoteCardStyle.entries, key = { it.name }) { style ->
                    StylePreviewChip(
                        style = style,
                        isSelected = style == selectedStyle,
                        onClick = { onStyleChange(style) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StylePreviewChip(
    style: QuoteCardStyle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val gradient = getQuoteGradient(style)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(gradient)
                .clickable { onClick() }
                .then(
                    if (isSelected) Modifier.background(
                        Color.White.copy(alpha = 0.3f),
                        RoundedCornerShape(16.dp)
                    ) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = style.name.lowercase()
                .replace("_", " ")
                .split(" ")
                .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                .take(10),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun getQuoteGradient(style: QuoteCardStyle): Brush {
    return when (style) {
        QuoteCardStyle.GRADIENT_SUNSET -> Brush.verticalGradient(
            listOf(Color(0xFFFF6B6B), Color(0xFFFFE66D), Color(0xFFFF8E53))
        )
        QuoteCardStyle.GRADIENT_OCEAN -> Brush.verticalGradient(
            listOf(Color(0xFF667EEA), Color(0xFF64B5F6), Color(0xFF4FC3F7))
        )
        QuoteCardStyle.GRADIENT_FOREST -> Brush.verticalGradient(
            listOf(Color(0xFF11998E), Color(0xFF38EF7D), Color(0xFF56AB2F))
        )
        QuoteCardStyle.GRADIENT_LAVENDER -> Brush.verticalGradient(
            listOf(Color(0xFFE8D5E8), Color(0xFFF3E5F5), Color(0xFFCE93D8))
        )
        QuoteCardStyle.GRADIENT_MIDNIGHT -> Brush.verticalGradient(
            listOf(Color(0xFF232526), Color(0xFF414345), Color(0xFF1a1a2e))
        )
        QuoteCardStyle.MINIMAL_LIGHT -> Brush.verticalGradient(
            listOf(Color(0xFFFAFAFA), Color(0xFFFFFFFF), Color(0xFFF5F5F5))
        )
        QuoteCardStyle.MINIMAL_DARK -> Brush.verticalGradient(
            listOf(Color(0xFF1E1E1E), Color(0xFF2D2D2D), Color(0xFF121212))
        )
        QuoteCardStyle.PAPER_TEXTURE -> Brush.verticalGradient(
            listOf(Color(0xFFF5F5DC), Color(0xFFFAF0E6), Color(0xFFEEE8CD))
        )
        QuoteCardStyle.BOOK_COVER -> Brush.verticalGradient(
            listOf(Color(0xFF8D6E63), Color(0xFFA1887F), Color(0xFF6D4C41))
        )
    }
}

private fun getQuoteTextColor(style: QuoteCardStyle): Color {
    return when (style) {
        QuoteCardStyle.GRADIENT_LAVENDER -> Color(0xFF4A148C)
        QuoteCardStyle.MINIMAL_LIGHT -> Color(0xFF212121)
        QuoteCardStyle.PAPER_TEXTURE -> Color(0xFF3E2723)
        else -> Color.White
    }
}
