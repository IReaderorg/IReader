package ireader.domain.usecases.file

import ireader.domain.models.common.Uri


interface FileSaver {
    fun save(uri: Uri, byteArray: ByteArray)

    fun validate(uri: Uri): Boolean

    fun read(uri: Uri) : ByteArray

}