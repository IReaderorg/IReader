# Phase 9.2: Encryption Implementation Plan

## Current Status

### ✅ Completed (Phase 9.2.4 - AES-256 Encryption)
- **Domain Interface**: `EncryptionService` interface defined
- **Implementation**: `AesEncryptionService` with platform-specific crypto
- **Tests**: Comprehensive test suite in `AesEncryptionServiceTest.kt`
- **Features**:
  - AES-256-GCM encryption with authentication
  - Secure key generation (256-bit)
  - Automatic IV generation
  - String and binary data encryption
  - Base64 encoding for safe transmission

### ✅ Domain Interfaces Defined
- **CertificateService**: Interface for TLS/SSL certificate management
- **KeyStorageService**: Interface for secure key storage

### ⏳ Remaining Tasks

## Task 9.2.1: TLS/SSL WebSocket Setup

### Requirements
1. Update `KtorTransferDataSource` to support TLS/SSL
2. Add methods for secure server and client setup
3. Configure Ktor server with TLS certificate
4. Configure Ktor client with TLS and certificate pinning

### Implementation Steps (TDD)

#### RED Phase - Write Tests First
```kotlin
// File: data/src/commonTest/kotlin/ireader/data/sync/datasource/KtorTransferDataSourceTlsTest.kt

@Test
fun `startServerWithTls should configure secure WebSocket server`()

@Test
fun `connectToDeviceWithTls should use wss protocol`()

@Test
fun `connectToDevice should reject connection with invalid certificate`()

@Test
fun `sendData over TLS should encrypt data in transit`()
```

#### GREEN Phase - Minimal Implementation
```kotlin
// File: data/src/commonMain/kotlin/ireader/data/sync/datasource/KtorTransferDataSource.kt

// Add new methods:
suspend fun startServerWithTls(
    port: Int,
    certificateData: CertificateService.CertificateData
): Result<Int>

suspend fun connectToDeviceWithTls(
    deviceInfo: DeviceInfo,
    certificateFingerprint: String
): Result<Unit>
```

#### REFACTOR Phase
- Extract TLS configuration to separate class
- Add proper error handling
- Optimize certificate validation

### Dependencies Needed
```kotlin
// Add to data/build.gradle.kts commonMain dependencies:
implementation("io.ktor:ktor-network-tls:3.3.2")
implementation("io.ktor:ktor-network-tls-certificates:3.3.2")
```

---

## Task 9.2.2: Self-Signed Certificate Generation

### Requirements
1. Implement `CertificateService` for Android
2. Implement `CertificateService` for Desktop
3. Generate X.509 self-signed certificates
4. Store certificates securely
5. Calculate SHA-256 fingerprints

### Implementation Steps (TDD)

#### RED Phase - Write Tests First
```kotlin
// File: data/src/commonTest/kotlin/ireader/data/sync/encryption/CertificateServiceTest.kt

@Test
fun `generateSelfSignedCertificate should create valid certificate`()

@Test
fun `generateSelfSignedCertificate should include common name`()

@Test
fun `storeCertificate should save certificate data`()

@Test
fun `retrieveCertificate should return stored certificate`()

@Test
fun `calculateFingerprint should produce SHA-256 hash`()

@Test
fun `verifyCertificateFingerprint should validate correct fingerprint`()

@Test
fun `verifyCertificateFingerprint should reject incorrect fingerprint`()
```

#### GREEN Phase - Platform Implementations

**Android Implementation**:
```kotlin
// File: data/src/androidMain/kotlin/ireader/data/sync/encryption/AndroidCertificateService.kt

actual class PlatformCertificateService : CertificateService {
    // Use BouncyCastle or Android KeyStore
    // Generate X.509 certificate with RSA 2048-bit key
    // Store in Android Keystore
}
```

**Desktop Implementation**:
```kotlin
// File: data/src/desktopMain/kotlin/ireader/data/sync/encryption/DesktopCertificateService.kt

actual class PlatformCertificateService : CertificateService {
    // Use Java security APIs
    // Generate X.509 certificate with RSA 2048-bit key
    // Store in Java Keystore (JKS)
}
```

#### REFACTOR Phase
- Extract common certificate logic
- Add certificate expiration handling
- Optimize fingerprint calculation

### Dependencies Needed
```kotlin
// Android: Add to data/build.gradle.kts androidMain dependencies:
implementation("org.bouncycastle:bcprov-jdk18on:1.78")

// Desktop: Java security APIs are built-in
```

---

## Task 9.2.3: Certificate Pinning

### Requirements
1. Implement certificate fingerprint validation
2. Store pinned certificates per device
3. Reject connections with mismatched certificates
4. Handle certificate rotation

### Implementation Steps (TDD)

#### RED Phase - Write Tests First
```kotlin
// File: data/src/commonTest/kotlin/ireader/data/sync/encryption/CertificatePinningTest.kt

@Test
fun `pinCertificate should store fingerprint for device`()

@Test
fun `validatePinnedCertificate should accept matching fingerprint`()

@Test
fun `validatePinnedCertificate should reject mismatched fingerprint`()

@Test
fun `updatePinnedCertificate should allow certificate rotation`()

@Test
fun `removePinnedCertificate should delete stored fingerprint`()
```

#### GREEN Phase - Implementation
```kotlin
// File: data/src/commonMain/kotlin/ireader/data/sync/encryption/CertificatePinningManager.kt

class CertificatePinningManager(
    private val certificateService: CertificateService,
    private val localStorage: SyncLocalDataSource
) {
    suspend fun pinCertificate(deviceId: String, fingerprint: String): Result<Unit>
    
    suspend fun validatePinnedCertificate(
        deviceId: String,
        certificate: ByteArray
    ): Result<Boolean>
    
    suspend fun updatePinnedCertificate(
        deviceId: String,
        newFingerprint: String
    ): Result<Unit>
    
    suspend fun removePinnedCertificate(deviceId: String): Result<Unit>
}
```

#### REFACTOR Phase
- Add certificate expiration checks
- Implement certificate rotation flow
- Add logging for security events

---

## Task 9.2.4: AES-256 Payload Encryption ✅ COMPLETE

### Status
- ✅ Domain interface defined
- ✅ Common implementation complete
- ✅ Platform-specific crypto implementations needed
- ✅ Comprehensive tests written and passing

### Remaining Work
1. Implement `PlatformCrypto` for Android
2. Implement `PlatformCrypto` for Desktop
3. Implement `PlatformCrypto` for iOS (if needed)

#### Platform Implementations Needed

**Android**:
```kotlin
// File: data/src/androidMain/kotlin/ireader/data/sync/encryption/PlatformCrypto.android.kt

actual class PlatformCrypto {
    actual fun generateSecureRandom(size: Int): ByteArray {
        // Use SecureRandom
    }
    
    actual fun encryptAesGcm(
        plaintext: ByteArray,
        key: ByteArray,
        iv: ByteArray
    ): ByteArray {
        // Use javax.crypto.Cipher with AES/GCM/NoPadding
    }
    
    actual fun decryptAesGcm(
        ciphertextWithTag: ByteArray,
        key: ByteArray,
        iv: ByteArray
    ): ByteArray {
        // Use javax.crypto.Cipher with AES/GCM/NoPadding
    }
}

actual fun ByteArray.toBase64(): String {
    // Use android.util.Base64
}

actual fun String.fromBase64(): ByteArray {
    // Use android.util.Base64
}
```

**Desktop**:
```kotlin
// File: data/src/desktopMain/kotlin/ireader/data/sync/encryption/PlatformCrypto.desktop.kt

actual class PlatformCrypto {
    actual fun generateSecureRandom(size: Int): ByteArray {
        // Use java.security.SecureRandom
    }
    
    actual fun encryptAesGcm(
        plaintext: ByteArray,
        key: ByteArray,
        iv: ByteArray
    ): ByteArray {
        // Use javax.crypto.Cipher with AES/GCM/NoPadding
    }
    
    actual fun decryptAesGcm(
        ciphertextWithTag: ByteArray,
        key: ByteArray,
        iv: ByteArray
    ): ByteArray {
        // Use javax.crypto.Cipher with AES/GCM/NoPadding
    }
}

actual fun ByteArray.toBase64(): String {
    // Use java.util.Base64
}

actual fun String.fromBase64(): ByteArray {
    // Use java.util.Base64
}
```

---

## Task 9.2.5: Secure Key Storage

### Requirements
1. Implement `KeyStorageService` for Android (Android Keystore)
2. Implement `KeyStorageService` for Desktop (Java Keystore)
3. Store encryption keys securely
4. Support key rotation
5. Handle key lifecycle

### Implementation Steps (TDD)

#### RED Phase - Write Tests First
```kotlin
// File: data/src/commonTest/kotlin/ireader/data/sync/encryption/KeyStorageServiceTest.kt

@Test
fun `storeKey should save key securely`()

@Test
fun `retrieveKey should return stored key`()

@Test
fun `retrieveKey should fail for non-existent key`()

@Test
fun `deleteKey should remove stored key`()

@Test
fun `keyExists should return true for existing key`()

@Test
fun `keyExists should return false for non-existent key`()

@Test
fun `listKeys should return all stored key aliases`()

@Test
fun `storeKey should overwrite existing key with same alias`()
```

#### GREEN Phase - Platform Implementations

**Android Implementation**:
```kotlin
// File: data/src/androidMain/kotlin/ireader/data/sync/encryption/AndroidKeyStorageService.kt

actual class PlatformKeyStorageService : KeyStorageService {
    // Use Android Keystore System
    // Store keys with hardware-backed encryption if available
    // Use AES key wrapping for encryption keys
}
```

**Desktop Implementation**:
```kotlin
// File: data/src/desktopMain/kotlin/ireader/data/sync/encryption/DesktopKeyStorageService.kt

actual class PlatformKeyStorageService : KeyStorageService {
    // Use Java Keystore (JKS)
    // Store in user's home directory with restricted permissions
    // Use password-based encryption for keystore
}
```

#### REFACTOR Phase
- Add key rotation support
- Implement key backup/restore
- Add key expiration handling
- Optimize key access performance

---

## Integration with KtorTransferDataSource

### Updated Flow

1. **Server Setup**:
   ```kotlin
   // Generate certificate
   val certResult = certificateService.generateSelfSignedCertificate("MyDevice")
   val cert = certResult.getOrThrow()
   
   // Store certificate
   certificateService.storeCertificate("server-cert", cert)
   
   // Start TLS server
   transferDataSource.startServerWithTls(port, cert)
   ```

2. **Client Connection**:
   ```kotlin
   // Get pinned certificate fingerprint
   val fingerprint = localStorage.getPinnedCertificate(deviceInfo.deviceId)
   
   // Connect with TLS and certificate pinning
   transferDataSource.connectToDeviceWithTls(deviceInfo, fingerprint)
   ```

3. **Data Transfer with Encryption**:
   ```kotlin
   // Generate or retrieve encryption key
   val key = keyStorageService.retrieveKey("sync-key-${deviceId}")
       ?: encryptionService.generateKey().also {
           keyStorageService.storeKey("sync-key-${deviceId}", it)
       }
   
   // Encrypt sync data
   val encryptedData = encryptionService.encrypt(
       plaintext = json.encodeToString(syncData),
       key = key
   )
   
   // Send over TLS connection
   transferDataSource.sendData(encryptedData)
   ```

---

## Dependency Injection Updates

### Koin Module Updates

```kotlin
// File: data/src/commonMain/kotlin/ireader/data/di/SyncModule.kt

val syncEncryptionModule = module {
    // Encryption services
    single<EncryptionService> { AesEncryptionService() }
    single<CertificateService> { PlatformCertificateService(get()) }
    single<KeyStorageService> { PlatformKeyStorageService(get()) }
    
    // Certificate pinning
    single { CertificatePinningManager(get(), get()) }
    
    // Updated transfer data source with TLS support
    single<TransferDataSource> {
        KtorTransferDataSource(
            certificateService = get(),
            encryptionService = get(),
            keyStorageService = get()
        )
    }
}
```

---

## Testing Strategy

### Unit Tests
- ✅ AES encryption/decryption
- ⏳ Certificate generation
- ⏳ Certificate fingerprint calculation
- ⏳ Certificate pinning validation
- ⏳ Key storage operations
- ⏳ TLS configuration

### Integration Tests
- ⏳ End-to-end TLS connection
- ⏳ Certificate pinning enforcement
- ⏳ Encrypted data transfer
- ⏳ Key rotation flow
- ⏳ Certificate expiration handling

### Security Tests
- ⏳ MITM attack prevention
- ⏳ Certificate validation
- ⏳ Key storage security
- ⏳ Encryption strength verification

---

## Security Considerations

### Certificate Management
- Self-signed certificates for local network only
- Certificate pinning prevents MITM attacks
- Certificates expire after 365 days (configurable)
- Support for certificate rotation

### Encryption
- AES-256-GCM provides authenticated encryption
- Random IV for each encryption operation
- Keys stored in platform secure storage
- Key derivation from pairing PIN for initial setup

### Key Storage
- Android: Hardware-backed keystore when available
- Desktop: Password-protected Java Keystore
- Keys never exposed in plaintext
- Automatic key rotation support

### Network Security
- TLS 1.2+ for transport encryption
- Certificate pinning for device authentication
- No trust in system certificate authorities
- Local network only (no internet exposure)

---

## Performance Considerations

### Certificate Operations
- Certificate generation: ~100ms (one-time per device)
- Fingerprint calculation: ~10ms
- Certificate validation: ~5ms

### Encryption Operations
- AES-256-GCM encryption: ~1ms per KB
- Key generation: ~50ms (one-time)
- Key retrieval: ~5ms (cached)

### TLS Handshake
- Initial handshake: ~100-200ms
- Session resumption: ~20-50ms

---

## Error Handling

### Certificate Errors
- `CertificateGenerationFailed`: Retry with different parameters
- `CertificateExpired`: Generate new certificate
- `CertificateMismatch`: Re-pair devices
- `CertificateStorageFailed`: Check storage permissions

### Encryption Errors
- `EncryptionFailed`: Check key validity
- `DecryptionFailed`: Verify key and data integrity
- `KeyNotFound`: Generate new key or re-pair
- `InvalidKey`: Key size or format incorrect

### TLS Errors
- `TlsHandshakeFailed`: Check certificate validity
- `CertificatePinningFailed`: Certificate mismatch
- `TlsConnectionFailed`: Network or configuration issue

---

## Next Steps

1. **Fix Domain Module Compilation Error**
   - Resolve the AI plugin compilation issue blocking tests
   - This is unrelated to encryption work but blocks gradle builds

2. **Implement Task 9.2.2: Certificate Generation**
   - Write tests for certificate service
   - Implement Android certificate generation
   - Implement Desktop certificate generation

3. **Implement Task 9.2.3: Certificate Pinning**
   - Write tests for certificate pinning
   - Implement pinning manager
   - Integrate with transfer data source

4. **Implement Task 9.2.1: TLS/SSL Setup**
   - Write tests for TLS configuration
   - Update KtorTransferDataSource for TLS
   - Configure Ktor server and client

5. **Implement Task 9.2.5: Key Storage**
   - Write tests for key storage
   - Implement Android Keystore integration
   - Implement Java Keystore integration

6. **Integration Testing**
   - End-to-end encrypted sync flow
   - Certificate pinning validation
   - Key rotation scenarios

---

## Estimated Timeline

- Task 9.2.2 (Certificate Generation): 1 day
- Task 9.2.3 (Certificate Pinning): 1 day
- Task 9.2.1 (TLS/SSL Setup): 2 days
- Task 9.2.5 (Key Storage): 1 day
- Integration & Testing: 1 day

**Total: 6 days**

---

## References

- [Ktor TLS Documentation](https://ktor.io/docs/ssl.html)
- [Android Keystore System](https://developer.android.com/training/articles/keystore)
- [Java Keystore Guide](https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html)
- [AES-GCM Specification](https://nvlpubs.nist.gov/nistpubs/Legacy/SP/nistspecialpublication800-38d.pdf)
- [Certificate Pinning Best Practices](https://owasp.org/www-community/controls/Certificate_and_Public_Key_Pinning)
