package ireader.core.util

import kotlin.random.Random

/**
 * KMP-compatible UUID generation.
 * Generates a random UUID v4 string.
 */
fun randomUUID(): String {
    val bytes = Random.nextBytes(16)
    
    // Set version to 4 (random UUID)
    bytes[6] = (bytes[6].toInt() and 0x0f or 0x40).toByte()
    // Set variant to RFC 4122
    bytes[8] = (bytes[8].toInt() and 0x3f or 0x80).toByte()
    
    return buildString {
        for (i in bytes.indices) {
            append(bytes[i].toUByte().toString(16).padStart(2, '0'))
            if (i == 3 || i == 5 || i == 7 || i == 9) append('-')
        }
    }
}

/**
 * Generate a UUID from a name (deterministic).
 * Uses a simple hash-based approach for KMP compatibility.
 */
fun uuidFromName(name: String): String {
    val hash = name.hashCode()
    val bytes = ByteArray(16)
    
    // Use hash to seed the bytes
    for (i in 0 until 4) {
        bytes[i] = (hash shr (i * 8)).toByte()
        bytes[i + 4] = (name.reversed().hashCode() shr (i * 8)).toByte()
        bytes[i + 8] = ((hash xor name.length) shr (i * 8)).toByte()
        bytes[i + 12] = ((hash + i) shr (i * 8)).toByte()
    }
    
    // Set version to 5 (name-based)
    bytes[6] = (bytes[6].toInt() and 0x0f or 0x50).toByte()
    // Set variant to RFC 4122
    bytes[8] = (bytes[8].toInt() and 0x3f or 0x80).toByte()
    
    return buildString {
        for (i in bytes.indices) {
            append(bytes[i].toUByte().toString(16).padStart(2, '0'))
            if (i == 3 || i == 5 || i == 7 || i == 9) append('-')
        }
    }
}
