package ireader.data.sync.datasource

import ireader.domain.services.sync.CertificateService
import javax.net.ssl.SSLContext

/**
 * Desktop (JVM)-specific TLS configuration for KtorTransferDataSource.
 * 
 * Provides actual implementations of expect functions for TLS/SSL configuration
 * on Desktop platform using Java SSL APIs.
 */

/**
 * Configure TLS for server on Desktop.
 * 
 * Creates an SSLContext from certificate data that can be used to configure
 * a secure WebSocket server.
 * 
 * @param certificateData Certificate and private key for TLS
 * @return SSLContext configured with the certificate
 */
internal actual fun KtorTransferDataSource.configureTlsServer(
    certificateData: CertificateService.CertificateData
): Any {
    // Create KeyStore from certificate data
    val keyStore = DesktopTlsConfig.createKeyStore(certificateData)
    
    // Create SSLContext with KeyStore
    val sslContext = DesktopTlsConfig.createSslContext(keyStore)
    
    return sslContext
}

/**
 * Configure TLS for client on Desktop with certificate pinning.
 * 
 * Creates an SSLContext with a custom TrustManager that validates
 * certificate fingerprints for pinning.
 * 
 * @param certificateFingerprint Expected SHA-256 certificate fingerprint
 * @return SSLContext configured with certificate pinning
 */
internal actual fun KtorTransferDataSource.configureTlsClient(
    certificateFingerprint: String
): Any {
    // Create SSLContext with certificate pinning
    val sslContext = DesktopTlsConfig.createSslContextWithPinning(certificateFingerprint)
    
    return sslContext
}
