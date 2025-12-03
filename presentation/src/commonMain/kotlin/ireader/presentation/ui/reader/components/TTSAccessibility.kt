package ireader.presentation.ui.reader.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import kotlin.time.Duration

/**
 * Accessibility components for TTS
 * Requirements: 11.1, 11.2, 11.3, 11.4, 11.5
 */

/**
 * Screen reader announcements for TTS state changes
 */
@Composable
fun TTSScreenReaderAnnouncement(
    isPlaying: Boolean,
    currentText: String,
    speechRate: Float
) {
    // Live region for screen reader announcements
    Box(
        modifier = Modifier
            .size(0.dp) // Invisible but accessible
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = buildScreenReaderText(isPlaying, currentText, speechRate)
            }
    )
}

/**
 * Build screen reader announcement text
 */
private fun buildScreenReaderText(
    isPlaying: Boolean,
    currentText: String,
    speechRate: Float
): String {
    return buildString {
        if (isPlaying) {
            append("Playing: ")
            append(currentText.take(100)) // Limit length
            if (currentText.length > 100) {
                append("...")
            }
        } else {
            append("Paused")
        }
        append(". Speed: ${(speechRate * 100).toInt()} percent")
    }
}

/**
 * Visual waveform feedback for audio playback
 * Requirements: 11.3
 */
@Composable
fun TTSWaveformVisualizer(
    isPlaying: Boolean,
    amplitude: Float = 0.5f,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    var phase by remember { mutableStateOf(0f) }
    
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                kotlinx.coroutines.delay(16) // ~60 FPS
                phase += 0.1f
                if (phase > 2 * kotlin.math.PI) {
                    phase = 0f
                }
            }
        }
    }
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .semantics {
                contentDescription = if (isPlaying) "Audio playing" else "Audio paused"
            }
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        
        if (!isPlaying) {
            // Draw flat line when paused
            drawLine(
                color = color.copy(alpha = 0.3f),
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = 2f
            )
            return@Canvas
        }
        
        // Draw waveform
        val path = Path()
        val points = 100
        val step = width / points
        
        path.moveTo(0f, centerY)
        
        for (i in 0..points) {
            val x = i * step
            val frequency = 2f
            val waveAmplitude = amplitude * height * 0.3f
            val y = centerY + sin((i * frequency + phase) * 0.1) * waveAmplitude
            path.lineTo(x, y.toFloat())
        }
        
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3f)
        )
    }
}

/**
 * High contrast mode support for TTS controls
 * Requirements: 11.4
 */
@Composable
fun rememberHighContrastColors(
    isHighContrast: Boolean
): TTSColors {
    val colorScheme = MaterialTheme.colorScheme
    
    return if (isHighContrast) {
        TTSColors(
            primary = Color.Black,
            onPrimary = Color.White,
            background = Color.White,
            onBackground = Color.Black,
            surface = Color.White,
            onSurface = Color.Black,
            highlight = Color.Yellow,
            onHighlight = Color.Black
        )
    } else {
        TTSColors(
            primary = colorScheme.primary,
            onPrimary = colorScheme.onPrimary,
            background = colorScheme.background,
            onBackground = colorScheme.onBackground,
            surface = colorScheme.surface,
            onSurface = colorScheme.onSurface,
            highlight = colorScheme.primaryContainer,
            onHighlight = colorScheme.onPrimaryContainer
        )
    }
}

/**
 * Color scheme for TTS components
 */
data class TTSColors(
    val primary: Color,
    val onPrimary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val highlight: Color,
    val onHighlight: Color
)

/**
 * Accessibility labels for TTS controls
 */
object TTSAccessibilityLabels {
    const val PLAY_BUTTON = "Play text-to-speech"
    const val PAUSE_BUTTON = "Pause text-to-speech"
    const val SKIP_FORWARD = "Skip forward 10 seconds"
    const val SKIP_BACKWARD = "Skip backward 10 seconds"
    const val SPEED_SLIDER = "Adjust playback speed"
    const val PROGRESS_BAR = "Playback progress"
    const val VOICE_SELECTION = "Select voice"
    const val DOWNLOAD_VOICE = "Download voice"
    const val DELETE_VOICE = "Delete voice"
    const val PREVIEW_VOICE = "Preview voice"
    
    fun formatSpeedLabel(speed: Float): String {
        return "Playback speed: ${(speed * 100).toInt()} percent"
    }
    
    fun formatProgressLabel(current: Duration, total: Duration): String {
        return "Progress: ${formatDuration(current)} of ${formatDuration(total)}"
    }
    
    fun formatVoiceLabel(name: String, language: String, gender: String): String {
        return "$name, $language, $gender voice"
    }
}

/**
 * Keyboard shortcuts help dialog content
 */
@Composable
fun TTSKeyboardShortcutsHelp(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        getTTSKeyboardShortcuts().forEach { shortcut ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                androidx.compose.material3.Text(
                    text = shortcut.keys,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                androidx.compose.material3.Text(
                    text = shortcut.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(2f)
                )
            }
        }
    }
}
