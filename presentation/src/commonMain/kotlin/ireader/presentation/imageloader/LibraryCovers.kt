package ireader.presentation.imageloader

import okio.FileSystem
import okio.Path


class LibraryCovers(
    private val fileSystem: FileSystem,
    private val path: Path,
) {

    init {
        fileSystem.createDirectories(path)
    }

    fun find(mangaId: Long): Path {
        return path / "$mangaId.0"
    }

    fun delete(mangaId: Long) {
        return fileSystem.delete(find(mangaId))
    }

    fun invalidate(mangaId: Long) {
        find(mangaId).setLastModified(0)
    }

    fun deleteAll() {
        fileSystem.delete(path)
    }
}

/**
 * Sets the last modified time of a file.
 * Note: This is a no-op on non-JVM platforms as okio doesn't support this directly.
 * The actual implementation should be provided per-platform if needed.
 */
expect fun Path.setLastModified(epoch: Long)
