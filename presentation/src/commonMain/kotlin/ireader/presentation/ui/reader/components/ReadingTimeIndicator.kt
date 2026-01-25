package ireader.presentation.ui.reader.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.coroutines.delay

/**
 * Displays current reading time in the reader screen
 * Shows time in format: "5m 23s" or "1h 15m"
 */
@Composable
fun ReadingTimeIndicator(
    sessionStartTime: Long?,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(currentTimeToLong()) }
    
    // Update time every second
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = currentTimeToLong()
        }
    }
    
    val formattedTime = remember(currentTime, sessionStartTime) {
        if (sessionStartTime == null) {
            "0s"
        } else {
            val elapsedMs = currentTime - sessionStartTime
            val seconds = (elapsedMs / 1000) % 60
            val minutes = (elapsedMs / 60000) % 60
            val hours = elapsedMs / 3600000
            
            when {
                hours > 0 -> "${hours}h ${minutes}m"
                minutes > 0 -> "${minutes}m ${seconds}s"
                else -> "${seconds}s"
            }
        }
    }
    
    AnimatedVisibility(
        visible = isVisible && sessionStartTime != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = formattedTime,
                color = Color.White,
                fontSize = 12.sp,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
