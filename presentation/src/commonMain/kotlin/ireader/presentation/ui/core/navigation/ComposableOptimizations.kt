package ireader.presentation.ui.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

/**
 * Optimization utilities for composable recomposition.
 * 
 * These utilities help prevent unnecessary recompositions during navigation
 * and improve overall performance.
 */

/**
 * Marker annotation for stable state classes.
 * 
 * Use this annotation on data classes that are used as state in composables
 * to indicate to the Compose compiler that the class is stable and won't
 * change unexpectedly.
 * 
 * Example:
 * ```
 * @Stable
 * data class ScreenState(
 *     val items: List<Item>,
 *     val isLoading: Boolean
 * )
 * ```
 */
@Stable
annotation class StableState

/**
 * Helper function to create derived state with proper memoization.
 * 
 * Use this when you need to compute expensive values from state that
 * should only be recalculated when dependencies change.
 * 
 * Example:
 * ```
 * val filteredItems = rememberDerivedState(items, filter) {
 *     items.filter { it.matches(filter) }
 * }
 * ```
 */
@Composable
fun <T, R> rememberDerivedState(
    vararg keys: Any?,
    calculation: () -> R
): R {
    val derived by remember(*keys) {
        derivedStateOf(calculation)
    }
    return derived
}

/**
 * Helper function to create stable keys for LazyColumn/LazyRow items.
 * 
 * Use this to generate stable keys that help Compose optimize list rendering
 * and prevent unnecessary recompositions.
 * 
 * Example:
 * ```
 * LazyColumn {
 *     items(
 *         items = books,
 *         key = { stableKey(it.id) }
 *     ) { book ->
 *         BookCard(book)
 *     }
 * }
 * ```
 */
fun stableKey(vararg parts: Any?): String {
    return parts.joinToString(separator = "_") { it?.toString() ?: "null" }
}
