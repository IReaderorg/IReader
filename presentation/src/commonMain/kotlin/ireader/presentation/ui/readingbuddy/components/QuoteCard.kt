package ireader.presentation.ui.readingbuddy.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.quote.Quote
import ireader.domain.models.quote.QuoteCardStyle
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

/**
 * Beautiful shareable quote card with various styles
 */
@Composable
fun QuoteCard(
    quote: Quote,
    style: QuoteCardStyle,
    onLike: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
    showActions: Boolean = true
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val (backgroundBrush, textColor, accentColor) = getStyleColors(style)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundBrush)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Opening quote mark
                Text(
                    text = "❝",
                    fontSize = 48.sp,
                    color = accentColor.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Quote text
                Text(
                    text = quote.text,
                    style = MaterialTheme.typography.titleLarge,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    color = textColor,
                    lineHeight = 32.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Closing quote mark
                Text(
                    text = "❞",
                    fontSize = 48.sp,
                    color = accentColor.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Divider
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(2.dp)
                        .background(accentColor.copy(alpha = 0.4f))
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Book title
                Text(
                    text = quote.bookTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
                
                // Author (if available)
                if (quote.author.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "by ${quote.author}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
                
                // Submitter credit
                if (quote.submitterUsername.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Shared by ${quote.submitterUsername}",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.6f)
                    )
                }
                
                // Actions
                if (showActions) {
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Like button
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { onLike() }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (quote.isLikedByUser) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = localizeHelper.localize(Res.string.like),
                                tint = if (quote.isLikedByUser) Color(0xFFE91E63) else textColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${quote.likesCount}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor.copy(alpha = 0.8f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(24.dp))
                        
                        // Share button
                        IconButton(
                            onClick = onShare,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(accentColor.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = localizeHelper.localize(Res.string.share),
                                tint = textColor
                            )
                        }
                    }
                }
                
                // App branding
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = localizeHelper.localize(Res.string.ireader),
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * Compact quote card for lists
 */
@Composable
fun QuoteCardCompact(
    quote: Quote,
    onLike: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Quote text
            Text(
                text = "\"${quote.text}\"",
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Book info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = quote.bookTitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (quote.author.isNotBlank()) {
                        Text(
                            text = quote.author,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Like button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onLike() }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (quote.isLikedByUser) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = localizeHelper.localize(Res.string.like),
                        tint = if (quote.isLikedByUser) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${quote.likesCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Get colors for each card style
 */
private fun getStyleColors(style: QuoteCardStyle): Triple<Brush, Color, Color> {
    return when (style) {
        QuoteCardStyle.GRADIENT_SUNSET -> Triple(
            Brush.linearGradient(listOf(Color(0xFFFF6B6B), Color(0xFFFFE66D))),
            Color.White,
            Color(0xFFFFFFFF)
        )
        QuoteCardStyle.GRADIENT_OCEAN -> Triple(
            Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF64B5F6))),
            Color.White,
            Color(0xFFFFFFFF)
        )
        QuoteCardStyle.GRADIENT_FOREST -> Triple(
            Brush.linearGradient(listOf(Color(0xFF11998E), Color(0xFF38EF7D))),
            Color.White,
            Color(0xFFFFFFFF)
        )
        QuoteCardStyle.GRADIENT_LAVENDER -> Triple(
            Brush.linearGradient(listOf(Color(0xFFE8D5E8), Color(0xFFF3E5F5))),
            Color(0xFF4A148C),
            Color(0xFF7B1FA2)
        )
        QuoteCardStyle.GRADIENT_MIDNIGHT -> Triple(
            Brush.linearGradient(listOf(Color(0xFF232526), Color(0xFF414345))),
            Color.White,
            Color(0xFF90CAF9)
        )
        QuoteCardStyle.MINIMAL_LIGHT -> Triple(
            Brush.linearGradient(listOf(Color(0xFFFAFAFA), Color(0xFFFFFFFF))),
            Color(0xFF212121),
            Color(0xFF757575)
        )
        QuoteCardStyle.MINIMAL_DARK -> Triple(
            Brush.linearGradient(listOf(Color(0xFF1E1E1E), Color(0xFF2D2D2D))),
            Color(0xFFE0E0E0),
            Color(0xFF9E9E9E)
        )
        QuoteCardStyle.PAPER_TEXTURE -> Triple(
            Brush.linearGradient(listOf(Color(0xFFF5F5DC), Color(0xFFFAF0E6))),
            Color(0xFF3E2723),
            Color(0xFF5D4037)
        )
        QuoteCardStyle.BOOK_COVER -> Triple(
            Brush.linearGradient(listOf(Color(0xFF8D6E63), Color(0xFFA1887F))),
            Color(0xFFFFF8E1),
            Color(0xFFFFECB3)
        )
    }
}
