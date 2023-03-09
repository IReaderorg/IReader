package ireader.domain.models.common


import android.net.Uri as AndroidUri

actual class Uri(val androidUri: AndroidUri) {
    actual override fun toString(): String = androidUri.toString()

    actual companion object {
        actual fun parse(uriString: String): Uri =
            Uri(AndroidUri.parse(uriString))
    }
}