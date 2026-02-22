package ireader.data.sync.datasource

import ireader.domain.services.sync.CertificateService
import javax.net.ssl.SSLContext

/**
 * Android-specific TLS configuration for KtorTransferDataSource.
 * 
 * Provides actual implementations of expect functions for TLS/SSL configuration
 * on Android platform using Android-specific APIs and OkHttp.
 */

/**
 * Configure TLS for server on Android.
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
    val keyStore = AndroidTlsConfig.createKeyStore(certificateData)
    
    // Create SSLContext with KeyStore
    val sslContext = AndroidTlsConfig.createSslContext(keyStore)
    
    return sslContext
}

/**
 * Configure TLS for client on Android with certificate pinning.
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
    // Create TrustManager that validates fingerprints
    val trustManager = AndroidTlsConfig.createPinningTrustManager(certificateFingerprint)
    
    // Create SSLContext with pinning TrustManager
    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, arrayOf(trustManager), null)
    
    return sslContext
}
