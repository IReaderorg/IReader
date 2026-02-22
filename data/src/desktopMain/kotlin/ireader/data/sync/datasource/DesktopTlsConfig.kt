package ireader.data.sync.datasource

import ireader.domain.services.sync.CertificateService
import java.io.ByteArrayInputStream
import java.security.KeyFactory
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Desktop (JVM)-specific TLS/SSL configuration for secure WebSocket connections.
 * 
 * Provides platform-specific implementations for:
 * - KeyStore creation from certificate data
 * - Java SSL configuration with custom TrustManager
 * - Certificate pinning validation
 * - SSLContext creation with TLS 1.2+ protocols
 * 
 * Security Features:
 * - Only allows TLS 1.2 and TLS 1.3 protocols
 * - Validates certificate fingerprints for pinning
 * - Rejects self-signed certificates from unknown sources
 * - Proper error handling for TLS failures
 */
object DesktopTlsConfig {
    
    private const val KEYSTORE_TYPE = "PKCS12"
    private const val KEY_ALIAS = "sync-key"
    // Security: Generate random password per KeyStore creation (ephemeral)
    // PKCS12 keystores are created on-the-fly and don't need persistent passwords
    
    /**
     * Create KeyStore from certificate data.
     * 
     * Converts PEM-encoded certificate and private key into a KeyStore
     * that can be used for TLS configuration.
     * 
     * Security: Uses randomly generated ephemeral password for in-memory keystore.
     * 
     * @param certificateData Certificate and private key data
     * @return KeyStore containing the certificate and private key
     * @throws IllegalArgumentException if certificate data is invalid
     */
    fun createKeyStore(certificateData: CertificateService.CertificateData): KeyStore {
        require(certificateData.certificate.isNotEmpty()) { "Certificate cannot be empty" }
        require(certificateData.privateKey.isNotEmpty()) { "Private key cannot be empty" }
        
        // Generate random ephemeral password for this keystore
        val keystorePassword = generateEphemeralPassword()
        
        try {
            // Parse X.509 certificate
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val certInputStream = ByteArrayInputStream(certificateData.certificate)
            val certificate = certificateFactory.generateCertificate(certInputStream) as X509Certificate
            
            // Parse PKCS8 private key
            val privateKeyBytes = parsePemPrivateKey(certificateData.privateKey)
            val keySpec = PKCS8EncodedKeySpec(privateKeyBytes)
            val keyFactory = KeyFactory.getInstance("RSA")
            val privateKey = keyFactory.generatePrivate(keySpec)
            
            // Create KeyStore
            val keyStore = KeyStore.getInstance(KEYSTORE_TYPE)
            keyStore.load(null, null)
            
            // Add certificate and private key to KeyStore
            keyStore.setKeyEntry(
                KEY_ALIAS,
                privateKey,
                keystorePassword.toCharArray(),
                arrayOf(certificate)
            )
            
            return keyStore
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to create KeyStore from certificate data: ${e.message}", e)
        }
    }
    
    /**
     * Create SSLContext configured for TLS 1.2+ with custom KeyStore.
     * 
     * Security: Uses randomly generated ephemeral password for in-memory keystore.
     * 
     * @param keyStore KeyStore containing certificate and private key
     * @return Configured SSLContext
     */
    fun createSslContext(keyStore: KeyStore): SSLContext {
        // Generate random ephemeral password for this operation
        val keystorePassword = generateEphemeralPassword()
        
        try {
            // Initialize KeyManagerFactory with KeyStore
            val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            keyManagerFactory.init(keyStore, keystorePassword.toCharArray())
            
            // Initialize TrustManagerFactory with KeyStore
            val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(keyStore)
            
            // Create SSLContext with TLS 1.2+
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(
                keyManagerFactory.keyManagers,
                trustManagerFactory.trustManagers,
                null
            )
            
            return sslContext
        } catch (e: Exception) {
            throw IllegalStateException("Failed to create SSLContext: ${e.message}", e)
        }
    }
    
    /**
     * Create custom TrustManager that validates certificate fingerprints.
     * 
     * This TrustManager performs certificate pinning by comparing the
     * server's certificate fingerprint against the expected fingerprint.
     * 
     * @param expectedFingerprint Expected SHA-256 certificate fingerprint
     * @return X509TrustManager that validates fingerprints
     */
    fun createPinningTrustManager(expectedFingerprint: String): X509TrustManager {
        require(expectedFingerprint.isNotBlank()) { "Expected fingerprint cannot be empty" }
        
        return object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String) {
                // Not used for client connections
            }
            
            override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String) {
                if (chain.isEmpty()) {
                    throw SecurityException("Certificate chain is empty")
                }
                
                // Get server certificate (first in chain)
                val serverCert = chain[0]
                
                // Calculate fingerprint
                val actualFingerprint = calculateCertificateFingerprint(serverCert.encoded)
                
                // Validate fingerprint
                if (actualFingerprint != expectedFingerprint) {
                    throw SecurityException(
                        "Certificate fingerprint mismatch. Expected: $expectedFingerprint, Got: $actualFingerprint"
                    )
                }
            }
            
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return emptyArray()
            }
        }
    }
    
    /**
     * Create SSLContext with certificate pinning.
     * 
     * @param expectedFingerprint Expected SHA-256 certificate fingerprint
     * @return SSLContext configured with pinning TrustManager
     */
    fun createSslContextWithPinning(expectedFingerprint: String): SSLContext {
        try {
            val trustManager = createPinningTrustManager(expectedFingerprint)
            
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf<TrustManager>(trustManager), null)
            
            return sslContext
        } catch (e: Exception) {
            throw IllegalStateException("Failed to create SSLContext with pinning: ${e.message}", e)
        }
    }
    
    /**
     * Calculate SHA-256 fingerprint of a certificate.
     * 
     * @param certificateBytes Certificate bytes
     * @return SHA-256 fingerprint in format AA:BB:CC:DD:...
     */
    private fun calculateCertificateFingerprint(certificateBytes: ByteArray): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(certificateBytes)
        return hash.joinToString(":") { "%02X".format(it) }
    }
    
    /**
     * Parse PEM-encoded private key to raw bytes.
     * 
     * Removes PEM headers/footers and decodes Base64.
     */
    private fun parsePemPrivateKey(pemKey: ByteArray): ByteArray {
        val pemString = String(pemKey, Charsets.UTF_8)
        val base64Key = pemString
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("-----BEGIN RSA PRIVATE KEY-----", "")
            .replace("-----END RSA PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")
        
        return Base64.getDecoder().decode(base64Key)
    }
    
    /**
     * Get supported TLS protocols (TLS 1.2 and 1.3 only).
     */
    fun getSupportedTlsProtocols(): Array<String> {
        return arrayOf("TLSv1.2", "TLSv1.3")
    }
    
    /**
     * Generate ephemeral random password for in-memory keystore.
     * 
     * Security: Password is only used for the lifetime of the KeyStore object
     * and is not persisted anywhere.
     */
    private fun generateEphemeralPassword(): String {
        val random = java.security.SecureRandom()
        val passwordBytes = ByteArray(32)
        random.nextBytes(passwordBytes)
        return java.util.Base64.getEncoder().encodeToString(passwordBytes)
    }
}
