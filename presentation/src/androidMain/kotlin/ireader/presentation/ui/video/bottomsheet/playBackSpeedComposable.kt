package ireader.presentation.ui.video.bottomsheet

import androidx.compose.foundation.lazy.LazyListScope
import ireader.presentation.ui.component.components.ChoicePreference
import ireader.presentation.ui.video.component.core.PlayerState


internal fun LazyListScope.playBackSpeedComposable(playerState: PlayerState, onValue: (Float) -> Unit) {
    item {
        ChoicePreference<Float>(preference = playerState.playbackSpeed, choices = mapOf(
                .5f to "0.5x",
                .75f to "0.75x",
                1f to "1x",
                1.25f to "1.25x",
                1.50f to "1.5x",
                1.75f to "1.75x",
                2f to "2x",
        ), title = "Play BackSpeed", onValue = onValue)
    }
}