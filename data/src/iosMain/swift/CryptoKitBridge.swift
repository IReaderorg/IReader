import CryptoKit
import Foundation

/**
 * Swift bridge for CryptoKit AES-GCM operations.
 * 
 * This bridge exposes CryptoKit's AES.GCM functionality to Kotlin/Native.
 * 
 * Usage:
 * 1. Add this file to your iOS project
 * 2. Ensure it's included in the framework/module that Kotlin/Native can access
 * 3. Update PlatformCrypto.ios.kt to call these methods instead of throwing NotImplementedError
 * 
 * Security: Uses CryptoKit's hardware-accelerated AES-256-GCM implementation.
 */
@objc public class CryptoKitBridge: NSObject {
    
    /**
     * Encrypt data using AES-256-GCM.
     * 
     * @param plaintext Data to encrypt
     * @param key 256-bit (32-byte) encryption key
     * @param nonce 96-bit (12-byte) nonce/IV
     * @return Combined ciphertext and authentication tag
     * @throws Error if encryption fails or parameters are invalid
     */
    @objc public static func encryptAesGcm(
        plaintext: Data,
        key: Data,
        nonce: Data
    ) throws -> Data {
        // Validate parameters
        guard key.count == 32 else {
            throw NSError(
                domain: "CryptoKitBridge",
                code: 1,
                userInfo: [NSLocalizedDescriptionKey: "Key must be 256 bits (32 bytes)"]
            )
        }
        
        guard nonce.count == 12 else {
            throw NSError(
                domain: "CryptoKitBridge",
                code: 2,
                userInfo: [NSLocalizedDescriptionKey: "Nonce must be 96 bits (12 bytes)"]
            )
        }
        
        // Create symmetric key
        let symmetricKey = SymmetricKey(data: key)
        
        // Create nonce
        let gcmNonce = try AES.GCM.Nonce(data: nonce)
        
        // Encrypt
        let sealedBox = try AES.GCM.seal(plaintext, using: symmetricKey, nonce: gcmNonce)
        
        // Return combined format (nonce + ciphertext + tag)
        // Note: We only return ciphertext + tag since nonce is provided separately
        guard let combined = sealedBox.combined else {
            throw NSError(
                domain: "CryptoKitBridge",
                code: 3,
                userInfo: [NSLocalizedDescriptionKey: "Failed to get combined ciphertext and tag"]
            )
        }
        
        // Extract just ciphertext + tag (skip the nonce at the beginning)
        let ciphertextAndTag = combined.suffix(from: 12)
        return Data(ciphertextAndTag)
    }
    
    /**
     * Decrypt data using AES-256-GCM.
     * 
     * @param ciphertextWithTag Combined ciphertext and authentication tag
     * @param key 256-bit (32-byte) encryption key
     * @param nonce 96-bit (12-byte) nonce/IV used during encryption
     * @return Decrypted plaintext
     * @throws Error if decryption fails, authentication fails, or parameters are invalid
     */
    @objc public static func decryptAesGcm(
        ciphertextWithTag: Data,
        key: Data,
        nonce: Data
    ) throws -> Data {
        // Validate parameters
        guard key.count == 32 else {
            throw NSError(
                domain: "CryptoKitBridge",
                code: 1,
                userInfo: [NSLocalizedDescriptionKey: "Key must be 256 bits (32 bytes)"]
            )
        }
        
        guard nonce.count == 12 else {
            throw NSError(
                domain: "CryptoKitBridge",
                code: 2,
                userInfo: [NSLocalizedDescriptionKey: "Nonce must be 96 bits (12 bytes)"]
            )
        }
        
        guard ciphertextWithTag.count >= 16 else {
            throw NSError(
                domain: "CryptoKitBridge",
                code: 4,
                userInfo: [NSLocalizedDescriptionKey: "Ciphertext must include 16-byte authentication tag"]
            )
        }
        
        // Create symmetric key
        let symmetricKey = SymmetricKey(data: key)
        
        // Reconstruct combined format (nonce + ciphertext + tag)
        var combined = Data()
        combined.append(nonce)
        combined.append(ciphertextWithTag)
        
        // Create sealed box
        let sealedBox = try AES.GCM.SealedBox(combined: combined)
        
        // Decrypt and verify authentication tag
        let plaintext = try AES.GCM.open(sealedBox, using: symmetricKey)
        
        return plaintext
    }
}
