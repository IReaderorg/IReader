# TLS/SSL Implementation for Local Sync

## Overview

This document describes the platform-specific TLS/SSL configuration for secure WebSocket connections in the IReader local sync feature.

## Architecture

### Platform-Specific Implementations

The TLS configuration is split into platform-specific modules:

1. **Android** (`AndroidTlsConfig.kt`)
   - Uses Android KeyStore and OkHttp
   - Implements certificate pinning with `CertificatePinner`
   - Supports TLS 1.2 and 1.3

2. **Desktop** (`DesktopTlsConfig.kt`)
   - Uses Java KeyStore (JKS)
   - Implements custom `TrustManager` for pinning
   - Supports TLS 1.2 and 1.3

### Key Components

#### 1. KeyStore Creation

Both platforms convert PEM-encoded certificates and private keys into platform-specific KeyStores:

```kotlin
// Android & Desktop
fun createKeyStore(certificateData: CertificateService.CertificateData): KeyStore
```

**Process:**
1. Parse X.509 certificate from PEM format
2. Parse PKCS8 private key from PEM format
3. Create PKCS12 KeyStore
4. Add certificate and private key to KeyStore

#### 2. SSLContext Configuration

Creates SSLContext with TLS 1.2+ protocols:

```kotlin
// Android & Desktop
fun createSslContext(keyStore: KeyStore): SSLContext
```

**Features:**
- Uses KeyManagerFactory for certificate management
- Uses TrustManagerFactory for certificate validation
- Configures TLS protocol (supports 1.2 and 1.3)

#### 3. Certificate Pinning

Implements certificate pinning to prevent MITM attacks:

**Android:**
```kotlin
fun configureOkHttpWithPinning(host: String, fingerprint: String): OkHttpClient
```
- Uses OkHttp's `CertificatePinner`
- Validates SHA-256 fingerprints

**Desktop:**
```kotlin
fun createPinningTrustManager(expectedFingerprint: String): X509TrustManager
```
- Custom `X509TrustManager` implementation
- Validates certificate fingerprints during TLS handshake

#### 4. Platform-Specific Extensions

Expect/actual pattern for platform-specific TLS configuration:

```kotlin
// Common (expect)
internal expect fun KtorTransferDataSource.configureTlsServer(
    certificateData: CertificateService.CertificateData
): Any

internal expect fun KtorTransferDataSource.configureTlsClient(
    certificateFingerprint: String
): Any

// Android/Desktop (actual)
internal actual fun KtorTransferDataSource.configureTlsServer(...): Any
internal actual fun KtorTransferDataSource.configureTlsClient(...): Any
```

## Security Features

### 1. TLS Protocol Restrictions

- **Supported:** TLS 1.2, TLS 1.3
- **Rejected:** TLS 1.0, TLS 1.1, SSLv3 (deprecated and insecure)

### 2. Certificate Validation

- Validates certificate fingerprints (SHA-256)
- Rejects self-signed certificates from unknown sources
- Enforces certificate pinning for known devices

### 3. Error Handling

Proper error handling for TLS failures:
- `IllegalArgumentException` - Invalid certificate data
- `IllegalStateException` - Server/client state errors
- `SecurityException` - Certificate validation failures

## Usage

### Server Side (TLS)

```kotlin
val dataSource = KtorTransferDataSource(certificateService)

// Generate or retrieve certificate
val certData = certificateService.generateSelfSignedCertificate("MyDevice")

// Start TLS server
val result = dataSource.startServerWithTls(
    port = 8443,
    certificateData = certData.getOrThrow()
)
```

### Client Side (TLS with Pinning)

```kotlin
val dataSource = KtorTransferDataSource(
    certificateService,
    certificatePinningManager
)

// Get device info with certificate fingerprint
val deviceInfo = DeviceInfo(
    deviceId = "device-123",
    deviceName = "Other Device",
    ipAddress = "192.168.1.100",
    port = 8443,
    platform = "Android"
)

// Connect with certificate pinning
val result = dataSource.connectToDeviceWithTls(
    deviceInfo = deviceInfo,
    certificateFingerprint = "AA:BB:CC:DD:..." // SHA-256 fingerprint
)
```

## Testing

### Unit Tests

1. **Certificate Validation Tests** (`TlsConfigurationTest.kt`)
   - Validates certificate data format
   - Validates fingerprint format
   - Validates TLS protocol support

2. **Platform-Specific Tests**
   - `AndroidTlsConfigTest.kt` - Android KeyStore and OkHttp
   - `DesktopTlsConfigTest.kt` - Java KeyStore and TrustManager

3. **Integration Tests** (`KtorTransferDataSourceTlsTest.kt`)
   - Tests TLS server configuration
   - Tests TLS client configuration
   - Tests certificate pinning enforcement

### Test Coverage

- ✅ Empty certificate validation
- ✅ Empty private key validation
- ✅ Empty fingerprint validation
- ✅ Invalid fingerprint format
- ✅ TLS protocol restrictions
- ✅ Certificate pinning validation
- ✅ Server state management
- ✅ Client state management

## Implementation Notes

### PEM Format

Certificates and private keys are expected in PEM format:

```
-----BEGIN CERTIFICATE-----
MIICljCCAX4CCQCKz8Qr8vN8pDANBgkqhkiG9w0BAQsFADANMQswCQYDVQQGEwJV
...
-----END CERTIFICATE-----

-----BEGIN PRIVATE KEY-----
MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCtest
...
-----END PRIVATE KEY-----
```

### Fingerprint Format

SHA-256 fingerprints are in colon-separated hex format:

```
AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99
```

### Platform Differences

| Feature | Android | Desktop |
|---------|---------|---------|
| KeyStore Type | PKCS12 | PKCS12 |
| Certificate Pinning | OkHttp CertificatePinner | Custom TrustManager |
| Base64 Decoding | android.util.Base64 | java.util.Base64 |
| SSL Provider | Android Security | Java Security |

## Future Enhancements

1. **iOS Support**
   - Implement using Security framework
   - Use URLSession for certificate pinning

2. **Certificate Rotation**
   - Support for certificate expiration
   - Automatic certificate renewal

3. **Enhanced Validation**
   - Certificate chain validation
   - Certificate revocation checking (CRL/OCSP)

4. **Performance Optimization**
   - KeyStore caching
   - SSLContext reuse

## References

- [RFC 5246 - TLS 1.2](https://tools.ietf.org/html/rfc5246)
- [RFC 8446 - TLS 1.3](https://tools.ietf.org/html/rfc8446)
- [OWASP Certificate Pinning](https://owasp.org/www-community/controls/Certificate_and_Public_Key_Pinning)
- [Android Network Security](https://developer.android.com/training/articles/security-ssl)
- [Java SSL/TLS](https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html)
