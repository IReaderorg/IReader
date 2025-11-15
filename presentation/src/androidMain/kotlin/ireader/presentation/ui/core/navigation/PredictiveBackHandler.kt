package ireader.presentation.ui.core.navigation

import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

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

    DisposableEffect(enabled) {
        val callback = object : OnBackPressedCallback(enabled) {
            override fun handleOnBackPressed() {
                onBack()
            }
        }

        backDispatcher?.addCallback(callback)

        onDispose {
            callback.remove()
        }
    }
}
