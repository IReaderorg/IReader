package ireader.domain.models.common


import androidx.core.net.toUri
import android.net.Uri as AndroidUri

actual class Uri(val androidUri: AndroidUri) {
    val path: String get() = androidUri.path ?: androidUri.toString()
    
    actual override fun toString(): String = androidUri.toString()

    actual companion object {
        actual fun parse(uriString: String): Uri =
            Uri(uriString.toUri())
    }
}