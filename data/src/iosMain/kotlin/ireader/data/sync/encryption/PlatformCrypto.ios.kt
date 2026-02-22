package ireader.data.sync.encryption

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.Security.*
import platform.darwin.NSUIntegerVar

/**
 * iOS implementation of cryptographic operations using CommonCrypto and Security framework.
 * 
 * Task 9.2.4: AES-256 payload encryption (iOS platform)
 * 
 * Uses iOS native crypto APIs for secure encryption.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual class PlatformCrypto {
    
    actual fun generateSecureRandom(size: Int): ByteArray {
        val bytes = ByteArray(size)
        bytes.usePinned { pinned ->
            SecRandomCopyBytes(kSecRandomDefault, size.toULong(), pinned.addressOf(0))
        }
        return bytes
    }
    
    actual fun encryptAesGcm(plaintext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        // TODO: Implement using CommonCrypto CCCrypt with kCCAlgorithmAES128
        // For now, throw an exception to indicate not implemented
        throw NotImplementedError("iOS AES-GCM encryption not yet implemented. Use Android or Desktop for testing.")
    }
    
    actual fun decryptAesGcm(ciphertextWithTag: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        // TODO: Implement using CommonCrypto CCCrypt with kCCAlgorithmAES128
        throw NotImplementedError("iOS AES-GCM decryption not yet implemented. Use Android or Desktop for testing.")
    }
}

/**
 * iOS Base64 encoding using Foundation.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun ByteArray.toBase64(): String {
    val nsData = this.toNSData()
    return nsData.base64EncodedStringWithOptions(0)
}

/**
 * iOS Base64 decoding using Foundation.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun String.fromBase64(): ByteArray {
    val nsData = NSData.create(base64EncodedString = this, options = 0)
        ?: throw IllegalArgumentException("Invalid Base64 string")
    return nsData.toByteArray()
}

/**
 * Convert ByteArray to NSData.
 */
@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData {
    return this.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = this.size.toULong())
    }
}

/**
 * Convert NSData to ByteArray.
 */
@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = this.length.toInt()
    val bytes = ByteArray(size)
    bytes.usePinned { pinned ->
        memcpy(pinned.addressOf(0), this.bytes, this.length)
    }
    return bytes
}
