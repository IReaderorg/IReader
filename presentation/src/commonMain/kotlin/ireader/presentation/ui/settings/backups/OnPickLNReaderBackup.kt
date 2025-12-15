package ireader.presentation.ui.settings.backups

import androidx.compose.runtime.Composable
import ireader.domain.models.common.Uri

/**
 * Platform-specific composable for picking LNReader backup files (.zip)
 * 
 * @param show Whether to show the file picker
 * @param onFileSelected Callback when a file is selected (or null if cancelled)
 */
@Composable
expect fun OnPickLNReaderBackup(
    show: Boolean,
    onFileSelected: (Uri?) -> Unit
)
