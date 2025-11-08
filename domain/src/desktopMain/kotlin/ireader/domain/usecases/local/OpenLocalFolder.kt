package ireader.domain.usecases.local

import ireader.core.source.LocalCatalogSource
import java.awt.Desktop
import java.io.File

/**
 * Desktop implementation to open the local folder
 */
actual class OpenLocalFolder actual constructor(
    private val localSource: LocalCatalogSource
) {
    actual fun open(): Boolean {
        val folderPath = localSource.getLocalFolderPath()
        val folder = File(folderPath)
        
        // Create folder if it doesn't exist
        if (!folder.exists()) {
            folder.mkdirs()
        }
        
        return try {
            when {
                Desktop.isDesktopSupported() -> {
                    Desktop.getDesktop().open(folder)
                    true
                }
                System.getProperty("os.name").lowercase().contains("win") -> {
                    Runtime.getRuntime().exec("explorer.exe \"${folder.absolutePath}\"")
                    true
                }
                System.getProperty("os.name").lowercase().contains("mac") -> {
                    Runtime.getRuntime().exec(arrayOf("open", folder.absolutePath))
                    true
                }
                System.getProperty("os.name").lowercase().contains("nix") ||
                System.getProperty("os.name").lowercase().contains("nux") -> {
                    Runtime.getRuntime().exec(arrayOf("xdg-open", folder.absolutePath))
                    true
                }
                else -> false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    actual fun getPath(): String {
        return localSource.getLocalFolderPath()
    }
}
