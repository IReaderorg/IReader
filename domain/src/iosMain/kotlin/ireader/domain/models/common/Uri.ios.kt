package ireader.domain.models.common

import platform.Foundation.NSURL

/**
 * iOS implementation of Uri using NSURL
 */
actual class Uri private constructor(private val urlString: String) {
    
    actual override fun toString(): String = urlString
    
    actual companion object {
        actual fun parse(uriString: String): Uri = Uri(uriString)
        
        actual val EMPTY: Uri = Uri("")
    }
}
