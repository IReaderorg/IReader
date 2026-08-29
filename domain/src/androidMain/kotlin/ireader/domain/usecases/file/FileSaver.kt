package ireader.domain.usecases.file

import android.content.Context
import ireader.domain.models.common.Uri
import okio.Source
import okio.buffer
import okio.sink
import okio.source

class AndroidFileSaver(
    internal val context: Context
) : FileSaver {
    override fun save(uri: Uri, byteArray: ByteArray) {
        context.contentResolver.openOutputStream(uri.androidUri, "w")?.sink()?.buffer()?.use { output ->
            output.write(byteArray)
        } ?: throw IllegalStateException("Could not open output stream for URI: ${uri.androidUri}")
    }

    override fun validate(uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri.androidUri)?.use { true } ?: false
        } catch (_: Exception) {
            false
        }
    }

    override fun read(uri: Uri): ByteArray {
        return context.contentResolver.openInputStream(uri.androidUri)?.source()?.buffer()?.use {
            it.readByteArray()
        } ?: throw IllegalStateException("Could not open input stream for URI: ${uri.androidUri}")
    }

    override fun readSource(uri: Uri): Source {
        return context.contentResolver.openInputStream(uri.androidUri)?.source()
            ?: throw IllegalStateException("Could not open source for URI: ${uri.androidUri}")
    }
}