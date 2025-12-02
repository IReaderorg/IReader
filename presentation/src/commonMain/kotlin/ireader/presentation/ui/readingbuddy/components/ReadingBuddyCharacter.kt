package ireader.presentation.ui.readingbuddy.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.quote.BuddyAnimation
import ireader.domain.models.quote.BuddyMood
import ireader.domain.models.quote.ReadingBuddyState

/**
 * Animated Reading Buddy character - a cute rabbit using emoji art
 */
@Composable
fun ReadingBuddyCharacter(
    state: ReadingBuddyState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "buddy")
    
    // Bounce animation
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = when (state.animation) {
            BuddyAnimation.BOUNCE, BuddyAnimation.JUMP -> -15f
            BuddyAnimation.DANCE -> -10f
            else -> 0f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state.animation) {
                    BuddyAnimation.JUMP -> 300
                    BuddyAnimation.DANCE -> 400
                    else -> 500
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )
    
    // Scale animation for celebrate
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = when (state.animation) {
            BuddyAnimation.CELEBRATE -> 1.1f
            BuddyAnimation.SPARKLE -> 1.05f
            else -> 1f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    // Rotation for wave
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = when (state.animation) {
            BuddyAnimation.WAVE -> 5f
            BuddyAnimation.CHEER -> 10f
            else -> -5f
        },
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Buddy container with animations
        Box(
            modifier = Modifier
                .offset(y = bounceOffset.dp)
                .scale(scale),
            contentAlignment = Alignment.Center
        ) {
            // Background glow for special moods
            if (state.mood in listOf(BuddyMood.EXCITED, BuddyMood.CELEBRATING, BuddyMood.PROUD)) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(
                            when (state.mood) {
                                BuddyMood.CELEBRATING -> Color(0x30FFD700)
                                BuddyMood.PROUD -> Color(0x30FF6B6B)
                                else -> Color(0x20FF9800)
                            }
                        )
                )
            }
            
            // The rabbit character
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Rabbit face with ears
                Text(
                    text = getRabbitEmoji(state.mood, state.animation),
                    fontSize = 80.sp
                )
                
                // Sparkles for special animations
                if (state.animation == BuddyAnimation.SPARKLE || state.animation == BuddyAnimation.CELEBRATE) {
                    Text(
                        text = "✨",
                        fontSize = 24.sp,
                        modifier = Modifier.offset(y = (-20).dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Level badge
        LevelBadge(
            level = state.level,
            modifier = Modifier
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Speech bubble with message
        SpeechBubble(
            message = state.message,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun LevelBadge(
    level: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "⭐",
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Level $level",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun SpeechBubble(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Get the appropriate rabbit emoji based on mood and animation
 */
private fun getRabbitEmoji(mood: BuddyMood, animation: BuddyAnimation): String {
    return when {
        animation == BuddyAnimation.SLEEP -> "🐰💤"
        animation == BuddyAnimation.READ -> "🐰📖"
        animation == BuddyAnimation.CELEBRATE -> "🐰🎉"
        animation == BuddyAnimation.DANCE -> "🐰💃"
        mood == BuddyMood.SLEEPING -> "😴🐰"
        mood == BuddyMood.SLEEPY -> "🥱🐰"
        mood == BuddyMood.SAD -> "🥺🐰"
        mood == BuddyMood.HAPPY -> "😊🐰"
        mood == BuddyMood.EXCITED -> "🤩🐰"
        mood == BuddyMood.CELEBRATING -> "🎊🐰"
        mood == BuddyMood.PROUD -> "🏆🐰"
        mood == BuddyMood.CHEERING -> "📣🐰"
        else -> "🐰"
    }
}
