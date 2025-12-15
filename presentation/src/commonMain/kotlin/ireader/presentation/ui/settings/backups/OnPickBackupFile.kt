package ireader.presentation.ui.settings.backups

import androidx.compose.runtime.Composable
import ireader.domain.models.common.Uri

/**
 * Platform-specific composable for picking backup files to restore (.gz, .json)
 * 
 * @param show Whether to show the file picker
 * @param onFileSelected Callback when a file is selected (or null if cancelled)
 */
@Composable
expect fun OnPickBackupFile(
    show: Boolean,
    onFileSelected: (Uri?) -> Unit
)

/**
 * Platform-specific composable for saving backup files (.gz)
 * 
 * @param show Whether to show the file picker
 * @param defaultFileName Default file name for the backup
 * @param onLocationSelected Callback when a location is selected (or null if cancelled)
 */
@Composable
expect fun OnSaveBackupFile(
    show: Boolean,
    defaultFileName: String,
    onLocationSelected: (Uri?) -> Unit
)
