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

fun Path.setLastModified(epoch: Long) {
    toFile().setLastModified(epoch)
}
