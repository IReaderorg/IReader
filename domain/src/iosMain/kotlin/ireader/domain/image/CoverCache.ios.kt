package ireader.domain.image

import ireader.core.io.VirtualFile
import ireader.core.io.VirtualInputStream
import ireader.domain.models.BookCover
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSURL

/**
 * iOS implementation of CoverCache
 * 
 * Uses Okio FileSystem for file operations
 */
actual class CoverCache actual constructor(context: Any) {
    
    private val okioFileSystem = FileSystem.SYSTEM
    
    private val cacheDir: Path by lazy {
        val paths = NSFileManager.defaultManager.URLsForDirectory(
            NSCachesDirectory,
            NSUserDomainMask
        )
        val cachePath = (paths.firstOrNull() as? NSURL)?.path ?: "/tmp"
        "$cachePath/covers".toPath().also { dir ->
            if (!okioFileSystem.exists(dir)) {
                okioFileSystem.createDirectories(dir)
            }
        }
    }
    
    private fun getCoverPath(cover: BookCover): Path {
        return cacheDir / "${cover.bookId}.jpg"
    }
    
    private fun getCustomCoverPath(cover: BookCover): Path {
        return cacheDir / "${cover.bookId}_custom.jpg"
    }
    
    actual suspend fun getCoverFile(cover: BookCover): VirtualFile? {
        val path = getCoverPath(cover)
        return if (okioFileSystem.exists(path)) {
            OkioVirtualFile(path, okioFileSystem)
        } else {
            null
        }
    }
    
    actual suspend fun getCustomCoverFile(cover: BookCover): VirtualFile {
        return OkioVirtualFile(getCustomCoverPath(cover), okioFileSystem)
    }
    
    actual suspend fun setCustomCoverToCache(cover: BookCover, inputStream: VirtualInputStream) {
        val path = getCustomCoverPath(cover)
        
        // Ensure directory exists
        if (!okioFileSystem.exists(cacheDir)) {
            okioFileSystem.createDirectories(cacheDir)
        }
        
        // Read all bytes from input stream and write to file
        val buffer = ByteArray(8192)
        val allBytes = mutableListOf<Byte>()
        var bytesRead: Int = inputStream.read(buffer)
        while (bytesRead != -1) {
            for (i in 0 until bytesRead) {
                allBytes.add(buffer[i])
            }
            bytesRead = inputStream.read(buffer)
        }
        inputStream.close()
        
        val sink = okioFileSystem.sink(path).buffer()
        try {
            sink.write(allBytes.toByteArray())
        } finally {
            sink.close()
        }
    }
    
    actual suspend fun deleteFromCache(cover: BookCover, deleteCustomCover: Boolean): Int {
        var deleted = 0
        
        val coverPath = getCoverPath(cover)
        if (okioFileSystem.exists(coverPath)) {
            okioFileSystem.delete(coverPath)
            deleted++
        }
        
        if (deleteCustomCover) {
            val customPath = getCustomCoverPath(cover)
            if (okioFileSystem.exists(customPath)) {
                okioFileSystem.delete(customPath)
                deleted++
            }
        }
        
        return deleted
    }
    
    actual suspend fun deleteCustomCover(cover: BookCover): Boolean {
        val customPath = getCustomCoverPath(cover)
        return if (okioFileSystem.exists(customPath)) {
            okioFileSystem.delete(customPath)
            true
        } else {
            false
        }
    }
    
    actual fun clearMemoryCache() {
        // iOS doesn't have a separate memory cache to clear
        // Coil handles this internally
    }
}

/**
 * Simple VirtualFile implementation using Okio
 */
private class OkioVirtualFile(
    private val okioPath: Path,
    private val fileSystem: FileSystem
) : VirtualFile {
    override val path: String = okioPath.toString()
    override val name: String = okioPath.name
    override val parent: VirtualFile? = okioPath.parent?.let { OkioVirtualFile(it, fileSystem) }
    override val extension: String = okioPath.name.substringAfterLast('.', "")
    
    override suspend fun exists(): Boolean = fileSystem.exists(okioPath)
    override suspend fun isDirectory(): Boolean = fileSystem.metadata(okioPath).isDirectory
    override suspend fun isFile(): Boolean = fileSystem.metadata(okioPath).isRegularFile
    override suspend fun size(): Long = fileSystem.metadata(okioPath).size ?: 0L
    override suspend fun lastModified(): Long = fileSystem.metadata(okioPath).lastModifiedAtMillis ?: 0L
    
    override suspend fun mkdirs(): Boolean {
        return try {
            fileSystem.createDirectories(okioPath)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun createNewFile(): Boolean {
        return try {
            fileSystem.sink(okioPath).close()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun delete(): Boolean {
        return try {
            fileSystem.delete(okioPath)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun deleteRecursively(): Boolean {
        return try {
            fileSystem.deleteRecursively(okioPath)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun listFiles(): List<VirtualFile> {
        return try {
            fileSystem.list(okioPath).map { OkioVirtualFile(it, fileSystem) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun listFiles(filter: (VirtualFile) -> Boolean): List<VirtualFile> {
        return listFiles().filter(filter)
    }
    
    override suspend fun readBytes(): ByteArray {
        val source = fileSystem.source(okioPath).buffer()
        return try {
            source.readByteArray()
        } finally {
            source.close()
        }
    }
    
    override suspend fun readText(): String {
        val source = fileSystem.source(okioPath).buffer()
        return try {
            source.readUtf8()
        } finally {
            source.close()
        }
    }
    
    override suspend fun writeBytes(bytes: ByteArray) {
        val sink = fileSystem.sink(okioPath).buffer()
        try {
            sink.write(bytes)
        } finally {
            sink.close()
        }
    }
    
    override suspend fun writeText(text: String) {
        val sink = fileSystem.sink(okioPath).buffer()
        try {
            sink.writeUtf8(text)
        } finally {
            sink.close()
        }
    }
    
    override suspend fun appendBytes(bytes: ByteArray) {
        val sink = fileSystem.appendingSink(okioPath).buffer()
        try {
            sink.write(bytes)
        } finally {
            sink.close()
        }
    }
    
    override suspend fun appendText(text: String) {
        val sink = fileSystem.appendingSink(okioPath).buffer()
        try {
            sink.writeUtf8(text)
        } finally {
            sink.close()
        }
    }
    
    override suspend fun copyTo(target: VirtualFile, overwrite: Boolean): Boolean {
        return try {
            val targetPath = (target as? OkioVirtualFile)?.okioPath ?: target.path.toPath()
            if (overwrite && fileSystem.exists(targetPath)) {
                fileSystem.delete(targetPath)
            }
            fileSystem.copy(okioPath, targetPath)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun moveTo(target: VirtualFile): Boolean {
        return try {
            val targetPath = (target as? OkioVirtualFile)?.okioPath ?: target.path.toPath()
            fileSystem.atomicMove(okioPath, targetPath)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override fun resolve(relativePath: String): VirtualFile {
        return OkioVirtualFile(okioPath / relativePath, fileSystem)
    }
    
    override suspend fun inputStream(): VirtualInputStream {
        return OkioVirtualInputStream(fileSystem.source(okioPath).buffer())
    }
    
    override suspend fun outputStream(append: Boolean): ireader.core.io.VirtualOutputStream {
        val sink = if (append) fileSystem.appendingSink(okioPath) else fileSystem.sink(okioPath)
        return OkioVirtualOutputStream(sink.buffer())
    }
    
    override fun walk(): kotlinx.coroutines.flow.Flow<VirtualFile> = kotlinx.coroutines.flow.flow {
        suspend fun walkDir(dir: Path) {
            emit(OkioVirtualFile(dir, fileSystem))
            try {
                fileSystem.list(dir).forEach { child ->
                    if (fileSystem.metadata(child).isDirectory) {
                        walkDir(child)
                    } else {
                        emit(OkioVirtualFile(child, fileSystem))
                    }
                }
            } catch (e: Exception) {
                // Ignore errors
            }
        }
        walkDir(okioPath)
    }
}

private class OkioVirtualInputStream(
    private val source: okio.BufferedSource
) : VirtualInputStream {
    override suspend fun read(): Int {
        return if (source.exhausted()) -1 else source.readByte().toInt() and 0xFF
    }
    
    override suspend fun read(buffer: ByteArray): Int {
        return source.read(buffer)
    }
    
    override suspend fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        return source.read(buffer, offset, length)
    }
    
    override suspend fun close() {
        source.close()
    }
}

private class OkioVirtualOutputStream(
    private val sink: okio.BufferedSink
) : ireader.core.io.VirtualOutputStream {
    override suspend fun write(byte: Int) {
        sink.writeByte(byte)
    }
    
    override suspend fun write(buffer: ByteArray) {
        sink.write(buffer)
    }
    
    override suspend fun write(buffer: ByteArray, offset: Int, length: Int) {
        sink.write(buffer, offset, length)
    }
    
    override suspend fun flush() {
        sink.flush()
    }
    
    override suspend fun close() {
        sink.close()
    }
}
