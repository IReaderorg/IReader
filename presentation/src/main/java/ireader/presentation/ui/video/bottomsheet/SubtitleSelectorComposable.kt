package ireader.presentation.ui.video.bottomsheet

import androidx.compose.foundation.lazy.LazyListScope
import ireader.presentation.ui.component.components.component.ChoicePreference
import ireader.presentation.ui.video.component.core.MediaState
import ireader.presentation.ui.video.component.core.PlayerState
import ireader.presentation.ui.video.component.cores.SubtitleData


internal fun LazyListScope.subtitleSelectorComposable(playerState: PlayerState,mediaState: MediaState?, onValue: (SubtitleData?) -> Unit) {
    item {
        ChoicePreference<SubtitleData?>(preference = playerState.currentSubtitle, choices =
        (playerState.currentSubtitles ?: emptyList()).associate {
            (it) to (it.name ?: "Unknown")
        }, title = "Subtitles", onValue = { subtitleData ->
            onValue(subtitleData)

        })
    }
}