package ireader.domain.usecases.local

import ireader.core.source.LocalCatalogSource

/**
 * Use case to open the local folder in the system file manager
 */
expect class OpenLocalFolder(localSource: LocalCatalogSource) {
    /**
     * Opens the local folder in the system file manager
     * Returns true if successful, false otherwise
     */
    fun open(): Boolean
    
    /**
     * Gets the path to the local folder
     */
    fun getPath(): String
}
