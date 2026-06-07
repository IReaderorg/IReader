package ireader.presentation.ui.reward

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.AchievementCategory
import ireader.domain.models.entities.Reward
import ireader.domain.models.entities.UserAchievement
import ireader.presentation.ui.common.FeatureScreenScaffold

/**
 * Reward screen showing user's level, XP progress, achievements, and recent rewards.
 *
 * State is managed by [RewardViewModel]. The screen displays:
 * - Level card with XP progress bar
 * - List of earned achievements grouped by category
 * - List of recent rewards with XP values
 */
@Composable
fun RewardScreen(
    vm: RewardViewModel,
    onBack: () -> Unit
) {
    val state by vm.state.collectAsState()

    FeatureScreenScaffold(
        title = "Rewards",
        icon = Icons.Default.EmojiEvents,
        iconTint = Color(0xFFFFD700),
        onBack = onBack,
        isLoading = state.isLoading,
        isEmpty = state.achievements.isEmpty() && state.rewards.isEmpty(),
        emptyMessage = "No rewards yet",
        emptySubMessage = "Start reading to earn rewards and achievements!"
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Level card with XP progress
            item {
                LevelCard(
                    level = state.currentLevel,
                    xp = state.currentXp,
                    xpToNextLevel = state.xpToNextLevel,
                    totalXp = state.totalXp
                )
            }

            // Achievements section
            if (state.achievements.isNotEmpty()) {
                item {
                    SectionHeader(title = "Achievements")
                }
                items(
                    items = state.achievements,
                    key = { it.id }
                ) { achievement ->
                    AchievementCard(achievement = achievement)
                }
            }

            // Recent rewards section
            if (state.rewards.isNotEmpty()) {
                item {
                    SectionHeader(title = "Recent Rewards")
                }
                items(
                    items = state.rewards,
                    key = { it.id }
                ) { reward ->
                    RewardItemCard(reward = reward)
                }
            }
        }
    }
}

/**
 * Displays the user's current level, XP progress, and total XP.
 */
@Composable
private fun LevelCard(
    level: Int,
    xp: Long,
    xpToNextLevel: Long,
    totalXp: Long
) {
    val progress = if (xpToNextLevel > 0) {
        (xp.toFloat() / xpToNextLevel.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Level badge
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "LVL",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$level",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = getLevelTitle(level),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFFFFD700),
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$xp / $xpToNextLevel XP • $totalXp total",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Section header used to group content within the screen.
 */
@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

/**
 * Card displaying a single achievement with icon, name, description, and category badge.
 */
@Composable
private fun AchievementCard(achievement: UserAchievement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = achievement.icon,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                CategoryBadge(category = achievement.category)
            }
        }
    }
}

/**
 * Card displaying a single reward with icon, name, description, and XP value.
 */
@Composable
private fun RewardItemCard(reward: Reward) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reward.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = reward.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (reward.xpValue > 0) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFD700).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "+${reward.xpValue} XP",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFA000),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Small badge showing the achievement category.
 */
@Composable
private fun CategoryBadge(category: AchievementCategory) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    ) {
        Text(
            text = category.name.lowercase()
                .replace("_", " ")
                .replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

/**
 * Returns the display title for a given level number.
 */
private fun getLevelTitle(level: Int): String = when {
    level <= 5 -> "Novice Reader"
    level <= 15 -> "Curious Reader"
    level <= 30 -> "Avid Reader"
    level <= 50 -> "Bookworm"
    level <= 100 -> "Master Reader"
    level <= 200 -> "Literary Legend"
    else -> "Reading Deity"
}
