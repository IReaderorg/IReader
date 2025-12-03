package ireader.domain.image.cache

import okio.Buffer
import okio.ByteString.Companion.encodeUtf8
import okio.ByteString.Companion.toByteString

/**
 * Hash utilities using Okio for KMP compatibility.
 * Replaces java.security.MessageDigest with okio.ByteString hashing.
 */
object Hash {

    fun sha256(bytes: ByteArray): String {
        return bytes.toByteString().sha256().hex()
    }

    fun sha256(string: String): String {
        return string.encodeUtf8().sha256().hex()
    }

    fun md5(bytes: ByteArray): String {
        return bytes.toByteString().md5().hex()
    }

    fun md5(string: String): String {
        return string.encodeUtf8().md5().hex()
    }
    
    fun sha1(bytes: ByteArray): String {
        return bytes.toByteString().sha1().hex()
    }
    
    fun sha1(string: String): String {
        return string.encodeUtf8().sha1().hex()
    }
    
    fun sha512(bytes: ByteArray): String {
        return bytes.toByteString().sha512().hex()
    }
    
    fun sha512(string: String): String {
        return string.encodeUtf8().sha512().hex()
    }
}
