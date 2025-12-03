package ireader.presentation.ui.component

import androidx.compose.runtime.Composable

@Composable
expect fun IBackHandler(enabled:Boolean, onBack:() -> Unit)
