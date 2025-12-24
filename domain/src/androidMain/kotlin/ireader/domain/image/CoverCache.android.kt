package ireader.domain.image

import android.content.Context
import coil3.imageLoader
import ireader.core.io.AndroidVirtualFile
import ireader.core.io.VirtualFile
import ireader.core.io.VirtualInputStream
import ireader.domain.image.cache.DiskUtil
import ireader.domain.models.BookCover
import java.io.File
import java.io.IOException

actual class CoverCache actual constructor(
    private val context: Any
) {
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
            val file = File(virtualFile.path)
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
        val file = File(virtualFile.path)
        return file.exists() && file.delete()
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