package ireader.presentation.ui.plugins.integration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import ireader.domain.plugins.ReaderContext
import kotlinx.coroutines.CoroutineScope

/**
 * Handler for reader context events that notifies feature plugins
 * Requirements: 6.3
 */
class ReaderContextHandler(
    private val featurePluginIntegration: FeaturePluginIntegration
) {
    
    /**
     * Notify plugins of text selection
     * 
     * @param bookId Current book ID
     * @param chapterId Current chapter ID
     * @param selectedText Selected text
     * @param currentPosition Current reading position
     * @param scope Coroutine scope
     * @param navController Navigation controller
     */
    fun onTextSelection(
        bookId: Long,
        chapterId: Long,
        selectedText: String,
        currentPosition: Int,
        scope: CoroutineScope,
        navController: NavHostController
    ) {
        val context = ReaderContext(
            bookId = bookId,
            chapterId = chapterId,
            selectedText = selectedText,
            currentPosition = currentPosition
        )
        
        featurePluginIntegration.handleReaderContext(context, scope, navController)
    }
    
    /**
     * Notify plugins of chapter change
     * 
     * @param bookId Current book ID
     * @param chapterId New chapter ID
     * @param currentPosition Current reading position
     * @param scope Coroutine scope
     * @param navController Navigation controller
     */
    fun onChapterChange(
        bookId: Long,
        chapterId: Long,
        currentPosition: Int,
        scope: CoroutineScope,
        navController: NavHostController
    ) {
        val context = ReaderContext(
            bookId = bookId,
            chapterId = chapterId,
            selectedText = null,
            currentPosition = currentPosition
        )
        
        featurePluginIntegration.handleReaderContext(context, scope, navController)
    }
    
    /**
     * Notify plugins of bookmark creation
     * 
     * @param bookId Current book ID
     * @param chapterId Current chapter ID
     * @param currentPosition Bookmark position
     * @param scope Coroutine scope
     * @param navController Navigation controller
     */
    fun onBookmark(
        bookId: Long,
        chapterId: Long,
        currentPosition: Int,
        scope: CoroutineScope,
        navController: NavHostController
    ) {
        val context = ReaderContext(
            bookId = bookId,
            chapterId = chapterId,
            selectedText = null,
            currentPosition = currentPosition
        )
        
        featurePluginIntegration.handleReaderContext(context, scope, navController)
    }
    
    /**
     * Notify plugins of position change
     * 
     * @param bookId Current book ID
     * @param chapterId Current chapter ID
     * @param currentPosition New reading position
     * @param scope Coroutine scope
     * @param navController Navigation controller
     */
    fun onPositionChange(
        bookId: Long,
        chapterId: Long,
        currentPosition: Int,
        scope: CoroutineScope,
        navController: NavHostController
    ) {
        val context = ReaderContext(
            bookId = bookId,
            chapterId = chapterId,
            selectedText = null,
            currentPosition = currentPosition
        )
        
        featurePluginIntegration.handleReaderContext(context, scope, navController)
    }
}

/**
 * Composable effect for handling reader context changes
 * Requirements: 6.3
 */
@Composable
fun ReaderContextEffect(
    bookId: Long,
    chapterId: Long,
    currentPosition: Int,
    selectedText: String?,
    contextHandler: ReaderContextHandler,
    scope: CoroutineScope,
    navController: NavHostController
) {
    // Create a stable key for the effect
    val contextKey = remember(bookId, chapterId, currentPosition, selectedText) {
        "$bookId-$chapterId-$currentPosition-${selectedText?.hashCode()}"
    }
    
    LaunchedEffect(contextKey) {
        if (selectedText != null) {
            contextHandler.onTextSelection(
                bookId = bookId,
                chapterId = chapterId,
                selectedText = selectedText,
                currentPosition = currentPosition,
                scope = scope,
                navController = navController
            )
        } else {
            contextHandler.onPositionChange(
                bookId = bookId,
                chapterId = chapterId,
                currentPosition = currentPosition,
                scope = scope,
                navController = navController
            )
        }
    }
}
