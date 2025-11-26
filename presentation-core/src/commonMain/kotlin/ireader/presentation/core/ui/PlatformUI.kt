package ireader.presentation.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ireader.domain.models.common.Uri

/**
 * Unified platform UI abstraction layer for cross-platform UI components
 */

/**
 * Platform-specific file picker interface
 */
interface PlatformFilePicker {
    /**
     * Pick a single file
     * @param mimeTypes List of MIME types or file extensions to filter
     * @param initialDirectory Optional initial directory path
     * @return Result containing the selected file URI or null if cancelled
     */
    suspend fun pickFile(
        mimeTypes: List<String> = emptyList(),
        initialDirectory: String? = null
    ): Result<Uri?>

    /**
     * Pick multiple files
     * @param mimeTypes List of MIME types or file extensions to filter
     * @param initialDirectory Optional initial directory path
     * @return Result containing list of selected file URIs or null if cancelled
     */
    suspend fun pickFiles(
        mimeTypes: List<String> = emptyList(),
        initialDirectory: String? = null
    ): Result<List<Uri>?>

    /**
     * Pick a directory
     * @param initialDirectory Optional initial directory path
     * @return Result containing the selected directory path or null if cancelled
     */
    suspend fun pickDirectory(
        initialDirectory: String? = null
    ): Result<String?>
}

/**
 * Platform-specific back handler interface
 */
interface PlatformBackHandler {
    /**
     * Handle back press events
     * @param enabled Whether the handler is enabled
     * @param onBack Callback when back is pressed
     */
    @Composable
    fun Handle(enabled: Boolean, onBack: () -> Unit)
}

/**
 * Platform-specific scrollbar interface
 */
interface PlatformScrollbar {
    /**
     * Show scrollbar for scrollable content
     * @param modifier Modifier for the scrollbar
     * @param rightSide Whether to show on right side (true) or left side (false)
     * @param content The scrollable content
     */
    @Composable
    fun Vertical(
        modifier: Modifier = Modifier,
        rightSide: Boolean = true,
        content: @Composable () -> Unit
    )
}

/**
 * Get platform-specific file picker implementation
 */
expect fun getPlatformFilePicker(): PlatformFilePicker

/**
 * Get platform-specific back handler implementation
 */
expect fun getPlatformBackHandler(): PlatformBackHandler

/**
 * Get platform-specific scrollbar implementation
 */
expect fun getPlatformScrollbar(): PlatformScrollbar
