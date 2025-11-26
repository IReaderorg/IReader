package ireader.domain.image

import coil3.PlatformContext
import ireader.core.io.VirtualFile
import ireader.core.io.VirtualInputStream
import ireader.domain.models.BookCover
import java.io.IOException

/**
 * Class used to create cover cache.
 * It is used to store the covers of the library.
 * Makes use of Coil (which can avoid repeating requests) to download covers.
 * Names of files are created with the md5 of the thumbnail URL.
 *
 * @param context the application context.
 * @constructor creates an instance of the cover cache.
 */
expect class CoverCache(context: Any) {

    /**
     * Returns the cover from cache.
     *
     * @param cover the book cover.
     * @return cover image file.
     */
    suspend fun getCoverFile(cover: BookCover): VirtualFile?

    /**
     * Returns the custom cover from cache.
     *
     * @param cover the book cover.
     * @return cover image file.
     */
    suspend fun getCustomCoverFile(cover: BookCover): VirtualFile

    /**
     * Saves the given stream as the book's custom cover to cache.
     *
     * @param cover the book cover.
     * @param inputStream the stream to copy.
     * @throws IOException if there's any error.
     */
    suspend fun setCustomCoverToCache(cover: BookCover, inputStream: VirtualInputStream)

    /**
     * Delete the cover files of the book from the cache.
     *
     * @param cover the book cover.
     * @param deleteCustomCover whether the custom cover should be deleted.
     * @return number of files that were deleted.
     */
    suspend fun deleteFromCache(cover: BookCover, deleteCustomCover: Boolean = false): Int

    /**
     * Delete custom cover of the book from the cache
     *
     * @param cover the book cover.
     * @return whether the cover was deleted.
     */
    suspend fun deleteCustomCover(cover: BookCover): Boolean

    /**
     * Clear coil's memory cache.
     */
    fun clearMemoryCache()

}
