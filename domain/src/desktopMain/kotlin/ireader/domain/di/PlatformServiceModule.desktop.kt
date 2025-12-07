package ireader.domain.di

import ireader.domain.services.common.ServiceResult
import ireader.domain.services.platform.*
import ireader.domain.services.tts_service.v2.TTSV2ServiceStarter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.dsl.module
import java.awt.Toolkit
import java.net.InetAddress
import java.nio.file.Files
import java.nio.file.Paths
import java.util.prefs.Preferences
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

/**
 * Desktop-specific platform service implementations
 */
actual val platformServiceModule = module {
    
    single<DeviceInfoService> {
        DesktopDeviceInfoService()
    }
    
    // TTS V2 Service Starter (no-op on desktop)
    single { TTSV2ServiceStarter() }
    
    single<NetworkService> {
        DesktopNetworkService()
    }
    
    single<BiometricService> {
        NoOpBiometricService() // Desktop doesn't have biometric
    }
    
    single<HapticService> {
        NoOpHapticService() // Desktop doesn't have haptic
    }
    
    single<SecureStorageService> {
        DesktopSecureStorageService()
    }
    
    single<ClipboardService> {
        ireader.domain.services.platform.DesktopClipboardService()
    }
    
    single<FileSystemService> {
        ireader.domain.services.platform.DesktopFileSystemService()
    }
    
    single<ShareService> {
        ireader.domain.services.platform.DesktopShareService(get())
    }
    
    single<SystemInteractionService> {
        ireader.domain.services.platform.DesktopSystemInteractionService()
    }
    
    single<PlatformCapabilities> {
        ireader.domain.services.platform.DesktopPlatformCapabilities()
    }
}

/**
 * Desktop implementation of DeviceInfoService
 */
class DesktopDeviceInfoService : DeviceInfoService {
    
    private var running = false
    
    override suspend fun initialize() {}
    override suspend fun start() { running = true }
    override suspend fun stop() { running = false }
    override fun isRunning(): Boolean = running
    override suspend fun cleanup() {}
    
    override fun isTablet(): Boolean {
        return false // Desktop is not a tablet
    }
    
    override fun isLandscape(): Boolean {
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        return screenSize.width > screenSize.height
    }
    
    override fun getDeviceType(): DeviceType {
        return DeviceType.DESKTOP
    }
    
    override fun getScreenSize(): ScreenSize {
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        val dpi = Toolkit.getDefaultToolkit().screenResolution
        val density = dpi / 160f
        
        return ScreenSize(
            widthDp = (screenSize.width / density).toInt(),
            heightDp = (screenSize.height / density).toInt(),
            widthPx = screenSize.width,
            heightPx = screenSize.height,
            smallestWidthDp = minOf(
                (screenSize.width / density).toInt(),
                (screenSize.height / density).toInt()
            )
        )
    }
    
    override fun getScreenDensity(): Float {
        val dpi = Toolkit.getDefaultToolkit().screenResolution
        return dpi / 160f
    }
    
    override fun observeOrientationChanges(): Flow<OrientationEvent> {
        // Desktop orientation doesn't change
        return MutableStateFlow(OrientationEvent(isLandscape(), 0))
    }
    
    override fun getDeviceModel(): String {
        return System.getProperty("os.name") ?: "Desktop"
    }
    
    override fun getOSVersion(): String {
        val osName = System.getProperty("os.name") ?: "Unknown"
        val osVersion = System.getProperty("os.version") ?: "Unknown"
        return "$osName $osVersion"
    }
    
    override fun hasCapability(capability: DeviceCapability): Boolean {
        return when (capability) {
            DeviceCapability.WIFI -> true
            DeviceCapability.BLUETOOTH -> true
            else -> false
        }
    }
}

/**
 * Desktop implementation of NetworkService
 */
class DesktopNetworkService : NetworkService {
    
    private var running = false
    
    override suspend fun initialize() {}
    override suspend fun start() { running = true }
    override suspend fun stop() { running = false }
    override fun isRunning(): Boolean = running
    override suspend fun cleanup() {}
    
    override fun isConnected(): Boolean {
        return try {
            val address = InetAddress.getByName("www.google.com")
            address.isReachable(5000)
        } catch (e: Exception) {
            false
        }
    }
    
    override fun isWiFi(): Boolean {
        // On desktop, we assume WiFi if connected
        return isConnected()
    }
    
    override fun isMobile(): Boolean {
        return false // Desktop doesn't use mobile data
    }
    
    override fun isEthernet(): Boolean {
        // Could be ethernet, but we can't easily detect
        return isConnected()
    }
    
    override fun isMetered(): Boolean {
        return false // Desktop connections are typically not metered
    }
    
    override fun getNetworkState(): NetworkState {
        return NetworkState(
            isConnected = isConnected(),
            type = if (isConnected()) NetworkType.ETHERNET else NetworkType.NONE,
            isMetered = false
        )
    }
    
    override fun observeNetworkChanges(): Flow<NetworkState> {
        // TODO: Implement network change observer
        return MutableStateFlow(getNetworkState())
    }
    
    override suspend fun measureNetworkSpeed(): ServiceResult<NetworkSpeed> {
        // TODO: Implement network speed measurement
        return ServiceResult.Error("Not implemented yet")
    }
    
    override suspend fun isHostReachable(host: String, timeoutMs: Long): Boolean {
        return try {
            val address = InetAddress.getByName(host)
            address.isReachable(timeoutMs.toInt())
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Desktop implementation of SecureStorageService using Java Preferences with encryption
 */
class DesktopSecureStorageService : SecureStorageService {
    
    private val prefs = Preferences.userNodeForPackage(DesktopSecureStorageService::class.java)
    private val cipher = Cipher.getInstance("AES")
    private val secretKey: SecretKey
    
    init {
        // Get or create encryption key
        val keyString = prefs.get("_encryption_key", null)
        secretKey = if (keyString != null) {
            val keyBytes = Base64.getDecoder().decode(keyString)
            SecretKeySpec(keyBytes, "AES")
        } else {
            val keyGen = KeyGenerator.getInstance("AES")
            keyGen.init(256)
            val key = keyGen.generateKey()
            prefs.put("_encryption_key", Base64.getEncoder().encodeToString(key.encoded))
            key
        }
    }
    
    private var running = false
    
    override suspend fun initialize() {}
    override suspend fun start() { running = true }
    override suspend fun stop() { running = false }
    override fun isRunning(): Boolean = running
    override suspend fun cleanup() {}
    
    private fun encrypt(value: String): String {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encrypted = cipher.doFinal(value.toByteArray())
        return Base64.getEncoder().encodeToString(encrypted)
    }
    
    private fun decrypt(encrypted: String): String {
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val decrypted = cipher.doFinal(Base64.getDecoder().decode(encrypted))
        return String(decrypted)
    }
    
    override suspend fun putString(key: String, value: String): ServiceResult<Unit> {
        return try {
            val encrypted = encrypt(value)
            prefs.put(key, encrypted)
            prefs.flush()
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to store value: ${e.message}")
        }
    }
    
    override suspend fun getString(key: String): ServiceResult<String?> {
        return try {
            val encrypted = prefs.get(key, null)
            val value = encrypted?.let { decrypt(it) }
            ServiceResult.Success(value)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to retrieve value: ${e.message}")
        }
    }
    
    override suspend fun putBytes(key: String, value: ByteArray): ServiceResult<Unit> {
        return try {
            val base64 = Base64.getEncoder().encodeToString(value)
            val encrypted = encrypt(base64)
            prefs.put(key, encrypted)
            prefs.flush()
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to store bytes: ${e.message}")
        }
    }
    
    override suspend fun getBytes(key: String): ServiceResult<ByteArray?> {
        return try {
            val encrypted = prefs.get(key, null)
            val base64 = encrypted?.let { decrypt(it) }
            val bytes = base64?.let { Base64.getDecoder().decode(it) }
            ServiceResult.Success(bytes)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to retrieve bytes: ${e.message}")
        }
    }
    
    override suspend fun remove(key: String): ServiceResult<Unit> {
        return try {
            prefs.remove(key)
            prefs.flush()
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to remove value: ${e.message}")
        }
    }
    
    override suspend fun contains(key: String): Boolean {
        return prefs.get(key, null) != null
    }
    
    override suspend fun clear(): ServiceResult<Unit> {
        return try {
            prefs.clear()
            prefs.flush()
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to clear storage: ${e.message}")
        }
    }
    
    override suspend fun getAllKeys(): List<String> {
        return prefs.keys().toList().filter { it != "_encryption_key" }
    }
    
    override fun isSecureStorageAvailable(): Boolean {
        return true
    }
    
    override fun getEncryptionLevel(): EncryptionLevel {
        return EncryptionLevel.SOFTWARE // Java Preferences with AES encryption
    }
}
