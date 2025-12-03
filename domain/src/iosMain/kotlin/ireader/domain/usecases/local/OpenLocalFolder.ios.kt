package ireader.domain.usecases.local

import ireader.core.source.LocalCatalogSource

/**
 * iOS implementation of OpenLocalFolder
 * 
 * TODO: Implement using UIDocumentPickerViewController or Files app integration
 */
actual class OpenLocalFolder actual constructor(private val localSource: LocalCatalogSource) {
    
    actual fun open(): Boolean {
        // TODO: Implement - iOS doesn't have a direct "open folder" concept
        // Could open Files app or show a document picker
        return false
    }
    
    actual fun getPath(): String {
        // TODO: Return the actual local folder path
        return ""
    }
}
