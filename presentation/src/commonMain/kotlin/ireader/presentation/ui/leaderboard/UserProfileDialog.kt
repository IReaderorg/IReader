package ireader.presentation.ui.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.entities.LeaderboardEntry
import ireader.presentation.ui.community.formatReadingTimeCompact
import ireader.presentation.ui.core.ui.AsyncImage

private fun rankColor(rank: Int): Color = when (rank) {
    1 -> Color(0xFFFFC73C); 2 -> Color(0xFFC6CCD6); 3 -> Color(0xFFD08B4B); else -> Color(0xFF8C7BAE)
}

/**
 * Full user-detail bottom sheet opened from a leaderboard row.
 * Mirrors the profile look: gradient header, avatar with level ring, badge, stat grid.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileSheet(
    entry: LeaderboardEntry,
    onDismiss: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val rc = rankColor(entry.rank)
    val progress = if (entry.xpToNextLevel > 0)
        (entry.xp.toFloat() / entry.xpToNextLevel.toFloat()).coerceIn(0f, 1f) else 0f

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, dragHandle = null) {
        Column(Modifier.fillMaxWidth()) {
            // Gradient header
            Box(Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(cs.primary, cs.primaryContainer)))
                .padding(bottom = 18.dp)) {
                Column(Modifier.fillMaxWidth().padding(top = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.BottomCenter) {
                        Box(Modifier.size(92.dp).clip(CircleShape).border(3.dp, rc, CircleShape)
                            .background(cs.onPrimary.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
                            if (entry.avatarUrl != null) {
                                AsyncImage(model = entry.avatarUrl, contentDescription = "Avatar",
                                    modifier = Modifier.size(92.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                            } else {
                                Text(entry.username.take(1).uppercase(), color = cs.onPrimary, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Box(Modifier.clip(RoundedCornerShape(8.dp)).background(rc).padding(horizontal = 9.dp, vertical = 1.dp)) {
                            Text("Lv ${entry.level}", color = Color(0xFF2A1466), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(entry.username, color = cs.onPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(entry.levelTitle, color = cs.onPrimary.copy(alpha = 0.85f), fontSize = 13.sp)
                    if (entry.hasBadge) {
                        Spacer(Modifier.height(6.dp))
                        Box(Modifier.clip(RoundedCornerShape(20.dp)).background(cs.onPrimary.copy(alpha = 0.16f))
                            .padding(horizontal = 12.dp, vertical = 5.dp)) {
                            Text("🏅 ${entry.badgeType?.replaceFirstChar { it.uppercase() } ?: "Badge"}",
                                color = cs.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                // XP bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Level ${entry.level}", color = cs.onSurface, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text("${entry.xp} / ${entry.xpToNextLevel} XP", color = cs.onSurfaceVariant, fontSize = 12.sp)
                }
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(cs.onSurface.copy(alpha = 0.12f))) {
                    Box(Modifier.fillMaxWidth(progress).height(10.dp).clip(RoundedCornerShape(5.dp))
                        .background(Brush.horizontalGradient(listOf(cs.primary, rc))))
                }

                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("🏆", "#${entry.rank}", "Rank", rc, Modifier.weight(1f))
                    StatTile("⏱", formatReadingTimeCompact(entry.totalReadingTimeMinutes), "Read time", cs.primary, Modifier.weight(1f))
                }
                Spacer(Modifier.height(24.dp).statusBarsPadding())
            }
        }
    }
}

@Composable
private fun StatTile(emoji: String, value: String, label: String, accent: Color, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Column(modifier.clip(RoundedCornerShape(14.dp)).background(cs.surfaceVariant).padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = accent, fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center)
        Text(label, color = cs.onSurfaceVariant, fontSize = 11.sp)
    }
}
