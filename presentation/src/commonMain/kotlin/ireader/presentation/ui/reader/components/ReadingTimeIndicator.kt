package ireader.presentation.ui.reader.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.coroutines.delay

private val IndicatorBg = Color(0xFF1A1A2E)
private val IndicatorTrack = Color(0xFF2D2D44)
private val IndicatorFill = Color(0xFF4CAF50)
private val IndicatorFillHour = Color(0xFFFF9800)
private val IndicatorFillDay = Color(0xFFE91E63)

/**
 * Animated book-shaped reading time indicator.
 * Shows a circular progress that fills as minutes accumulate (1 fill = 1 minute).
 * After 60 minutes (1 hour), the circle resets and a small hour count appears.
 */
@Composable
fun ReadingTimeIndicator(
    sessionStartTime: Long?,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(currentTimeToLong()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = currentTimeToLong()
        }
    }

    val elapsedSeconds = remember(currentTime, sessionStartTime) {
        if (sessionStartTime == null) 0L else (currentTime - sessionStartTime) / 1000
    }

    val totalMinutes = elapsedSeconds / 60
    val currentMinuteSeconds = elapsedSeconds % 60
    val hours = totalMinutes / 60
    val minutesInHour = totalMinutes % 60

    // Progress: fills from 0 to 1 over 60 seconds (1 minute)
    val fillProgress by animateFloatAsState(
        targetValue = currentMinuteSeconds.toFloat() / 60f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "fill"
    )

    // Color shifts based on total time
    val fillColor = when {
        hours > 0 -> IndicatorFillDay
        totalMinutes >= 30 -> IndicatorFillHour
        else -> IndicatorFill
    }

    AnimatedVisibility(
        visible = elapsedSeconds > 0,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(IndicatorBg.copy(alpha = 0.85f))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(36.dp)) {
                val stroke = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                val sweepAngle = 360f * fillProgress

                // Track (empty circle)
                drawArc(
                    color = IndicatorTrack,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = stroke,
                    topLeft = Offset.Zero,
                    size = Size(size.width, size.height)
                )

                // Fill arc
                if (fillProgress > 0f) {
                    drawArc(
                        color = fillColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = stroke,
                        topLeft = Offset.Zero,
                        size = Size(size.width, size.height)
                    )
                }

                // Center dot
                drawCircle(
                    color = fillColor,
                    radius = 3.dp.toPx(),
                    center = Offset(size.width / 2, size.height / 2)
                )
            }
        }
    }
}
