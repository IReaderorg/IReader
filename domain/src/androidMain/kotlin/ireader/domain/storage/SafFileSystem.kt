package ireader.domain.storage

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import okio.FileHandle
import okio.FileMetadata
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.Sink
import okio.Source
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.FileNotFoundException

/**
 * A FileSystem implementation that works with SAF (Storage Access Framework).
 * 
 * This bridges the gap between Okio's FileSystem API and Android's DocumentFile API,
 * allowing code that uses Okio to work transparently with SAF-selected folders.
 * 
 * Strategy:
 * - For SAF directories: Uses DocumentFile API via ContentResolver
 * - For regular paths: Delegates to FileSystem.SYSTEM
 * 
 * The SAF root is determined by the user's selected storage folder.
 */
class SafFileSystem(
    private val context: Context,
    private val safStorageManager: SafStorageManager?
) : FileSystem() {
    
    companion object {
        private const val TAG = "SafFileSystem"
        
        /**
         * Create a SafFileSystem instance.
         */
        fun create(context: Context, safStorageManager: SafStorageManager?): SafFileSystem {
            return SafFileSystem(context, safStorageManager)
        }
    }
    
    private val systemFs = FileSystem.SYSTEM
    
    /**
     * Check if a path is within the SAF-managed directory.
     */
    private fun isSafPath(path: Path): Boolean {
        // SAF paths are identified by a special prefix or by checking if they're
        // within the SAF root directory
        val safRoot = safStorageManager?.getRootDocumentFile()
        if (safRoot == null) return false
        
        // For now, we use a simple heuristic: if the path starts with the SAF directory name
        // This will be refined based on actual usage
        return false // Disabled for now - using fallback paths
    }
    
    /**
     * Get DocumentFile for a path within SAF storage.
     */
    private fun getDocumentFile(path: Path): DocumentFile? {
        val safRoot = safStorageManager?.getRootDocumentFile() ?: return null
        
        // Navigate to the file
        val segments = path.segments
        var current: DocumentFile = safRoot
        
        for (segment in segments) {
            val child = current.findFile(segment) ?: return null
            current = child
        }
        
        return current
    }

    
    override fun appendingSink(file: Path, mustExist: Boolean): Sink {
        if (isSafPath(file)) {
            throw UnsupportedOperationException("Appending not supported for SAF paths")
        }
        return systemFs.appendingSink(file, mustExist)
    }
    
    override fun atomicMove(source: Path, target: Path) {
        if (isSafPath(source) || isSafPath(target)) {
            throw UnsupportedOperationException("Atomic move not supported for SAF paths")
        }
        systemFs.atomicMove(source, target)
    }
    
    override fun canonicalize(path: Path): Path {
        if (isSafPath(path)) {
            return path // SAF paths are already canonical
        }
        return systemFs.canonicalize(path)
    }
    
    override fun createDirectory(dir: Path, mustCreate: Boolean) {
        if (isSafPath(dir)) {
            val parent = dir.parent?.let { getDocumentFile(it) }
            if (parent != null) {
                val dirName = dir.name
                if (parent.findFile(dirName) == null) {
                    parent.createDirectory(dirName)
                } else if (mustCreate) {
                    throw FileNotFoundException("Directory already exists: $dir")
                }
            }
            return
        }
        systemFs.createDirectory(dir, mustCreate)
    }
    
    override fun createSymlink(source: Path, target: Path) {
        if (isSafPath(source) || isSafPath(target)) {
            throw UnsupportedOperationException("Symlinks not supported for SAF paths")
        }
        systemFs.createSymlink(source, target)
    }
    
    override fun delete(path: Path, mustExist: Boolean) {
        if (isSafPath(path)) {
            val docFile = getDocumentFile(path)
            if (docFile != null) {
                docFile.delete()
            } else if (mustExist) {
                throw FileNotFoundException("File not found: $path")
            }
            return
        }
        systemFs.delete(path, mustExist)
    }
    
    override fun list(dir: Path): List<Path> {
        if (isSafPath(dir)) {
            val docFile = getDocumentFile(dir) ?: return emptyList()
            return docFile.listFiles().mapNotNull { child ->
                child.name?.let { dir / it }
            }
        }
        return systemFs.list(dir)
    }
    
    override fun listOrNull(dir: Path): List<Path>? {
        if (isSafPath(dir)) {
            val docFile = getDocumentFile(dir) ?: return null
            return docFile.listFiles().mapNotNull { child ->
                child.name?.let { dir / it }
            }
        }
        return systemFs.listOrNull(dir)
    }
    
    override fun metadataOrNull(path: Path): FileMetadata? {
        if (isSafPath(path)) {
            val docFile = getDocumentFile(path) ?: return null
            return FileMetadata(
                isRegularFile = docFile.isFile,
                isDirectory = docFile.isDirectory,
                size = docFile.length(),
                createdAtMillis = null,
                lastModifiedAtMillis = docFile.lastModified(),
                lastAccessedAtMillis = null
            )
        }
        return systemFs.metadataOrNull(path)
    }
    
    override fun openReadOnly(file: Path): FileHandle {
        if (isSafPath(file)) {
            throw UnsupportedOperationException("FileHandle not supported for SAF paths, use source() instead")
        }
        return systemFs.openReadOnly(file)
    }
    
    override fun openReadWrite(file: Path, mustCreate: Boolean, mustExist: Boolean): FileHandle {
        if (isSafPath(file)) {
            throw UnsupportedOperationException("FileHandle not supported for SAF paths, use sink() instead")
        }
        return systemFs.openReadWrite(file, mustCreate, mustExist)
    }
    
    override fun sink(file: Path, mustCreate: Boolean): Sink {
        if (isSafPath(file)) {
            val parent = file.parent?.let { getDocumentFile(it) }
            if (parent != null) {
                val fileName = file.name
                // Delete existing file
                parent.findFile(fileName)?.delete()
                // Create new file
                val mimeType = getMimeType(fileName)
                val newFile = parent.createFile(mimeType, fileName)
                if (newFile != null) {
                    val outputStream = context.contentResolver.openOutputStream(newFile.uri)
                    if (outputStream != null) {
                        return outputStream.sink()
                    }
                }
            }
            throw FileNotFoundException("Cannot create file: $file")
        }
        return systemFs.sink(file, mustCreate)
    }
    
    override fun source(file: Path): Source {
        if (isSafPath(file)) {
            val docFile = getDocumentFile(file)
            if (docFile != null && docFile.exists()) {
                val inputStream = context.contentResolver.openInputStream(docFile.uri)
                if (inputStream != null) {
                    return inputStream.source()
                }
            }
            throw FileNotFoundException("File not found: $file")
        }
        return systemFs.source(file)
    }
    
    /**
     * Get MIME type for a file name.
     */
    private fun getMimeType(fileName: String): String {
        return when {
            fileName.endsWith(".js") -> "application/javascript"
            fileName.endsWith(".json") -> "application/json"
            fileName.endsWith(".txt") -> "text/plain"
            fileName.endsWith(".png") -> "image/png"
            fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") -> "image/jpeg"
            fileName.endsWith(".apk") -> "application/vnd.android.package-archive"
            else -> "application/octet-stream"
        }
    }
}
