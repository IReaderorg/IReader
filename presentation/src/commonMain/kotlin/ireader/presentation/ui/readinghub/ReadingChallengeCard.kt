package ireader.presentation.ui.readinghub

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.gamification.ChallengeType
import ireader.domain.models.gamification.ReadingChallenge

private val ChallengeGreen = Color(0xFF4CAF50)
private val ChallengeBlue = Color(0xFF2196F3)
private val ChallengePurple = Color(0xFF9C27B0)

@Composable
fun ReadingChallengeCard(
    dailyChallenge: ReadingChallenge?,
    weeklyChallenge: ReadingChallenge?,
    monthlyChallenge: ReadingChallenge?,
    onCreateChallenge: (ChallengeType, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(ChallengeType.DAILY) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎯", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Reading Goals",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Daily Challenge
            ChallengeRow(
                challenge = dailyChallenge,
                type = ChallengeType.DAILY,
                color = ChallengeGreen,
                onCreate = {
                    selectedType = ChallengeType.DAILY
                    showCreateDialog = true
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Weekly Challenge
            ChallengeRow(
                challenge = weeklyChallenge,
                type = ChallengeType.WEEKLY,
                color = ChallengeBlue,
                onCreate = {
                    selectedType = ChallengeType.WEEKLY
                    showCreateDialog = true
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Monthly Challenge
            ChallengeRow(
                challenge = monthlyChallenge,
                type = ChallengeType.MONTHLY,
                color = ChallengePurple,
                onCreate = {
                    selectedType = ChallengeType.MONTHLY
                    showCreateDialog = true
                }
            )
        }
    }

    if (showCreateDialog) {
        CreateChallengeDialog(
            type = selectedType,
            onDismiss = { showCreateDialog = false },
            onCreate = { minutes ->
                onCreateChallenge(selectedType, minutes)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun ChallengeRow(
    challenge: ReadingChallenge?,
    type: ChallengeType,
    color: Color,
    onCreate: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = challenge?.progress ?: 0f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "challenge_progress"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            if (challenge?.isCompleted == true) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(type.emoji, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = type.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (challenge != null) {
                    Text(
                        text = "${challenge.currentMinutes}/${challenge.goalMinutes} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (challenge != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color.copy(alpha = 0.15f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.7f)))
                            )
                    )
                }
                if (challenge.isCompleted) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "+${challenge.rewardStones} 💎 Earned!",
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap to set a goal",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (challenge == null) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f))
                    .clickable { onCreate() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Create goal",
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun CreateChallengeDialog(
    type: ChallengeType,
    onDismiss: () -> Unit,
    onCreate: (Long) -> Unit
) {
    var inputMinutes by remember { mutableStateOf("") }

    val presetMinutes = when (type) {
        ChallengeType.DAILY -> listOf(15L, 30L, 60L, 90L)
        ChallengeType.WEEKLY -> listOf(120L, 180L, 300L, 420L)
        ChallengeType.MONTHLY -> listOf(600L, 900L, 1200L, 1800L)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(type.emoji, fontSize = 36.sp)
                Spacer(Modifier.height(8.dp))
                Text("Set ${type.label}", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    "Choose a reading goal for this ${type.label.lowercase().removeSuffix(" goal")}:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))

                // Preset buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetMinutes.take(2).forEach { minutes ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { inputMinutes = minutes.toString() },
                            shape = RoundedCornerShape(8.dp),
                            color = if (inputMinutes == minutes.toString()) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Text(
                                text = formatGoalTime(minutes),
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetMinutes.drop(2).forEach { minutes ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { inputMinutes = minutes.toString() },
                            shape = RoundedCornerShape(8.dp),
                            color = if (inputMinutes == minutes.toString()) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Text(
                                text = formatGoalTime(minutes),
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = inputMinutes,
                    onValueChange = { inputMinutes = it.filter { c -> c.isDigit() } },
                    label = { Text("Custom (minutes)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    inputMinutes.toLongOrNull()?.let { onCreate(it) }
                },
                enabled = inputMinutes.toLongOrNull()?.let { it > 0 } == true
            ) {
                Text("Create Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatGoalTime(minutes: Long): String {
    return when {
        minutes < 60 -> "${minutes}m"
        minutes < 1440 -> "${minutes / 60}h ${minutes % 60}m".trimEnd(' ', 'm')
        else -> "${minutes / 1440}d"
    }
}
