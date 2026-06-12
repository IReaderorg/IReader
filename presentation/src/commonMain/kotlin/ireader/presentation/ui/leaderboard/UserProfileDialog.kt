package ireader.presentation.ui.leaderboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.entities.LeaderboardEntry
import ireader.presentation.ui.core.ui.AsyncImage

private fun rankColor(rank: Int): Color = when (rank) {
    1 -> Color(0xFFFFC73C); 2 -> Color(0xFFC6CCD6); 3 -> Color(0xFFD08B4B); else -> Color(0xFF8C7BAE)
}

private fun tierColor(tier: ireader.domain.models.gamification.ReaderTier): Color = when (tier) {
    ireader.domain.models.gamification.ReaderTier.BRONZE -> Color(0xFFCD7F32)
    ireader.domain.models.gamification.ReaderTier.SILVER -> Color(0xFFC6CCD6)
    ireader.domain.models.gamification.ReaderTier.GOLD -> Color(0xFFFFD700)
    ireader.domain.models.gamification.ReaderTier.PLATINUM -> Color(0xFF00BCD4)
    ireader.domain.models.gamification.ReaderTier.DIAMOND -> Color(0xFFE91E63)
    ireader.domain.models.gamification.ReaderTier.LEGEND -> Color(0xFF9C27B0)
}

/**
 * Full user-detail bottom sheet opened from a leaderboard row.
 * Shows avatar, level, XP, stats grid, tier badge, and reading achievements.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileSheet(
    entry: LeaderboardEntry,
    totalUsers: Int,
    onDismiss: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val rc = rankColor(entry.rank)
    val tier = if (totalUsers > 0) ireader.domain.models.gamification.ReaderTier.fromPercentile(entry.rank.toFloat() / totalUsers) else ireader.domain.models.gamification.ReaderTier.BRONZE
    val tierCol = tierColor(tier)
    val progress by animateFloatAsState(
        targetValue = if (entry.xpToNextLevel > 0) (entry.xp.toFloat() / entry.xpToNextLevel.toFloat()).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(800),
        label = "xp"
    )

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, dragHandle = null) {
        Column(Modifier.fillMaxWidth()) {
            // Gradient header with avatar
            Box(Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(cs.primary, cs.primaryContainer)))
                .padding(bottom = 20.dp)) {
                Column(Modifier.fillMaxWidth().padding(top = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    // Avatar with level ring
                    Box(contentAlignment = Alignment.BottomCenter) {
                        Box(Modifier.size(96.dp).clip(CircleShape).border(4.dp, rc, CircleShape)
                            .background(cs.onPrimary.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
                            if (entry.avatarUrl != null) {
                                AsyncImage(model = entry.avatarUrl, contentDescription = "Avatar",
                                    modifier = Modifier.size(96.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                            } else {
                                Text(entry.username.take(1).uppercase(), color = cs.onPrimary, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        // Level badge
                        Box(Modifier.clip(RoundedCornerShape(10.dp)).background(rc).padding(horizontal = 10.dp, vertical = 2.dp)) {
                            Text("Lv ${entry.level}", color = Color(0xFF2A1466), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(entry.username, color = cs.onPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(entry.levelTitle, color = cs.onPrimary.copy(alpha = 0.85f), fontSize = 13.sp)

                    // Tier badge
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.clip(RoundedCornerShape(20.dp)).background(tierCol.copy(alpha = 0.2f))
                        .border(1.dp, tierCol.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(tierIcon(tier), contentDescription = null, tint = tierCol, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("${tier.emblem} ${tier.display}", color = cs.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                // XP progress bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Level ${entry.level}", color = cs.onSurface, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text("${entry.xp} / ${entry.xpToNextLevel} XP", color = cs.onSurfaceVariant, fontSize = 12.sp)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = rc,
                    trackColor = cs.onSurface.copy(alpha = 0.08f),
                )

                Spacer(Modifier.height(20.dp))

                // Stats grid
                Text("Reading Stats", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(icon = Icons.Filled.EmojiEvents, value = "#${entry.rank}", label = "Rank", accent = rc, modifier = Modifier.weight(1f))
                    StatTile(icon = Icons.Filled.Star, value = "${entry.totalReadingTimeMinutes / 60}h", label = "Reading Time", accent = cs.primary, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(icon = Icons.Filled.MenuBook, value = "${entry.totalChaptersRead}", label = "Chapters", accent = Color(0xFF4CAF50), modifier = Modifier.weight(1f))
                    StatTile(icon = Icons.Filled.LocalFireDepartment, value = "${entry.readingStreak}d", label = "Streak", accent = Color(0xFFFF6B35), modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(icon = Icons.Filled.Speed, value = "${entry.booksCompleted}", label = "Books Completed", accent = Color(0xFF2196F3), modifier = Modifier.weight(1f))
                    StatTile(icon = Icons.Filled.Star, value = formatReadingTimeCompact(entry.totalReadingTimeMinutes), label = "Total Time", accent = Color(0xFF9C27B0), modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(24.dp).statusBarsPadding())
            }
        }
    }
}

@Composable
private fun StatTile(icon: ImageVector, value: String, label: String, accent: Color, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Column(modifier.clip(RoundedCornerShape(14.dp)).background(cs.surfaceVariant).padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(6.dp))
        Text(value, color = accent, fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center)
        Text(label, color = cs.onSurfaceVariant, fontSize = 11.sp)
    }
}

private fun tierIcon(tier: ireader.domain.models.gamification.ReaderTier): ImageVector = when (tier) {
    ireader.domain.models.gamification.ReaderTier.LEGEND -> Icons.Filled.EmojiEvents
    ireader.domain.models.gamification.ReaderTier.DIAMOND -> Icons.Filled.Star
    else -> Icons.Filled.Star
}

private fun formatReadingTimeCompact(minutes: Long): String {
    return when {
        minutes < 60 -> "${minutes}m"
        minutes < 1440 -> "${minutes / 60}h ${minutes % 60}m"
        else -> "${minutes / 1440}d"
    }
}
