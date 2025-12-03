package ireader.presentation.ui.reader.viewmodel

/**
 * iOS implementation of font download functionality
 * iOS uses system fonts, so this is a no-op
 */
actual suspend fun ReaderScreenViewModel.downloadGoogleFontIfNeeded(fontName: String) {
    // iOS uses system fonts - no download needed
    // Custom fonts need to be bundled with the app
}
