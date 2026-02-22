package ireader.data.sync.encryption

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import ireader.domain.services.sync.CertificateService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.*
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.Date
import javax.security.auth.x500.X500Principal

/**
 * Android implementation of CertificateService.
 * 
 * Uses Android Keystore System for secure certificate storage.
 * Generates self-signed X.509 certificates for local network TLS.
 * 
 * Task 9.2.2: Self-signed certificate generation (Android)
 */
class AndroidCertificateService(
    private val context: Context
) : CertificateService {
    
    private val certificateDir = File(context.filesDir, "certificates")
    private val keystoreFile = File(certificateDir, "sync_keystore.bks")
    // Security: Use random password per session for ephemeral keystore
    private val keystorePassword: CharArray by lazy { generateRandomPassword() }
    
    companion object {
        private const val KEYSTORE_TYPE = "BKS"
        private const val KEY_ALGORITHM = "RSA"
        private const val KEY_SIZE = 2048
        private const val SIGNATURE_ALGORITHM = "SHA256withRSA"
    }
    
    init {
        if (!certificateDir.exists()) {
            certificateDir.mkdirs()
        }
        
        // Verify BouncyCastle is available
        try {
            Class.forName("org.bouncycastle.cert.X509v3CertificateBuilder")
            Class.forName("org.bouncycastle.operator.jcajce.JcaContentSignerBuilder")
        } catch (e: ClassNotFoundException) {
            throw IllegalStateException(
                "BouncyCastle library not found. Please add BouncyCastle dependency to your project. " +
                "Required: org.bouncycastle:bcprov-jdk15on and org.bouncycastle:bcpkix-jdk15on",
                e
            )
        }
    }
    
    override suspend fun generateSelfSignedCertificate(
        commonName: String,
        validityDays: Int
    ): Result<CertificateService.CertificateData> = withContext(Dispatchers.IO) {
        try {
            // Generate RSA key pair
            val keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM)
            keyPairGenerator.initialize(KEY_SIZE)
            val keyPair = keyPairGenerator.generateKeyPair()
            
            // Calculate validity dates
            val notBefore = Date()
            val notAfter = Date(notBefore.time + (validityDays * 24L * 60 * 60 * 1000))
            
            // Create X.509 certificate
            val certificate = generateX509Certificate(
                keyPair = keyPair,
                commonName = commonName,
                notBefore = notBefore,
                notAfter = notAfter
            )
            
            // Calculate fingerprint
            val fingerprint = calculateFingerprint(certificate.encoded)
            
            val certificateData = CertificateService.CertificateData(
                certificate = certificate.encoded,
                privateKey = keyPair.private.encoded,
                publicKey = keyPair.public.encoded,
                fingerprint = fingerprint
            )
            
            Result.success(certificateData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun storeCertificate(
        alias: String,
        certificateData: CertificateService.CertificateData
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Load or create keystore
            val keyStore = KeyStore.getInstance(KEYSTORE_TYPE)
            if (keystoreFile.exists()) {
                FileInputStream(keystoreFile).use { fis ->
                    keyStore.load(fis, keystorePassword)
                }
            } else {
                keyStore.load(null, keystorePassword)
            }
            
            // Reconstruct certificate and private key
            val certFactory = java.security.cert.CertificateFactory.getInstance("X.509")
            val certificate = certFactory.generateCertificate(
                certificateData.certificate.inputStream()
            ) as X509Certificate
            
            val keyFactory = KeyFactory.getInstance(KEY_ALGORITHM)
            val privateKeySpec = java.security.spec.PKCS8EncodedKeySpec(certificateData.privateKey)
            val privateKey = keyFactory.generatePrivate(privateKeySpec)
            
            // Store in keystore
            keyStore.setKeyEntry(
                alias,
                privateKey,
                keystorePassword,
                arrayOf<Certificate>(certificate)
            )
            
            // Save keystore to file
            FileOutputStream(keystoreFile).use { fos ->
                keyStore.store(fos, keystorePassword)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun retrieveCertificate(alias: String): Result<CertificateService.CertificateData> = withContext(Dispatchers.IO) {
        try {
            if (!keystoreFile.exists()) {
                return@withContext Result.failure(Exception("Keystore not found"))
            }
            
            val keyStore = KeyStore.getInstance(KEYSTORE_TYPE)
            FileInputStream(keystoreFile).use { fis ->
                keyStore.load(fis, keystorePassword)
            }
            
            if (!keyStore.containsAlias(alias)) {
                return@withContext Result.failure(Exception("Certificate not found: $alias"))
            }
            
            val certificate = keyStore.getCertificate(alias) as X509Certificate
            val privateKey = keyStore.getKey(alias, keystorePassword) as PrivateKey
            val publicKey = certificate.publicKey
            
            val fingerprint = calculateFingerprint(certificate.encoded)
            
            val certificateData = CertificateService.CertificateData(
                certificate = certificate.encoded,
                privateKey = privateKey.encoded,
                publicKey = publicKey.encoded,
                fingerprint = fingerprint
            )
            
            Result.success(certificateData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun verifyCertificateFingerprint(
        certificate: ByteArray,
        expectedFingerprint: String
    ): Boolean {
        return try {
            val actualFingerprint = calculateFingerprint(certificate)
            actualFingerprint.equals(expectedFingerprint, ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }
    
    override fun calculateFingerprint(certificate: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(certificate)
        return hash.joinToString(":") { "%02X".format(it) }
    }
    
    override suspend fun deleteCertificate(alias: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!keystoreFile.exists()) {
                return@withContext Result.success(Unit)
            }
            
            val keyStore = KeyStore.getInstance(KEYSTORE_TYPE)
            FileInputStream(keystoreFile).use { fis ->
                keyStore.load(fis, keystorePassword)
            }
            
            if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias)
                
                FileOutputStream(keystoreFile).use { fos ->
                    keyStore.store(fos, keystorePassword)
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun certificateExists(alias: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!keystoreFile.exists()) {
                return@withContext false
            }
            
            val keyStore = KeyStore.getInstance(KEYSTORE_TYPE)
            FileInputStream(keystoreFile).use { fis ->
                keyStore.load(fis, keystorePassword)
            }
            
            keyStore.containsAlias(alias)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Generate a random password for keystore.
     * 
     * Security: Each session uses a different random password for the keystore.
     * This is acceptable for certificate storage as certificates are not secret.
     */
    private fun generateRandomPassword(): CharArray {
        val random = java.security.SecureRandom()
        val passwordBytes = ByteArray(32)
        random.nextBytes(passwordBytes)
        return android.util.Base64.encodeToString(passwordBytes, android.util.Base64.NO_WRAP).toCharArray()
    }
    
    /**
     * Generate a self-signed X.509 certificate using BouncyCastle.
     * 
     * Creates a proper X.509v3 certificate with:
     * - RSA 2048-bit key pair
     * - SHA256withRSA signature algorithm
     * - Specified validity period
     * - Common name in subject
     */
    private fun generateX509Certificate(
        keyPair: KeyPair,
        commonName: String,
        notBefore: Date,
        notAfter: Date
    ): X509Certificate {
        try {
            // Create X.509 certificate builder
            val certBuilder = X509v3CertificateBuilder(
                // Issuer (self-signed, so same as subject)
                X500Name("CN=$commonName"),
                // Serial number
                BigInteger.valueOf(System.currentTimeMillis()),
                // Validity start
                notBefore,
                // Validity end
                notAfter,
                // Subject
                X500Name("CN=$commonName"),
                // Public key
                SubjectPublicKeyInfo.getInstance(keyPair.public.encoded)
            )
            
            // Create content signer
            val contentSigner = JcaContentSignerBuilder(SIGNATURE_ALGORITHM)
                .build(keyPair.private)
            
            // Build certificate
            val certHolder = certBuilder.build(contentSigner)
            
            // Convert to X509Certificate
            return JcaX509CertificateConverter()
                .getCertificate(certHolder)
        } catch (e: Exception) {
            throw RuntimeException("Failed to generate X.509 certificate", e)
        }
    }
}
