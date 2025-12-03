package ireader.core.util

/**
 * iOS implementation of PlatformFilePicker
 * TODO: Implement using UIDocumentPickerViewController
 */
actual object PlatformFilePicker {
    actual suspend fun pickFiles(
        allowMultiple: Boolean,
        mimeTypes: List<String>
    ): List<String> {
        // TODO: Implement using UIDocumentPickerViewController
        return emptyList()
    }
    
    actual suspend fun pickDirectory(): String? {
        // TODO: Implement using UIDocumentPickerViewController
        return null
    }
}
