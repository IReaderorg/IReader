package ireader.domain.usecases.file

import android.content.Context
import ireader.domain.models.common.Uri
import okio.buffer
import okio.gzip
import okio.sink
import okio.source
import java.io.InputStream

class AndroidFileSaver(
        private val context: Context
) : FileSaver {
     override fun save(uri: Uri, byteArray: ByteArray) {
        context.contentResolver.openOutputStream(uri.androidUri, "w")!!.sink().gzip().buffer()
                .use { output ->
                    output.write(byteArray)
                }
    }

     override fun validate(uri: Uri): Boolean {
        context.contentResolver.openInputStream(uri.androidUri)!!.source().gzip().buffer()
                .use { it.readByteArray() }
        return true
    }

    override fun read(uri: Uri): ByteArray {
        return context!!.contentResolver.openInputStream(uri.androidUri)!!.source().gzip().buffer().use {
            it.readByteArray()
        }
    }

    override fun readStream(uri: Uri): InputStream {
        return context.contentResolver.openInputStream(uri.androidUri)!!
    }

}