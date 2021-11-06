package ir.kazemcodes.infinity.presentation.screen

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*


/**
 * Destinations used in the [InfReaderApp].
 */


data class Message(val id: Long, @StringRes val messageId: Int)

/**
 * Class responsible for managing Snackbar messages to show on the screen
 */
object SnackbarManager {

    private val _messages: MutableStateFlow<List<Message>> = MutableStateFlow(emptyList())
    val messages: StateFlow<List<Message>> get() = _messages.asStateFlow()

    fun showMessage(@StringRes messageTextId: Int) {
        _messages.update { currentMessages ->
            currentMessages + Message(
                id = UUID.randomUUID().mostSignificantBits,
                messageId = messageTextId
            )
        }
    }

    fun setMessageShown(messageId: Long) {
        _messages.update { currentMessages ->
            currentMessages.filterNot { it.id == messageId }
        }
    }
}


@Composable
fun rememberInfAppState(
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    navController: NavController = rememberNavController(),
    snakbarManager : SnackbarManager = SnackbarManager,
    resources : Resources,
    coroutineScope: CoroutineScope
)  = remember(
    scaffoldState,
    navController,
    snakbarManager,
    resources,
    coroutineScope
) {
    InfReaderAppState(scaffoldState,navController,snakbarManager,resources,coroutineScope)
}

/**
 * Responsible for holding state related to [InfReaderApp] and containing UI-related logic.
 */
class InfReaderAppState(
    val scaffoldState: ScaffoldState,
    val navController: NavController,
    private val snackbarManager: SnackbarManager,
    private val resources: Resources,
    coroutineScope: CoroutineScope
) {
    // Process snackbars coming from SnackbarManager
    init {
        coroutineScope.launch {
            snackbarManager.messages.collect { currentMessages ->
                if (currentMessages.isNotEmpty()) {
                    val message = currentMessages[0]
                    val text = resources.getText(message.messageId)

                    // Display the snackbar on the screen. `showSnackbar` is a function
                    // that suspends until the snackbar disappears from the screen
                    scaffoldState.snackbarHostState.showSnackbar(text.toString())
                    // Once the snackbar is gone or dismissed, notify the SnackbarManager
                    snackbarManager.setMessageShown(message.id)
                }
            }
        }
    }
    //-------------------------------------------------------

//    val bottomBarTabs = BottomNavItem.values()
//    //list of it routes
//    private val bottomBarRoutes : List<String> = bottomBarTabs.map { it.route }
//
//    val shouldShowBottomBar : Boolean
//        @Composable get() = navController
//            .currentBackStackEntryAsState().value?.destination?.route in bottomBarRoutes


    //-------------------------------------------------------


    val currentRoute: String?
        get() = navController.currentDestination?.route

    fun upPress() {
        navController.navigateUp()
    }
    /**
     * If the lifecycle is not resumed it means this NavBackStackEntry already processed a nav event.
     *
     * This is used to de-duplicate navigation events.
     */
    private fun NavBackStackEntry.lifecycleIsResumed() =
        this.lifecycle.currentState == Lifecycle.State.RESUMED

    private val NavGraph.startDestination: NavDestination?
        get() = findNode(startDestinationId)

    private tailrec fun findStartDestination(graph: NavDestination): NavDestination {
        return if (graph is NavGraph) findStartDestination(graph.startDestination!!) else graph
    }


    fun navigateToBottomBarRoute(route: String) {
        if (route != currentRoute) {
            navController.navigate(route) {
                launchSingleTop = true
                restoreState = true
                // Pop up backstack to the first destination and save state. This makes going back
                // to the start destination when pressing back in any other bottom tab.
                popUpTo(findStartDestination(navController.graph).id) {
                    saveState = true
                }
            }
        }
    }

}

@Composable
@ReadOnlyComposable
private fun resources(): Resources {
    LocalConfiguration.current
    return LocalContext.current.resources
}


