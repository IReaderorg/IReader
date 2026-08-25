package ireader.presentation.core.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

/**
 * Get a ViewModel instance from Koin, scoped to the current NavBackStackEntry.
 *
 * The instance lives in the destination's ViewModelStore: navigating forward
 * (detail/search/webview) keeps it alive, so screen state — loaded books,
 * query, filters, scroll position — survives the round trip. It is cleared
 * only when the screen itself is popped.
 */
@Composable
inline fun <reified T : ViewModel> getIViewModel(
    key: String? = null,
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): T {
    return koinViewModel(key = key, qualifier = qualifier, parameters = parameters)
}
