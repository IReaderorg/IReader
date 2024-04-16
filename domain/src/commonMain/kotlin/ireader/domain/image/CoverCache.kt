package ireader.domain.image

import coil3.PlatformContext
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
expect class CoverCache(context: Any,   getSimpleStorage: GetSimpleStorage) {

    /**
     * Returns the cover from cache.
     *
     * @param manga the manga.
     * @return cover image.
     */
    fun getCoverFile(cover: BookCover): File?

    /**
     * Returns the custom cover from cache.
     *
     * @param manga the manga.
     * @return cover image.
     */
     fun getCustomCoverFile(cover: BookCover): File

    /**
     * Saves the given stream as the manga's custom cover to cache.
     *
     * @param manga the manga.
     * @param inputStream the stream to copy.
     * @throws IOException if there's any error.
     */
     fun setCustomCoverToCache(cover: BookCover, inputStream: InputStream)

    /**
     * Delete the cover files of the manga from the cache.
     *
     * @param manga the manga.
     * @param deleteCustomCover whether the custom cover should be deleted.
     * @return number of files that were deleted.
     */
     fun deleteFromCache(cover: BookCover, deleteCustomCover: Boolean = false): Int

    /**
     * Delete custom cover of the manga from the cache
     *
     * @param manga the manga.
     * @return whether the cover was deleted.
     */
     fun deleteCustomCover(cover: BookCover): Boolean

    /**
     * Clear coil's memory cache.
     */
     fun clearMemoryCache()

}
