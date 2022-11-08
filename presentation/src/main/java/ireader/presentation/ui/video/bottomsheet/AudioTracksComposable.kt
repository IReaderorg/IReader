package ireader.presentation.ui.video.bottomsheet

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.rememberCoroutineScope
import ireader.core.log.Log
import ireader.presentation.ui.component.components.component.ChoicePreference
import ireader.presentation.ui.video.component.core.MediaState
import ireader.presentation.ui.video.component.core.PlayerState

internal fun LazyListScope.audioTracksComposable(playerState: PlayerState, mediaState: MediaState?, onValue: (String?) -> Unit) {
    item {
        val scope = rememberCoroutineScope()
        ChoicePreference<String?>(preference = playerState.selectedAudioTrack, choices =
        (mediaState?.playerState?.currentTracks?.allAudioTracks
                ?: emptyList()).associate {
            (it.label) to (it.label ?: "Audio Track: ${it.language}")
        } ?: emptyMap(), title = "Audio Tracks", onValue = {
            Log.error { "PlayerAudioSelected: " + playerState.selectedAudioTrack.toString() }
            onValue(it)

        })
    }
}