package ireader.presentation.ui.tts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.i18n.localize
import ireader.i18n.resources.*
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

/**
 * Compact Spotify-like TTS player with 5 buttons
 * Replaces the large TTSMediaControls component
 * 
 * Buttons (left to right):
 * 1. Previous chapter
 * 2. Previous paragraph
 * 3. Play/Pause
 * 4. Next paragraph
 * 5. Next chapter
 * 6. Settings (opens drawer)
 */
@Composable
fun SpotifyLikeTTSPlayer(
    isPlaying: Boolean,
    onPreviousChapter: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onNextChapter: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous chapter button
            IconButton(onClick = onPreviousChapter) {
                Icon(
                    Icons.Default.FastRewind,
                    contentDescription = localizeHelper.localize(Res.string.previous_chapter),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Previous paragraph button
            IconButton(onClick = onPrevious) {
                Icon(
                    Icons.Default.SkipPrevious,
                    contentDescription = localizeHelper.localize(Res.string.previous_paragraph),
                    modifier = Modifier.size(28.dp)
                )
            }
            
            // Play/Pause button (larger, primary)
            IconButton(onClick = onPlayPause) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) 
                        localizeHelper.localize(Res.string.pause) 
                    else 
                        localizeHelper.localize(Res.string.play),
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // Next paragraph button
            IconButton(onClick = onNext) {
                Icon(
                    Icons.Default.SkipNext,
                    contentDescription = localizeHelper.localize(Res.string.next_paragraph),
                    modifier = Modifier.size(28.dp)
                )
            }
            
            // Next chapter button
            IconButton(onClick = onNextChapter) {
                Icon(
                    Icons.Default.FastForward,
                    contentDescription = localizeHelper.localize(Res.string.next_chapter),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Settings button
            IconButton(onClick = onSettings) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = localizeHelper.localize(Res.string.tts_settings),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
