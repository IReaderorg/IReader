package ireader.presentation.ui.video.bottomsheet

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.platform.LocalContext
import ireader.presentation.ui.component.components.component.PreferenceRow

class LoadLocalFileComposable {
}
internal fun LazyListScope.loadLocalFileComposable(title: String, onClick: () -> Unit) {
    item {
        val context = LocalContext.current
        PreferenceRow(title = title, onClick = {
            onClick()
        })
    }
}