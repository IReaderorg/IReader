package ireader.presentation.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import io.ktor.http.encodeURLPathPart
import ireader.presentation.core.NavigationRoutes.repositoryAdd
import ireader.presentation.core.ui.BookDetailScreenSpec
import ireader.presentation.core.ui.ChatGptLoginScreenSpec
import ireader.presentation.core.ui.DeepSeekLoginScreenSpec
import ireader.presentation.core.ui.ExploreScreenSpec
import ireader.presentation.core.ui.GlobalSearchScreenSpec
import ireader.presentation.core.ui.ReaderScreenSpec
import ireader.presentation.core.ui.RepositoryAddScreenSpec

/**
 * Reusable navigation options to avoid lambda allocation on every navigation call.
 * These are pre-built NavOptionsBuilder configurations for common patterns.
 */
@Stable
object NavOptions {
    /** Standard single-top navigation without back stack clearing */
    val singleTop: NavOptionsBuilder.() -> Unit = {
        launchSingleTop = true
    }
    
    /** Navigation that clears the entire back stack */
    val clearAll: NavOptionsBuilder.() -> Unit = {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
    
    /** Navigation to main screen, clearing intermediate screens */
    val toMain: NavOptionsBuilder.() -> Unit = {
        popUpTo(NavigationRoutes.MAIN) { inclusive = false }
        launchSingleTop = true
    }
}

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
 * Optimized to use graph-based popUpTo instead of iterative popping.
 */
fun NavHostController.popUntilRoot() {
    graph.startDestinationRoute?.let { startRoute ->
        popBackStack(startRoute, inclusive = false)
    }
}

/**
 * Check if navigation to a route would be redundant (already at that destination).
 * Prevents unnecessary navigation and recomposition.
 */
fun NavHostController.isCurrentRoute(route: String): Boolean {
    return currentBackStackEntry?.destination?.route == route
}

/**
 * Safe navigation that checks if already at destination before navigating.
 * Returns true if navigation was performed, false if already at destination.
 */
inline fun NavHostController.navigateIfNotCurrent(
    route: String,
    noinline builder: NavOptionsBuilder.() -> Unit = NavOptions.singleTop
): Boolean {
    if (isCurrentRoute(route)) return false
    navigate(route, builder)
    return true
}


/**
 * Navigation wrapper functions that convert screen spec objects to string routes.
 * Use navigateTo() instead of navigate() to avoid serialization issues.
 */

/**
 * Optimized navigation with launchSingleTop and popUpTo to prevent duplicate destinations,
 * reduce recomposition overhead, and manage back stack efficiently.
 * 
 * Uses pre-built NavOptions where possible to reduce lambda allocations.
 */
fun NavHostController.navigateTo(spec: BookDetailScreenSpec) {
    val route = NavigationRoutes.bookDetail(spec.bookId)
    // Skip if already viewing this book
    if (isCurrentRoute(route)) return
    navigate(route, NavOptions.singleTop)
}

fun NavHostController.navigateTo(spec: ReaderScreenSpec) {
    val route = NavigationRoutes.reader(spec.bookId, spec.chapterId)
    // Skip if already reading this chapter
    if (isCurrentRoute(route)) return
    navigate(route) {
        launchSingleTop = true
        // Clear any existing reader screens to prevent deep reader stack
        // Use base route name for popUpTo (parameterized routes don't match)
        popUpTo(NavigationRoutes.READER_BASE) { inclusive = true }
    }
}

fun NavHostController.navigateTo(spec: ExploreScreenSpec) {
    val route = NavigationRoutes.explore(spec.sourceId)
    if (isCurrentRoute(route)) return
    navigate(route) {
        launchSingleTop = true
        popUpTo(NavigationRoutes.EXPLORE_BASE) { inclusive = true }
    }
}

fun NavHostController.navigateTo(spec: RepositoryAddScreenSpec) {
    if (isCurrentRoute(repositoryAdd)) return
    navigate(repositoryAdd, NavOptions.singleTop)
}

fun NavHostController.navigateTo(spec: GlobalSearchScreenSpec) {
    val query = spec.query?.let { "?query=${it.encodeURLPathPart()}" } ?: ""
    val route = "${NavigationRoutes.GLOBAL_SEARCH}$query"
    navigate(route) {
        launchSingleTop = true
        popUpTo(NavigationRoutes.GLOBAL_SEARCH) { inclusive = true }
    }
}

fun NavHostController.navigateTo(spec: ChatGptLoginScreenSpec) {
    if (isCurrentRoute("chatGptLogin")) return
    navigate("chatGptLogin") {
        launchSingleTop = true
        popUpTo("chatGptLogin") { inclusive = true }
    }
}

fun NavHostController.navigateTo(spec: DeepSeekLoginScreenSpec) {
    if (isCurrentRoute("deepSeekLogin")) return
    navigate("deepSeekLogin") {
        launchSingleTop = true
        popUpTo("deepSeekLogin") { inclusive = true }
    }
}

fun NavHostController.navigateTo(spec: ireader.presentation.ui.home.sources.extension.SourceDetailScreen) {
    val route = NavigationRoutes.sourceDetail(spec.catalog.sourceId)
    if (isCurrentRoute(route)) return
    navigate(route) {
        launchSingleTop = true
        // Use base route name for popUpTo (parameterized routes don't match)
        popUpTo(NavigationRoutes.SOURCE_DETAIL_BASE) { inclusive = true }
    }
}

fun NavHostController.navigateTo(spec: ireader.presentation.core.ui.SourceMigrationScreenSpec) {
    val route = "sourceMigration/${spec.sourceId}"
    if (isCurrentRoute(route)) return
    navigate(route) {
        launchSingleTop = true
        // Use base route name for popUpTo (parameterized routes don't match)
        popUpTo(NavigationRoutes.SOURCE_MIGRATION_BASE) { inclusive = true }
    }
}

fun NavHostController.navigateTo(spec: ireader.presentation.core.ui.BrowseSettingsScreenSpec) {
    if (isCurrentRoute(NavigationRoutes.browseSettings)) return
    navigate(NavigationRoutes.browseSettings, NavOptions.singleTop)
}

fun NavHostController.navigateTo(spec: ireader.presentation.core.ui.PluginDetailsScreenSpec) {
    val route = NavigationRoutes.pluginDetails(spec.pluginId)
    if (isCurrentRoute(route)) return
    navigate(route) {
        launchSingleTop = true
        popUpTo(NavigationRoutes.PLUGIN_DETAILS_BASE) { inclusive = true }
    }
}

fun NavHostController.navigateTo(spec: ireader.presentation.core.ui.UserSourcesListScreenSpec) {
    if (isCurrentRoute(NavigationRoutes.userSources)) return
    navigate(NavigationRoutes.userSources, NavOptions.singleTop)
}

// Expect functions for platform-specific screen specs
expect fun NavHostController.navigateTo(spec: ireader.presentation.core.ui.WebViewScreenSpec)

// TTSV2ScreenSpec navigation
fun NavHostController.navigateTo(spec: ireader.presentation.core.ui.TTSV2ScreenSpec) {
    val route = NavigationRoutes.ttsV2(spec.bookId, spec.chapterId, spec.sourceId, spec.readingParagraph)
    if (isCurrentRoute(route)) return
    navigate(route) {
        launchSingleTop = true
        // Use base route name for popUpTo (parameterized routes don't match)
        popUpTo(NavigationRoutes.TTS_V2_BASE) { inclusive = true }
    }
}

/**
 * Navigation convenience functions for common patterns.
 * Uses pre-built NavOptions to minimize allocations.
 */

/** Navigate and clear entire back stack. Use for logout/reset. */
fun NavHostController.navigateAndClearAll(route: String) {
    navigate(route, NavOptions.clearAll)
}

/** Navigate to main screen, clearing intermediate screens. */
fun NavHostController.navigateToMain() {
    if (isCurrentRoute(NavigationRoutes.MAIN)) return
    navigate(NavigationRoutes.MAIN, NavOptions.toMain)
}

/** Navigate to reader, clearing other reader sessions. */
fun NavHostController.navigateToReaderAndClearOthers(bookId: Long, chapterId: Long) {
    val route = NavigationRoutes.reader(bookId, chapterId)
    if (isCurrentRoute(route)) return
    navigate(route) {
        // Use base route name for popUpTo (parameterized routes don't match)
        popUpTo(NavigationRoutes.READER_BASE) { inclusive = true }
        launchSingleTop = true
    }
}

/** Navigate to settings screen. */
fun NavHostController.navigateToSettings(settingsRoute: String) {
    if (isCurrentRoute(settingsRoute)) return
    navigate(settingsRoute, NavOptions.singleTop)
}

/**
 * Batch navigation helper - navigate to multiple screens efficiently.
 * Useful for deep linking scenarios.
 */
fun NavHostController.navigateDeepLink(vararg routes: String) {
    routes.forEachIndexed { index, route ->
        navigate(route) {
            if (index == routes.lastIndex) {
                launchSingleTop = true
            }
        }
    }
}

/**
 * Navigate back with result - pops back stack and can trigger result handling.
 * Returns true if pop was successful.
 */
fun NavHostController.navigateBack(): Boolean {
    return if (previousBackStackEntry != null) {
        popBackStack()
    } else {
        false
    }
}

/**
 * Navigate back to a specific route, clearing everything above it.
 */
fun NavHostController.navigateBackTo(route: String, inclusive: Boolean = false): Boolean {
    return popBackStack(route, inclusive)
}
