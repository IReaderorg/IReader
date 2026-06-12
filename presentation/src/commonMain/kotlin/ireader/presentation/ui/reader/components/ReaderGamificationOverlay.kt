package ireader.presentation.ui.reader.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val XpGreen = Color(0xFF4CAF50)
private val StreakOrange = Color(0xFFFF7043)
private val LevelGold = Color(0xFFFFD700)

/**
 * In-reader gamification overlay showing session XP, streak, and level progress.
 * Positioned at bottom-right of the reader screen.
 */
@Composable
fun ReaderGamificationOverlay(
    sessionXpEarned: Int,
    currentStreak: Int,
    level: Int,
    levelProgress: Float,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Session XP
            if (sessionXpEarned > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("+", color = XpGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("$sessionXpEarned", color = XpGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(" XP", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                }
            }

            // Streak
            if (currentStreak > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = StreakOrange,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text("${currentStreak}d", color = StreakOrange, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Level progress mini bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Lv.$level", color = LevelGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = levelProgress.coerceIn(0f, 1f))
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.horizontalGradient(listOf(LevelGold, Color(0xFFFF9800)))
                            )
                    )
                }
            }
        }
    }
}
