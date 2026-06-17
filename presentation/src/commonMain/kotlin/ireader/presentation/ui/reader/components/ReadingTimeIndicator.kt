package ireader.presentation.ui.reader.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.coroutines.delay

/**
 * Filled clock reading time indicator.
 * A clock face that fills clockwise over 60 seconds (1 minute).
 * The fill grows as a pie wedge from 12 o'clock. Uses the reader's text color.
 */
@Composable
fun ReadingTimeIndicator(
    sessionStartTime: Long?,
    isVisible: Boolean,
    textColor: Color,
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

    val minuteProgress = remember(elapsedSeconds) {
        (elapsedSeconds % 60).toFloat() / 60f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = minuteProgress,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "clock-fill"
    )

    val fillColor = textColor.copy(alpha = 0.85f)
    val bgColor = textColor.copy(alpha = 0.12f)
    val borderColor = textColor.copy(alpha = 0.4f)
    val handColor = textColor.copy(alpha = 0.95f)

    AnimatedVisibility(
        visible = isVisible && elapsedSeconds > 0,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val cx = size.width / 2
            val cy = size.height / 2
            val radius = minOf(cx, cy) - 1.dp.toPx()
            val sweepAngle = 360f * animatedProgress

            // Clock body background
            drawCircle(
                color = bgColor,
                radius = radius,
                center = Offset(cx, cy)
            )

            // Filled wedge (pie slice from 12 o'clock)
            if (animatedProgress > 0f) {
                drawArc(
                    color = fillColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(cx - radius, cy - radius),
                    size = Size(radius * 2, radius * 2)
                )
            }

            // Clock border
            drawCircle(
                color = borderColor,
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Hour hand — always points to 12
            drawLine(
                color = handColor,
                start = Offset(cx, cy),
                end = Offset(cx, cy - radius * 0.55f),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Minute hand — sweeps clockwise with progress
            val minuteAngle = Math.toRadians((-90f + sweepAngle).toDouble())
            val minuteLen = radius * 0.75f
            drawLine(
                color = handColor,
                start = Offset(cx, cy),
                end = Offset(
                    cx + (minuteLen * kotlin.math.cos(minuteAngle)).toFloat(),
                    cy + (minuteLen * kotlin.math.sin(minuteAngle)).toFloat()
                ),
                strokeWidth = 1.2.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Center pin
            drawCircle(
                color = handColor,
                radius = 1.5.dp.toPx(),
                center = Offset(cx, cy)
            )
        }
    }
}
