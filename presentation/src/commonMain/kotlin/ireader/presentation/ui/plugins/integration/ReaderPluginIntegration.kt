package ireader.presentation.ui.plugins.integration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import ireader.domain.plugins.ReaderContext
import kotlinx.coroutines.CoroutineScope

/**
 * Composable integration point for feature plugins in the reader screen
 * Requirements: 6.1, 6.2, 6.3
 */
@Composable
fun rememberReaderPluginIntegration(
    featurePluginIntegration: FeaturePluginIntegration,
    bookId: Long,
    chapterId: Long,
    currentPosition: Int,
    selectedText: String?,
    navController: NavHostController,
    scope: CoroutineScope
): ReaderPluginState {
    
    // Get menu items
    val menuItems = remember(featurePluginIntegration) {
        featurePluginIntegration.getPluginMenuItems()
    }
    
    // Create context handler
    val contextHandler = remember(featurePluginIntegration) {
        ReaderContextHandler(featurePluginIntegration)
    }
    
    // Handle context changes
    LaunchedEffect(bookId, chapterId, currentPosition, selectedText) {
        val context = ReaderContext(
            bookId = bookId,
            chapterId = chapterId,
            selectedText = selectedText,
            currentPosition = currentPosition
        )
        
        featurePluginIntegration.handleReaderContext(context, scope, navController)
    }
    
    return ReaderPluginState(
        menuItems = menuItems,
        contextHandler = contextHandler,
        hasPlugins = menuItems.isNotEmpty()
    )
}

/**
 * State for reader plugin integration
 */
data class ReaderPluginState(
    val menuItems: List<ireader.domain.plugins.PluginMenuItem>,
    val contextHandler: ReaderContextHandler,
    val hasPlugins: Boolean
)

/**
 * Helper for notifying plugins of specific reader events
 */
class ReaderPluginEventNotifier(
    private val featurePluginIntegration: FeaturePluginIntegration
) {
    
    /**
     * Notify plugins when text is selected
     */
    fun notifyTextSelection(
        bookId: Long,
        chapterId: Long,
        selectedText: String,
        position: Int,
        scope: CoroutineScope,
        navController: NavHostController
    ) {
        val context = ReaderContext(
            bookId = bookId,
            chapterId = chapterId,
            selectedText = selectedText,
            currentPosition = position
        )
        
        featurePluginIntegration.handleReaderContext(context, scope, navController)
    }
    
    /**
     * Notify plugins when chapter changes
     */
    fun notifyChapterChange(
        bookId: Long,
        newChapterId: Long,
        position: Int,
        scope: CoroutineScope,
        navController: NavHostController
    ) {
        val context = ReaderContext(
            bookId = bookId,
            chapterId = newChapterId,
            selectedText = null,
            currentPosition = position
        )
        
        featurePluginIntegration.handleReaderContext(context, scope, navController)
    }
    
    /**
     * Notify plugins when a bookmark is created
     */
    fun notifyBookmarkCreated(
        bookId: Long,
        chapterId: Long,
        position: Int,
        scope: CoroutineScope,
        navController: NavHostController
    ) {
        val context = ReaderContext(
            bookId = bookId,
            chapterId = chapterId,
            selectedText = null,
            currentPosition = position
        )
        
        featurePluginIntegration.handleReaderContext(context, scope, navController)
    }
}
