package ireader.presentation.ui.core.navigation

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState

/**
 * Predictive back gesture handler for Android.
 * 
 * This provides smooth back navigation with proper cleanup
 * and supports Android's predictive back gesture feature.
 */
@Composable
fun PredictiveBackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit
) {
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    // Use rememberUpdatedState to safely reference lambda in DisposableEffect
    val currentOnBack = rememberUpdatedState(onBack)

    DisposableEffect(enabled, backDispatcher) {
        val callback = object : OnBackPressedCallback(enabled) {
            override fun handleOnBackPressed() {
                currentOnBack.value()
            }
        }

        backDispatcher?.addCallback(callback)

        onDispose {
            callback.remove()
        }
    }
}
