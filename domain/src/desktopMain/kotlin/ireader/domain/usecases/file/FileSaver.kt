package ireader.domain.usecases.file

import ireader.domain.models.common.Uri
import java.io.File

class DesktopFileSaver : FileSaver {
    override fun save(uri: Uri, byteArray: ByteArray) {
        val file = File(uri.uriString)
        if (!file.exists()) {
            file.createNewFile()
        }
        file.writeBytes(byteArray)
    }

    override fun validate(uri: Uri): Boolean {
        return File(uri.uriString).exists()
    }

    override fun read(uri: Uri): ByteArray {
        return File(uri.uriString).readBytes()
    }

}