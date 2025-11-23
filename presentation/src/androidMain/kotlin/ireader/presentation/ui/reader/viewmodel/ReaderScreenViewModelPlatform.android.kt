package ireader.presentation.ui.reader.viewmodel

/**
 * Android implementation - no-op since Android has native Google Fonts API support
 * Fonts are downloaded automatically by Google Play Services
 */
actual suspend fun ReaderScreenViewModel.downloadGoogleFontIfNeeded(fontName: String) {
    // No-op on Android - Google Fonts are handled natively
    ireader.core.log.Log.debug("Android: Using native Google Fonts API for: $fontName")
}
