

package org.ireader.core_api.util

object ImageUtil {

    private val jpgMagic = charByteArrayOf(0xFF, 0xD8, 0xFF)
    private val pngMagic = charByteArrayOf(0x89, 0x50, 0x4E, 0x47)
    private val gifMagic = "GIF8".encodeToByteArray()
    private val webpMagic = "RIFF".encodeToByteArray()

    fun findType(bytes: ByteArray): ImageType? {
        return when {
            bytes.compareWith(jpgMagic) -> ImageType.JPG
            bytes.compareWith(pngMagic) -> ImageType.PNG
            bytes.compareWith(gifMagic) -> ImageType.GIF
            bytes.compareWith(webpMagic) -> ImageType.WEBP
            else -> null
        }
    }

    private fun ByteArray.compareWith(magic: ByteArray): Boolean {
        for (i in magic.indices) {
            if (this[i] != magic[i]) return false
        }
        return true
    }

    private fun charByteArrayOf(vararg bytes: Int): ByteArray {
        return ByteArray(bytes.size) { pos -> bytes[pos].toByte() }
    }

    enum class ImageType(val mime: String, val extension: String) {
        JPG("image/jpeg", "jpg"),
        PNG("image/png", "png"),
        GIF("image/gif", "gif"),
        WEBP("image/webp", "webp")
    }
}
