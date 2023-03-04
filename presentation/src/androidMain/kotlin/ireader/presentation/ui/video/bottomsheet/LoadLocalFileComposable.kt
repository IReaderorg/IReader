package ireader.presentation.ui.video.bottomsheet

import androidx.compose.foundation.lazy.LazyListScope
import ireader.presentation.ui.component.components.PreferenceRow

class LoadLocalFileComposable {
}
internal fun LazyListScope.loadLocalFileComposable(title: String, onClick: () -> Unit) {
    item {
        PreferenceRow(title = title, onClick = {
            onClick()
        })
    }
}