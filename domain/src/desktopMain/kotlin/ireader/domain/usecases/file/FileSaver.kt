package ireader.domain.usecases.file

import ireader.domain.models.common.Uri

class DesktopFileSaver : FileSaver {
    override fun save(uri: Uri, byteArray: ByteArray) {
    }

    override fun validate(uri: Uri): Boolean {
        return true
    }

    override fun read(uri: Uri): ByteArray {
        return ByteArray(0)
    }

}