package ireader.presentation.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController
import ireader.presentation.core.ui.BookDetailScreenSpec
import ireader.presentation.core.ui.ChatGptLoginScreenSpec
import ireader.presentation.core.ui.DeepSeekLoginScreenSpec
import ireader.presentation.core.ui.ExploreScreenSpec
import ireader.presentation.core.ui.GlobalSearchScreenSpec
import ireader.presentation.core.ui.ReaderScreenSpec

/**
 * CompositionLocal that provides access to the NavHostController throughout the app.
 * 
 * This allows any composable in the hierarchy to access the navigation controller
 * without explicitly passing it through the composable tree.
 * 
 * @throws IllegalStateException if accessed before being provided
 */
val LocalNavigator: ProvidableCompositionLocal<NavHostController?> = compositionLocalOf {
    null
}

/**
 * Safe accessor for LocalNavigator that throws if not provided.
 * 
 * This provides a convenient way to access the navigator without dealing with nullability.
 * 
 * Usage: val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
 * 
 * @throws IllegalStateException if LocalNavigator is not provided in the composition
 */
val ProvidableCompositionLocal<NavHostController?>.currentOrThrow: NavHostController
    @Composable
    get() = this.current ?: error("No NavController provided. Make sure to wrap your content with ProvideNavigator.")

/**
 * Provides the NavHostController to the composition tree via LocalNavigator.
 * 
 * This composable should wrap the NavHost and any content that needs access
 * to the navigation controller.
 * 
 * @param navController The NavHostController to provide
 * @param content The composable content that will have access to the navigator
 */
@Composable
fun ProvideNavigator(
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalNavigator provides navController) {
        content()
    }
}

/**
 * Extension function to pop the back stack until only the root destination remains.
 * 
 * This is useful for "pop to root" navigation patterns, such as when navigating
 * from a deep link back to the main screen.
 */
fun NavHostController.popUntilRoot() {
    // Keep popping until we reach the start destination
    // Use currentBackStackEntry to check if we can pop
    while (previousBackStackEntry != null) {
        if (!popBackStack()) {
            break
        }
    }
}


/**
 * Navigation wrapper functions that convert screen spec objects to string routes.
 * Use navigateTo() instead of navigate() to avoid serialization issues.
 */

fun NavHostController.navigateTo(spec: BookDetailScreenSpec) {
    navigate(NavigationRoutes.bookDetail(spec.bookId))
}

fun NavHostController.navigateTo(spec: ReaderScreenSpec) {
    navigate("reader/${spec.bookId}/${spec.chapterId}")
}

fun NavHostController.navigateTo(spec: ExploreScreenSpec) {
    // Don't include query in route since it's not part of the path pattern
    navigate("explore/${spec.sourceId}")
}

fun NavHostController.navigateTo(spec: GlobalSearchScreenSpec) {
    // Navigate without query parameter - the screen will handle it internally
    navigate("globalSearch")
}

fun NavHostController.navigateTo(spec: ChatGptLoginScreenSpec) {
    navigate("chatGptLogin")
}

fun NavHostController.navigateTo(spec: DeepSeekLoginScreenSpec) {
    navigate("deepSeekLogin")
}

fun NavHostController.navigateTo(spec: ireader.presentation.ui.home.sources.extension.SourceDetailScreen) {
    navigate("sourceDetail/${spec.catalog.sourceId}")
}

fun NavHostController.navigateTo(spec: ireader.presentation.core.ui.SourceMigrationScreenSpec) {
    navigate("sourceMigration/${spec.sourceId}")
}

// Expect functions for platform-specific screen specs
expect fun NavHostController.navigateTo(spec: ireader.presentation.core.ui.WebViewScreenSpec)
expect fun NavHostController.navigateTo(spec: ireader.presentation.core.ui.TTSScreenSpec)
