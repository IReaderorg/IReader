package ireader.domain.plugins

import ireader.core.io.VirtualFile
import java.util.zip.ZipFile

/**
 * Desktop implementation of ZIP entry extraction
 * Uses java.util.zip.ZipFile for efficient ZIP handling
 */
actual suspend fun extractZipEntry(file: VirtualFile, entryName: String): String? {
    return try {
        // Convert VirtualFile to java.io.File for ZipFile
        val javaFile = java.io.File(file.path)
        
        ZipFile(javaFile).use { zip ->
            val entry = zip.getEntry(entryName) ?: return null
            
            zip.getInputStream(entry).use { stream ->
                stream.bufferedReader().readText()
            }
        }
    } catch (e: Exception) {
        null
    }
}
