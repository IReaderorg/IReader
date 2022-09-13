package ireader.domain.usecases.epub

import android.content.Context
import android.net.Uri
import java.io.InputStream

fun Context.openStream(uri: Uri): InputStream {
    return this.contentResolver.openInputStream(uri)!!
}
