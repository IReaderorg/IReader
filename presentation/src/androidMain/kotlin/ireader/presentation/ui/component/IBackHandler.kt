package ireader.presentation.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun IBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(
            enabled = enabled,
            onBack = onBack,
    )
}