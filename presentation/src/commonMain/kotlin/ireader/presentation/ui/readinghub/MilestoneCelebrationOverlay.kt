package ireader.presentation.ui.readinghub

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.gamification.Milestone
import kotlinx.coroutines.delay

private val GoldColor = Color(0xFFFFD700)
private val DiamondColor = Color(0xFFE91E63)
private val FireColor = Color(0xFFFF5722)

@Composable
fun MilestoneCelebrationOverlay(
    milestone: Milestone?,
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(visible) {
        if (visible) {
            delay(4000)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible && milestone != null,
        enter = scaleIn(initialScale = 0.5f) + fadeIn(),
        exit = scaleOut(targetScale = 0.5f) + fadeOut(),
        modifier = modifier
    ) {
        milestone?.let {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                // Floating particles effect
                val infiniteTransition = rememberInfiniteTransition(label = "particles")
                val particleScale by infiniteTransition.animateFloat(
                    initialValue = 0.8f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "particle_scale"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .scale(particleScale),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    listOf(
                                        getColorForMilestone(milestone),
                                        getColorForMilestone(milestone).copy(alpha = 0.8f)
                                    )
                                )
                            )
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Big emoji
                            Text(milestone.emoji, fontSize = 64.sp)

                            Spacer(Modifier.height(16.dp))

                            // Title
                            Text(
                                text = milestone.title,
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(8.dp))

                            // Description
                            Text(
                                text = milestone.description,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(20.dp))

                            // Reward
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("💎", fontSize = 20.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "+${milestone.rewardStones} Spirit Stones",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            // Dismiss hint
                            Text(
                                text = "Tap anywhere to continue",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MilestoneToast(
    milestone: Milestone,
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(visible) {
        if (visible) {
            delay(3000)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + androidx.compose.animation.slideInVertically(initialOffsetY = { -it }),
        exit = fadeOut() + androidx.compose.animation.slideOutVertically(targetOffsetY = { -it }),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = getColorForMilestone(milestone),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(milestone.emoji, fontSize = 28.sp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = milestone.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = milestone.description,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "+${milestone.rewardStones} 💎",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun getColorForMilestone(milestone: Milestone): Color {
    return when {
        milestone.rewardStones >= 1000 -> DiamondColor
        milestone.rewardStones >= 500 -> GoldColor
        milestone.rewardStones >= 100 -> Color(0xFF9C27B0)
        milestone.rewardStones >= 50 -> Color(0xFF2196F3)
        else -> FireColor
    }
}
