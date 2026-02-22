package ireader.domain.services.sync

/**
 * Service for managing TLS/SSL certificates for secure WebSocket connections.
 * 
 * Task 9.2.2: Self-signed certificate generation
 * Task 9.2.3: Certificate pinning
 * 
 * Generates self-signed certificates for local network communication
 * and implements certificate pinning to prevent MITM attacks.
 */
interface CertificateService {
    
    /**
     * Certificate data containing the certificate and private key.
     */
    data class CertificateData(
        val certificate: ByteArray,
        val privateKey: ByteArray,
        val publicKey: ByteArray,
        val fingerprint: String
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            
            other as CertificateData
            
            if (!certificate.contentEquals(other.certificate)) return false
            if (!privateKey.contentEquals(other.privateKey)) return false
            if (!publicKey.contentEquals(other.publicKey)) return false
            if (fingerprint != other.fingerprint) return false
            
            return true
        }
        
        override fun hashCode(): Int {
            var result = certificate.contentHashCode()
            result = 31 * result + privateKey.contentHashCode()
            result = 31 * result + publicKey.contentHashCode()
            result = 31 * result + fingerprint.hashCode()
            return result
        }
    }
    
    /**
     * Generate a self-signed certificate for local network use.
     * 
     * @param commonName Common Name (CN) for the certificate (e.g., device name)
     * @param validityDays Number of days the certificate is valid
     * @return CertificateData containing certificate, keys, and fingerprint
     */
    suspend fun generateSelfSignedCertificate(
        commonName: String,
        validityDays: Int = 365
    ): Result<CertificateData>
    
    /**
     * Store certificate data securely.
     * 
     * @param alias Unique identifier for the certificate
     * @param certificateData Certificate data to store
     * @return Result indicating success or failure
     */
    suspend fun storeCertificate(
        alias: String,
        certificateData: CertificateData
    ): Result<Unit>
    
    /**
     * Retrieve stored certificate data.
     * 
     * @param alias Unique identifier for the certificate
     * @return Result containing certificate data or failure
     */
    suspend fun retrieveCertificate(alias: String): Result<CertificateData>
    
    /**
     * Verify certificate fingerprint for certificate pinning.
     * 
     * @param certificate Certificate bytes to verify
     * @param expectedFingerprint Expected SHA-256 fingerprint
     * @return true if fingerprint matches, false otherwise
     */
    fun verifyCertificateFingerprint(
        certificate: ByteArray,
        expectedFingerprint: String
    ): Boolean
    
    /**
     * Calculate SHA-256 fingerprint of a certificate.
     * 
     * @param certificate Certificate bytes
     * @return SHA-256 fingerprint as hex string
     */
    fun calculateFingerprint(certificate: ByteArray): String
    
    /**
     * Delete stored certificate.
     * 
     * @param alias Unique identifier for the certificate
     * @return Result indicating success or failure
     */
    suspend fun deleteCertificate(alias: String): Result<Unit>
    
    /**
     * Check if certificate exists.
     * 
     * @param alias Unique identifier for the certificate
     * @return true if certificate exists, false otherwise
     */
    suspend fun certificateExists(alias: String): Boolean
}
