package ireader.domain.usersource.importer

import ireader.domain.usersource.model.UserSource
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Exports UserSource to various formats for sharing.
 */
class SourceExporter {
    
    private val json = Json {
        prettyPrint = true
        encodeDefaults = false
    }
    
    private val compactJson = Json {
        prettyPrint = false
        encodeDefaults = false
    }
    
    /**
     * Export a single source to JSON.
     */
    fun exportToJson(source: UserSource, pretty: Boolean = true): String {
        return if (pretty) {
            json.encodeToString(source)
        } else {
            compactJson.encodeToString(source)
        }
    }
    
    /**
     * Export multiple sources to JSON array.
     */
    fun exportToJson(sources: List<UserSource>, pretty: Boolean = true): String {
        return if (pretty) {
            json.encodeToString(sources)
        } else {
            compactJson.encodeToString(sources)
        }
    }
    
    /**
     * Generate a shareable URL for the source (base64 encoded).
     */
    fun generateShareUrl(source: UserSource): String {
        val jsonStr = compactJson.encodeToString(source)
        val encoded = encodeBase64(jsonStr.encodeToByteArray())
        return "ireader://source?data=$encoded"
    }
    
    /**
     * Parse a share URL back to source.
     */
    fun parseShareUrl(url: String): UserSource? {
        return try {
            val data = url.substringAfter("data=")
            val decoded = decodeBase64(data)
            val jsonStr = decoded.decodeToString()
            compactJson.decodeFromString<UserSource>(jsonStr)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Generate QR code data for the source.
     */
    fun generateQrData(source: UserSource): String {
        return compactJson.encodeToString(source)
    }
    
    // Simple Base64 encoding/decoding for KMP
    private val base64Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    
    private fun encodeBase64(bytes: ByteArray): String {
        val result = StringBuilder()
        var i = 0
        while (i < bytes.size) {
            val b1 = bytes[i++].toInt() and 0xFF
            val b2 = if (i < bytes.size) bytes[i++].toInt() and 0xFF else 0
            val b3 = if (i < bytes.size) bytes[i++].toInt() and 0xFF else 0
            
            val padding = when {
                i == bytes.size + 2 -> 2
                i == bytes.size + 1 -> 1
                else -> 0
            }
            
            result.append(base64Chars[(b1 shr 2) and 0x3F])
            result.append(base64Chars[((b1 shl 4) or (b2 shr 4)) and 0x3F])
            result.append(if (padding >= 2) '=' else base64Chars[((b2 shl 2) or (b3 shr 6)) and 0x3F])
            result.append(if (padding >= 1) '=' else base64Chars[b3 and 0x3F])
        }
        return result.toString()
    }
    
    private fun decodeBase64(str: String): ByteArray {
        val result = mutableListOf<Byte>()
        var i = 0
        while (i < str.length) {
            val c1 = base64Chars.indexOf(str[i++])
            val c2 = base64Chars.indexOf(str[i++])
            val c3 = if (str[i] != '=') base64Chars.indexOf(str[i]) else 0
            i++
            val c4 = if (str[i] != '=') base64Chars.indexOf(str[i]) else 0
            i++
            
            result.add(((c1 shl 2) or (c2 shr 4)).toByte())
            if (str[i - 2] != '=') result.add((((c2 and 0xF) shl 4) or (c3 shr 2)).toByte())
            if (str[i - 1] != '=') result.add((((c3 and 0x3) shl 6) or c4).toByte())
        }
        return result.toByteArray()
    }
}
