package ireader.presentation.ui.reader.viewmodel

/**
 * Platform-specific font download functionality
 * Desktop: Downloads Google Fonts
 * Android: No-op (uses native Google Fonts API)
 */
expect suspend fun ReaderScreenViewModel.downloadGoogleFontIfNeeded(fontName: String)