package ireader.presentation.ui.video.bottomsheet

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.platform.LocalContext
import ireader.presentation.ui.component.components.component.PreferenceRow
import ireader.presentation.ui.video.component.core.PlayerState

class LoadLocalFileComposable {
}
internal fun LazyListScope.loadLocalFileComposable(playerState: PlayerState, onClick: () -> Unit) {
    item {
        val context = LocalContext.current
        PreferenceRow(title = "load local file", onClick = {
            onClick()
        })
    }
}