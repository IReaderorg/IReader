package ireader.domain.models.common

actual class Uri(val uriString: String) {
    val path: String get() = uriString
    
    actual override fun toString(): String = uriString

    actual companion object {
        actual fun parse(uriString: String): Uri = Uri(uriString)
    }
}