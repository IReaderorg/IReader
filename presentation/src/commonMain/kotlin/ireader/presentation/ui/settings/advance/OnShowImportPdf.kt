package ireader.presentation.ui.settings.advance

import androidx.compose.runtime.Composable
import ireader.domain.models.common.Uri

/**
 * Platform-specific PDF file picker dialog
 * 
 * Shows a file picker for selecting PDF files to import.
 * Supports multiple file selection.
 * 
 * @param show Whether to show the file picker
 * @param onFileSelected Callback with selected PDF file URIs (empty list if cancelled)
 */
@Composable
expect fun OnShowImportPdf(show: Boolean, onFileSelected: suspend (List<Uri>) -> Unit)
