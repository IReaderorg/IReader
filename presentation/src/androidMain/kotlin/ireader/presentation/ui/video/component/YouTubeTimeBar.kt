package ireader.presentation.ui.video.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.video.component.core.TimeBar
import ireader.presentation.ui.video.component.core.TimeBarProgress
import ireader.presentation.ui.video.component.core.TimeBarScrubber


@Composable
fun YouTubeTimeBar() {
    var position by remember { mutableStateOf(60L) }
    val bufferedPosition by remember { derivedStateOf { (position + 10).coerceAtMost(100) } }
    TimeBar(
        durationMs = 100,
        positionMs = position,
        bufferedPositionMs = bufferedPosition,
        modifier = Modifier
            .systemGestureExclusion()
            .fillMaxWidth()
            .height(50.dp),
        contentPadding = PaddingValues(vertical = 24.dp),
        onScrubStop = { position = it },
        progress = { current, _, buffered ->
            // YouTube use current progress as played progress
            TimeBarProgress(current, buffered, playedColor = Color.Red)
        }
    ) { enabled, scrubbing ->
        TimeBarScrubber(enabled, scrubbing, draggedSize = 20.dp, color = Color.Red)
    }
}