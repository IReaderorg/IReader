package ireader.presentation.ui.video.bottomsheet

import androidx.compose.foundation.lazy.LazyListScope
import ireader.presentation.ui.component.components.ChoicePreference
import ireader.presentation.ui.video.component.core.PlayerState
import ireader.presentation.ui.video.component.cores.SubtitleData


internal fun LazyListScope.subtitleSelectorComposable(playerState: PlayerState, subtitles: List<SubtitleData>, onValue: (SubtitleData?) -> Unit) {
    item {
        ChoicePreference<SubtitleData?>(preference = playerState.currentSubtitle, choices =
        (subtitles ?: emptyList()).associate {
            (it) to (it.name ?: "Unknown")
        }, title = "Subtitles", onValue = { subtitleData ->
            onValue(subtitleData)

        })
    }
}