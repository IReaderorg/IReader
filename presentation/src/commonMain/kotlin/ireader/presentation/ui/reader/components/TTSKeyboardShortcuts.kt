package ireader.presentation.ui.reader.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.*

/**
 * Keyboard shortcuts for TTS controls
 * Requirements: 11.1, 11.2
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TTSKeyboardShortcuts(
    onPlayPause: () -> Unit,
    onSkipForward: () -> Unit,
    onSkipBackward: () -> Unit,
    onIncreaseSpeed: () -> Unit,
    onDecreaseSpeed: () -> Unit,
    onStop: () -> Unit
) {
    DisposableEffect(Unit) {
        val keyEventHandler = object : KeyEventHandler {
            override fun onKeyEvent(event: KeyEvent): Boolean {
                if (event.type != KeyEventType.KeyDown) {
                    return false
                }
                
                return when {
                    // Space: Play/Pause
                    event.key == Key.Spacebar && !event.isCtrlPressed && !event.isAltPressed -> {
                        onPlayPause()
                        true
                    }
                    
                    // K: Play/Pause (YouTube-style)
                    event.key == Key.K && !event.isCtrlPressed && !event.isAltPressed -> {
                        onPlayPause()
                        true
                    }
                    
                    // Right Arrow or L: Skip forward
                    (event.key == Key.DirectionRight || event.key == Key.L) && !event.isCtrlPressed -> {
                        onSkipForward()
                        true
                    }
                    
                    // Left Arrow or J: Skip backward
                    (event.key == Key.DirectionLeft || event.key == Key.J) && !event.isCtrlPressed -> {
                        onSkipBackward()
                        true
                    }
                    
                    // Shift + >: Increase speed
                    event.key == Key.Period && event.isShiftPressed -> {
                        onIncreaseSpeed()
                        true
                    }
                    
                    // Shift + <: Decrease speed
                    event.key == Key.Comma && event.isShiftPressed -> {
                        onDecreaseSpeed()
                        true
                    }
                    
                    // Escape: Stop
                    event.key == Key.Escape -> {
                        onStop()
                        true
                    }
                    
                    else -> false
                }
            }
        }
        
        // Note: Actual registration would depend on platform-specific implementation
        // This is a placeholder for the keyboard event handling structure
        
        onDispose {
            // Cleanup
        }
    }
}

/**
 * Interface for handling key events
 */
interface KeyEventHandler {
    fun onKeyEvent(event: KeyEvent): Boolean
}

/**
 * Get keyboard shortcuts help text
 */
fun getTTSKeyboardShortcuts(): List<KeyboardShortcut> {
    return listOf(
        KeyboardShortcut("Space or K", "Play/Pause"),
        KeyboardShortcut("→ or L", "Skip forward 10 seconds"),
        KeyboardShortcut("← or J", "Skip backward 10 seconds"),
        KeyboardShortcut("Shift + >", "Increase playback speed"),
        KeyboardShortcut("Shift + <", "Decrease playback speed"),
        KeyboardShortcut("Esc", "Stop playback")
    )
}

/**
 * Data class for keyboard shortcut information
 */
data class KeyboardShortcut(
    val keys: String,
    val description: String
)
