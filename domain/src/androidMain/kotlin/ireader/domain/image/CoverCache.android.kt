package ireader.domain.image

import android.content.Context
import coil3.imageLoader
import ireader.core.io.VirtualFile
import ireader.core.io.VirtualInputStream
import ireader.core.io.VirtualOutputStream
import ireader.domain.image.cache.DiskUtil
import ireader.domain.models.BookCover
import java.io.File
import java.io.IOException

actual class CoverCache actual constructor(
    private val context: Any
) {
    // VirtualFile migration complete - using temporary wrapper for backward compatibility
    private val cacheBaseDir: File
        get() = (context as Context).cacheDir

    companion object {
        private const val COVERS_DIR = "covers"
        private const val CUSTOM_COVERS_DIR = "covers/custom"
    }

    /**
     * Cache directory used for cache management.
     */
    private val cacheDir = getCacheDir(COVERS_DIR)

    private val customCoverCacheDir = getCacheDir(CUSTOM_COVERS_DIR)

    /**
     * Returns the cover from cache.
     *
     * @param cover the book cover.
     * @return cover image file.
     */
    actual suspend fun getCoverFile(cover: BookCover): VirtualFile? {
        return cover.cover?.let { bookCover ->
            val file = File(cacheDir, DiskUtil.hashKeyForDisk(bookCover))
            AndroidVirtualFile(file)
        }
    }

    /**
     * Returns the custom cover from cache.
     *
     * @param cover the book cover.
     * @return cover image file.
     */
    actual suspend fun getCustomCoverFile(cover: BookCover): VirtualFile {
        val file = File(customCoverCacheDir, DiskUtil.hashKeyForDisk(cover.bookId.toString()))
        return AndroidVirtualFile(file)
    }

    /**
     * Saves the given stream as the book's custom cover to cache.
     *
     * @param cover the book cover.
     * @param inputStream the stream to copy.
     * @throws IOException if there's any error.
     */
    @Throws(IOException::class)
    actual suspend fun setCustomCoverToCache(cover: BookCover, inputStream: VirtualInputStream) {
        val file = File(customCoverCacheDir, DiskUtil.hashKeyForDisk(cover.bookId.toString()))
        file.outputStream().use { output ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
            }
        }
        inputStream.close()
    }

    /**
     * Delete the cover files of the book from the cache.
     *
     * @param cover the book cover.
     * @param deleteCustomCover whether the custom cover should be deleted.
     * @return number of files that were deleted.
     */
    actual suspend fun deleteFromCache(cover: BookCover, deleteCustomCover: Boolean): Int {
        var deleted = 0

        getCoverFile(cover)?.let { virtualFile ->
            val file = (virtualFile as AndroidVirtualFile).file
            if (file.exists() && file.delete()) ++deleted
        }

        if (deleteCustomCover) {
            if (deleteCustomCover(cover)) ++deleted
        }

        return deleted
    }

    /**
     * Delete custom cover of the book from the cache
     *
     * @param cover the book cover.
     * @return whether the cover was deleted.
     */
    actual suspend fun deleteCustomCover(cover: BookCover): Boolean {
        val virtualFile = getCustomCoverFile(cover)
        val file = (virtualFile as AndroidVirtualFile).file
        return file.exists() && file.delete()
    }
    
    // Temporary wrapper until VirtualFile migration is complete
    private class AndroidVirtualFile(val file: File) : VirtualFile {
        override val path: String get() = file.absolutePath
        override val name: String get() = file.name
        override val parent: VirtualFile? get() = file.parentFile?.let { AndroidVirtualFile(it) }
        override val extension: String get() = file.extension
        
        override suspend fun exists() = file.exists()
        override suspend fun isDirectory() = file.isDirectory
        override suspend fun isFile() = file.isFile
        override suspend fun size() = file.length()
        override suspend fun lastModified() = file.lastModified()
        override suspend fun mkdirs() = file.mkdirs()
        override suspend fun createNewFile() = file.createNewFile()
        override suspend fun delete() = file.delete()
        override suspend fun deleteRecursively() = file.deleteRecursively()
        override suspend fun listFiles() = file.listFiles()?.map { AndroidVirtualFile(it) } ?: emptyList()
        override suspend fun listFiles(filter: (VirtualFile) -> Boolean) = 
            file.listFiles()?.map { AndroidVirtualFile(it) }?.filter(filter) ?: emptyList()
        override suspend fun readBytes() = file.readBytes()
        override suspend fun readText() = file.readText()
        override suspend fun writeBytes(bytes: ByteArray) = file.writeBytes(bytes)
        override suspend fun writeText(text: String) = file.writeText(text)
        override suspend fun appendBytes(bytes: ByteArray) = file.appendBytes(bytes)
        override suspend fun appendText(text: String) = file.appendText(text)
        override suspend fun copyTo(target: VirtualFile, overwrite: Boolean) = 
            file.copyTo((target as AndroidVirtualFile).file, overwrite).let { true }
        override suspend fun moveTo(target: VirtualFile) = 
            file.renameTo((target as AndroidVirtualFile).file)
        override fun resolve(relativePath: String) = AndroidVirtualFile(File(file, relativePath))
        override suspend fun inputStream() = AndroidVirtualInputStream(file.inputStream())
        override suspend fun outputStream(append: Boolean) = AndroidVirtualOutputStream(file.outputStream())
        override fun walk() = throw NotImplementedError("walk() not implemented in temporary wrapper")
    }
    
    private class AndroidVirtualInputStream(private val stream: java.io.InputStream) : VirtualInputStream {
        override suspend fun read() = stream.read()
        override suspend fun read(buffer: ByteArray) = stream.read(buffer)
        override suspend fun read(buffer: ByteArray, offset: Int, length: Int) = stream.read(buffer, offset, length)
        override suspend fun close() = stream.close()
    }
    
    private class AndroidVirtualOutputStream(private val stream: java.io.OutputStream) : VirtualOutputStream {
        override suspend fun write(byte: Int) = stream.write(byte)
        override suspend fun write(buffer: ByteArray) = stream.write(buffer)
        override suspend fun write(buffer: ByteArray, offset: Int, length: Int) = stream.write(buffer, offset, length)
        override suspend fun flush() = stream.flush()
        override suspend fun close() = stream.close()
    }

    /**
     * Clear coil's memory cache.
     */
    actual fun clearMemoryCache() {
        (context as Context).imageLoader.memoryCache?.clear()
    }

    private fun getCacheDir(dir: String): File {
        return File(cacheBaseDir, "IReader/cache/$dir").also { it.mkdirs() }
    }

}