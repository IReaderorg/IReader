package ireader.domain.usecases.file

import ireader.domain.models.common.Uri
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.Source
import okio.buffer

class DesktopFileSaver : FileSaver {
    
    private val fileSystem = FileSystem.SYSTEM
    
    override fun save(uri: Uri, byteArray: ByteArray) {
        val path = uri.uriString.toPath()
        fileSystem.sink(path).buffer().use { sink ->
            sink.write(byteArray)
        }
    }

    override fun validate(uri: Uri): Boolean {
        return fileSystem.exists(uri.uriString.toPath())
    }

    override fun read(uri: Uri): ByteArray {
        val path = uri.uriString.toPath()
        return fileSystem.source(path).buffer().use { source ->
            source.readByteArray()
        }
    }

    override fun readSource(uri: Uri): Source {
        return fileSystem.source(uri.uriString.toPath())
    }
}