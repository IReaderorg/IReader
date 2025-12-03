package ireader.core.util

/**
 * iOS implementation of PlatformFilePicker
 * TODO: Implement using UIDocumentPickerViewController
 */
actual object PlatformFilePicker {
    actual suspend fun pickFiles(
        fileTypes: List<String>,
        multiSelect: Boolean
    ): List<String>? {
        // TODO: Implement using UIDocumentPickerViewController
        return null
    }
}
