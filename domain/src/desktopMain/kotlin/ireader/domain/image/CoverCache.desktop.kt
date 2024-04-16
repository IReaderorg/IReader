package ireader.domain.image

import coil3.PlatformContext
import ireader.domain.image.cache.DiskUtil
import ireader.domain.models.BookCover
import ireader.domain.usecases.files.GetSimpleStorage
import java.io.File
import java.io.IOException
import java.io.InputStream

/**
 * Class used to create cover cache.
 * It is used to store the covers of the library.
 * Makes use of Glide (which can avoid repeating requests) to download covers.
 * Names of files are created with the md5 of the thumbnail URL.
 *
 * @param context the application context.
 * @constructor creates an instance of the cover cache.
 */
actual class CoverCache actual constructor(
    private val context: Any,
    private val getSimpleStorage: GetSimpleStorage
) {
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
     * @param manga the manga.
     * @return cover image.
     */
    actual fun getCoverFile(cover: BookCover): File? {
        return cover.cover?.let { bookCover ->
            return File(cacheDir, DiskUtil.hashKeyForDisk(bookCover))
        }
    }

    /**
     * Returns the custom cover from cache.
     *
     * @param manga the manga.
     * @return cover image.
     */
    actual fun getCustomCoverFile(cover: BookCover): File {
        return File(customCoverCacheDir, DiskUtil.hashKeyForDisk(cover.bookId.toString()))
    }

    /**
     * Saves the given stream as the manga's custom cover to cache.
     *
     * @param manga the manga.
     * @param inputStream the stream to copy.
     * @throws IOException if there's any error.
     */
    @Throws(IOException::class)
    actual fun setCustomCoverToCache(cover: BookCover, inputStream: InputStream) {
        getCustomCoverFile(cover).outputStream().use {
            inputStream.copyTo(it)
        }
    }

    /**
     * Delete the cover files of the manga from the cache.
     *
     * @param manga the manga.
     * @param deleteCustomCover whether the custom cover should be deleted.
     * @return number of files that were deleted.
     */
    actual fun deleteFromCache(cover: BookCover, deleteCustomCover: Boolean): Int {
        var deleted = 0

        getCoverFile(cover)?.let {
            if (it.exists() && it.delete()) ++deleted
        }

        if (deleteCustomCover) {
            if (deleteCustomCover(cover)) ++deleted
        }

        return deleted
    }

    /**
     * Delete custom cover of the manga from the cache
     *
     * @param manga the manga.
     * @return whether the cover was deleted.
     */
    actual fun deleteCustomCover(cover: BookCover): Boolean {
        return getCustomCoverFile(cover).let {
            it.exists() && it.delete()
        }
    }

    /**
     * Clear coil's memory cache.
     */
    actual fun clearMemoryCache() {

    }

    private fun getCacheDir(dir: String): File {
        return File(getSimpleStorage.ireaderCacheDir(), dir).also { it.mkdirs() }
    }

}