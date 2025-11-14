package ireader.presentation.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.compose.getKoin
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

/**
 * Get a ViewModel instance from Koin with optional parameters.
 * This replaces Voyager's getIViewModel() function.
 * The ViewModel is remembered to prevent recreation on recomposition.
 */
@Composable
public inline fun <reified T : Any> getIViewModel(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): T {
    val koin = getKoin()
    return remember(qualifier, parameters) {
        koin.get(qualifier, parameters)
    }
}


