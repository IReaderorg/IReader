package ireader.domain.usecases.file

import ireader.domain.models.common.Uri
import okio.Source

/**
 * Platform-agnostic file saver interface.
 * Uses Okio Source for KMP compatibility instead of java.io.InputStream.
 */
interface FileSaver {
    fun save(uri: Uri, byteArray: ByteArray)

    fun validate(uri: Uri): Boolean

    fun read(uri: Uri): ByteArray
    
    /**
     * Read file as Okio Source for streaming operations.
     */
    fun readSource(uri: Uri): Source
}