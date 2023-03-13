package ireader.presentation.ui.core.modifier

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.ui.Modifier

actual fun Modifier.systemGestureExclusion(): Modifier = this.systemGestureExclusion()
actual fun Modifier.navigationBarsPadding(): Modifier = this.navigationBarsPadding()
actual fun Modifier.systemBarsPadding(): Modifier = this.systemBarsPadding()