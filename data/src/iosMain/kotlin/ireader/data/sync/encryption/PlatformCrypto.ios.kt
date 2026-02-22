package ireader.data.sync.encryption

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.Security.*

/**
 * iOS implementation of cryptographic operations.
 * 
 * Task 9.2.4: AES-256 payload encryption (iOS platform)
 * 
 * IMPORTANT: This implementation requires CryptoKit (iOS 13+) which is not
 * directly accessible from Kotlin/Native. For production use, you should:
 * 
 * 1. Create a Swift wrapper around CryptoKit's AES.GCM
 * 2. Expose it to Kotlin via @objc interface
 * 3. Call it from this Kotlin code
 * 
 * For now, this provides a placeholder implementation that throws
 * NotImplementedError with clear instructions.
 * 
 * Alternative: Use a third-party crypto library like libsodium that
 * has proper Kotlin/Native bindings.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual class PlatformCrypto {
    
    actual fun generateSecureRandom(size: Int): ByteArray {
        if (size == 0) return ByteArray(0)
        
        val bytes = ByteArray(size)
        val result = bytes.usePinned { pinned ->
            SecRandomCopyBytes(kSecRandomDefault, size.toULong(), pinned.addressOf(0))
        }
        
        // SecRandomCopyBytes returns 0 on success, non-zero on failure
        if (result != 0) {
            throw SecurityException("Failed to generate secure random bytes. SecRandomCopyBytes returned error code: $result")
        }
        
        return bytes
    }
    
    actual fun encryptAesGcm(plaintext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        require(key.size == 32) { "Key must be 256 bits (32 bytes)" }
        require(iv.size == 12) { "IV must be 96 bits (12 bytes) for GCM" }
        
        throw NotImplementedError(
            """
            iOS AES-GCM encryption requires CryptoKit (iOS 13+) which is not directly 
            accessible from Kotlin/Native.
            
            To implement this:
            
            1. Create a Swift file (e.g., CryptoKitBridge.swift):
            
            ```swift
            import CryptoKit
            import Foundation
            
            @objc public class CryptoKitBridge: NSObject {
                @objc public static func encryptAesGcm(
                    plaintext: Data,
                    key: Data,
                    nonce: Data
                ) throws -> Data {
                    let symmetricKey = SymmetricKey(data: key)
                    let gcmNonce = try AES.GCM.Nonce(data: nonce)
                    let sealedBox = try AES.GCM.seal(plaintext, using: symmetricKey, nonce: gcmNonce)
                    return sealedBox.combined!
                }
                
                @objc public static func decryptAesGcm(
                    combined: Data,
                    key: Data,
                    nonce: Data
                ) throws -> Data {
                    let symmetricKey = SymmetricKey(data: key)
                    let sealedBox = try AES.GCM.SealedBox(combined: combined)
                    return try AES.GCM.open(sealedBox, using: symmetricKey)
                }
            }
            ```
            
            2. Call it from Kotlin:
            
            ```kotlin
            val plaintextData = plaintext.toNSData()
            val keyData = key.toNSData()
            val nonceData = iv.toNSData()
            
            val combined = CryptoKitBridge.encryptAesGcm(
                plaintext = plaintextData,
                key = keyData,
                nonce = nonceData
            )
            
            return combined.toByteArray()
            ```
            
            Alternative: Use libsodium with Kotlin/Native bindings for cross-platform crypto.
            """.trimIndent()
        )
    }
    
    actual fun decryptAesGcm(ciphertextWithTag: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        require(key.size == 32) { "Key must be 256 bits (32 bytes)" }
        require(iv.size == 12) { "IV must be 96 bits (12 bytes) for GCM" }
        require(ciphertextWithTag.size >= 16) { "Ciphertext must include 16-byte authentication tag" }
        
        throw NotImplementedError(
            """
            iOS AES-GCM decryption requires CryptoKit (iOS 13+) which is not directly 
            accessible from Kotlin/Native.
            
            See encryptAesGcm() documentation for implementation instructions.
            
            The Swift bridge method decryptAesGcm() should be called here.
            """.trimIndent()
        )
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
