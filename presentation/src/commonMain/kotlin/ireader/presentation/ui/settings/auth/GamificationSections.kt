package ireader.presentation.ui.settings.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.gamification.AchievementView
import ireader.domain.models.gamification.OwnedTitle
import ireader.domain.models.gamification.ReadingActivityItem

/** Celebration dialog shown when achievements are freshly unlocked, with a Share-to-Discord CTA. */
@Composable
fun AchievementUnlockDialog(
    unlocks: List<ireader.domain.models.gamification.UnlockedAchievement>,
    shareEnabled: Boolean,
    onShare: (name: String, tier: String) -> Unit,
    onDismiss: () -> Unit,
) {
    if (unlocks.isEmpty()) return
    val first = unlocks.first()
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Awesome!") }
        },
        dismissButton = if (shareEnabled) {
            { TextButton(onClick = { onShare(first.name, first.tier); onDismiss() }) { Text("Share to Discord") } }
        } else null,
        icon = { Text(first.icon, fontSize = 40.sp) },
        title = {
            Text(
                if (unlocks.size == 1) "Achievement Unlocked!" else "${unlocks.size} Achievements Unlocked!",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                unlocks.take(5).forEach { u ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(u.icon, fontSize = 22.sp)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(u.name, fontWeight = FontWeight.SemiBold)
                            Text("+${u.rewardXp} XP · +${u.rewardStones} 💎", fontSize = 12.sp,
                                color = rarityColor(u.tier))
                        }
                    }
                }
            }
        },
    )
}

private fun rarityColor(rarity: String): Color = when (rarity.uppercase()) {
    "LEGENDARY" -> Color(0xFFFFB300)
    "PLATINUM" -> Color(0xFF00BCD4)
    "EPIC", "GOLD" -> Color(0xFF9C27B0)
    "RARE", "SILVER" -> Color(0xFF2196F3)
    else -> Color(0xFF8D6E63)
}

/** Level / XP / spirit-stone hero card with a daily check-in action. Works signed-out (local-first). */
@Composable
fun LevelProgressCard(
    level: Int,
    levelTitle: String,
    levelProgress: Float,
    spiritStones: Long,
    checkinStreak: Int,
    signedIn: Boolean,
    onCheckIn: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(56.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("$level", color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold, fontSize = 22.sp)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(levelTitle, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { levelProgress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    )
                    Text("Level ${level + 1} next", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Diamond, null, tint = Color(0xFF00BCD4), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("$spiritStones Spirit Stones", style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(Modifier.weight(1f))
                if (checkinStreak > 0) {
                    Icon(Icons.Filled.LocalFireDepartment, null, tint = Color(0xFFFF7043),
                        modifier = Modifier.size(18.dp))
                    Text(" $checkinStreak", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            if (signedIn) {
                Spacer(Modifier.height(12.dp))
                Button(onClick = onCheckIn, modifier = Modifier.fillMaxWidth()) {
                    Text("Daily Check-in")
                }
            }
        }
    }
}

/** Horizontal achievement showcase: completed first, then in-progress with a bar. */
@Composable
fun AchievementShowcaseSection(achievements: List<AchievementView>) {
    if (achievements.isEmpty()) return
    val sorted = achievements.sortedWith(compareByDescending<AchievementView> { it.isCompleted }.thenByDescending { it.fraction })
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Achievements", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(sorted) { a ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(72.dp),
                    ) {
                        Box(
                            Modifier.size(56.dp).clip(CircleShape)
                                .background(
                                    if (a.isCompleted) rarityColor(a.def.tier).copy(alpha = 0.25f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(2.dp, rarityColor(a.def.tier).copy(alpha = if (a.isCompleted) 1f else 0.3f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) { Text(a.def.icon, fontSize = 24.sp) }
                        Spacer(Modifier.height(4.dp))
                        Text(a.def.name, style = MaterialTheme.typography.labelSmall, maxLines = 2,
                            color = MaterialTheme.colorScheme.onSurface)
                        if (!a.isCompleted) {
                            LinearProgressIndicator(
                                progress = { a.fraction },
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Owned titles; tap to activate (cosmetic). */
@Composable
fun TitlesSection(titles: List<OwnedTitle>, onActivate: (String?) -> Unit) {
    if (titles.isEmpty()) return
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Titles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(titles) { t ->
                    val active = t.isActive
                    Box(
                        Modifier.clip(RoundedCornerShape(20.dp))
                            .background(
                                if (active) rarityColor(t.rarity).copy(alpha = 0.25f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(1.dp, rarityColor(t.rarity), RoundedCornerShape(20.dp))
                            .clickable { onActivate(if (active) null else t.titleId) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Text("📛 ${t.titleName}", style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

/** Followers / following + recent activity feed. */
@Composable
fun SocialActivitySection(followers: Int, following: Int, activity: List<ReadingActivityItem>) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row {
                Text("$following", fontWeight = FontWeight.Bold)
                Text(" Following   ", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$followers", fontWeight = FontWeight.Bold)
                Text(" Followers", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (activity.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("Recent activity", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                activity.take(8).forEach { a ->
                    val emoji = when (a.type) {
                        "ACHIEVEMENT" -> "🏅"; "REVIEW" -> "⭐"; "VOTE" -> "🗳"; else -> "📖"
                    }
                    Text(
                        "$emoji ${a.description.ifBlank { a.bookTitle ?: a.type }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 3.dp),
                    )
                }
            }
        }
    }
}
