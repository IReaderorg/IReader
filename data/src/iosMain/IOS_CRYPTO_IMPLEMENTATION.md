# iOS AES-256-GCM Encryption Implementation Guide

## Overview

iOS AES-256-GCM encryption requires CryptoKit (iOS 13+), which is not directly accessible from Kotlin/Native. This guide explains how to implement it using a Swift bridge.

## Current Status

✅ **Implemented:**
- Secure random number generation using `SecRandomCopyBytes`
- Error checking for random generation
- Base64 encoding/decoding
- Input validation for encryption parameters

❌ **Not Implemented:**
- AES-256-GCM encryption (requires Swift bridge)
- AES-256-GCM decryption (requires Swift bridge)

## Why Swift Bridge is Needed

CryptoKit is a Swift-only framework that provides:
- Hardware-accelerated AES-GCM encryption
- Proper authentication tag handling
- Secure key management
- iOS platform integration

Kotlin/Native cannot directly call Swift APIs, so we need an Objective-C compatible bridge.

## Implementation Steps

### Step 1: Add Swift Bridge File

The Swift bridge file `CryptoKitBridge.swift` is provided in `data/src/iosMain/swift/`.

Add it to your Xcode project:

1. Open your iOS project in Xcode
2. Add `CryptoKitBridge.swift` to your target
3. Ensure it's included in the framework that Kotlin/Native links against

### Step 2: Configure Kotlin/Native to Use Swift Bridge

In your `build.gradle.kts` for the iOS target:

```kotlin
kotlin {
    iosX64 {
        binaries {
            framework {
                // Ensure Swift bridge is accessible
                export(project(":your-swift-bridge-module"))
            }
        }
    }
    
    iosArm64 {
        binaries {
            framework {
                export(project(":your-swift-bridge-module"))
            }
        }
    }
    
    iosSimulatorArm64 {
        binaries {
            framework {
                export(project(":your-swift-bridge-module"))
            }
        }
    }
}
```

### Step 3: Update PlatformCrypto.ios.kt

Replace the `NotImplementedError` implementations with actual calls to the Swift bridge:

```kotlin
actual fun encryptAesGcm(plaintext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
    require(key.size == 32) { "Key must be 256 bits (32 bytes)" }
    require(iv.size == 12) { "IV must be 96 bits (12 bytes) for GCM" }
    
    try {
        val plaintextData = plaintext.toNSData()
        val keyData = key.toNSData()
        val nonceData = iv.toNSData()
        
        val ciphertextWithTag = CryptoKitBridge.encryptAesGcm(
            plaintext = plaintextData,
            key = keyData,
            nonce = nonceData
        )
        
        return ciphertextWithTag.toByteArray()
    } catch (e: Exception) {
        throw SecurityException("iOS AES-GCM encryption failed: ${e.message}", e)
    }
}

actual fun decryptAesGcm(ciphertextWithTag: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
    require(key.size == 32) { "Key must be 256 bits (32 bytes)" }
    require(iv.size == 12) { "IV must be 96 bits (12 bytes) for GCM" }
    require(ciphertextWithTag.size >= 16) { "Ciphertext must include 16-byte authentication tag" }
    
    try {
        val ciphertextData = ciphertextWithTag.toNSData()
        val keyData = key.toNSData()
        val nonceData = iv.toNSData()
        
        val plaintext = CryptoKitBridge.decryptAesGcm(
            ciphertextWithTag = ciphertextData,
            key = keyData,
            nonce = nonceData
        )
        
        return plaintext.toByteArray()
    } catch (e: Exception) {
        throw SecurityException("iOS AES-GCM decryption failed: ${e.message}", e)
    }
}
```

### Step 4: Test the Implementation

Run the tests in `AesGcmEncryptionTest.kt`:

```bash
./gradlew :data:iosX64Test
```

All tests should pass once the Swift bridge is properly integrated.

## Alternative: Use libsodium

If you prefer a pure Kotlin/Native solution without Swift bridges, consider using libsodium:

1. Add libsodium dependency:
```kotlin
kotlin {
    iosX64 {
        compilations.getByName("main") {
            cinterops {
                val libsodium by creating {
                    defFile(project.file("src/nativeInterop/cinterop/libsodium.def"))
                }
            }
        }
    }
}
```

2. Use libsodium's `crypto_aead_aes256gcm_*` functions for AES-256-GCM

Pros:
- Pure Kotlin/Native solution
- Cross-platform (works on all platforms)
- Well-tested cryptographic library

Cons:
- Additional dependency
- Larger binary size
- May not use hardware acceleration on iOS

## Security Considerations

1. **Key Management**: Never hardcode encryption keys. Use KeyStorageService.
2. **IV/Nonce**: Always use unique IVs for each encryption operation. Never reuse IVs with the same key.
3. **Authentication**: AES-GCM provides authenticated encryption. Always verify the authentication tag during decryption.
4. **Error Handling**: Don't expose detailed error messages that could leak information about keys or plaintexts.

## Testing

The following tests verify the implementation:

- `PlatformCryptoTest.kt` - Tests random generation
- `AesGcmEncryptionTest.kt` - Tests encryption/decryption roundtrip
- `CertificateServiceTest.kt` - Tests certificate operations

Run all tests:
```bash
./gradlew :data:iosX64Test
```

## Troubleshooting

### "CryptoKitBridge not found"

Ensure the Swift file is:
1. Added to your Xcode project
2. Included in the correct target
3. Marked as `@objc public`

### "Module 'CryptoKit' not found"

CryptoKit requires iOS 13+. Update your deployment target:

```swift
// In your Xcode project settings
iOS Deployment Target: 13.0 or higher
```

### Tests fail with NotImplementedError

The Swift bridge hasn't been integrated yet. Follow Steps 1-3 above.

## References

- [Apple CryptoKit Documentation](https://developer.apple.com/documentation/cryptokit)
- [AES-GCM Specification (NIST SP 800-38D)](https://csrc.nist.gov/publications/detail/sp/800-38d/final)
- [Kotlin/Native Interop](https://kotlinlang.org/docs/native-c-interop.html)
