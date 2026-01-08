package ireader.domain.di

import ireader.domain.services.common.ServiceResult
import ireader.domain.services.download.desktopDownloadModule
import ireader.domain.services.platform.*
import ireader.domain.services.tts_service.v2.TTSV2ServiceStarter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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
    
    // Include Desktop-specific download module
    includes(desktopDownloadModule)
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
    private val networkStateFlow = MutableStateFlow(NetworkState(
        isConnected = false,
        type = NetworkType.NONE,
        isMetered = false
    ))
    private var monitorJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    override suspend fun initialize() {
        // Initial network check
        networkStateFlow.value = getNetworkState()
    }
    
    override suspend fun start() { 
        running = true
        startNetworkMonitoring()
    }
    
    override suspend fun stop() { 
        running = false
        monitorJob?.cancel()
        monitorJob = null
    }
    
    override fun isRunning(): Boolean = running
    
    override suspend fun cleanup() {
        stop()
    }
    
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
        val connected = isConnected()
        return NetworkState(
            isConnected = connected,
            type = if (connected) detectNetworkType() else NetworkType.NONE,
            isMetered = false
        )
    }
    
    override fun observeNetworkChanges(): Flow<NetworkState> {
        return networkStateFlow
    }
    
    override suspend fun measureNetworkSpeed(): ServiceResult<NetworkSpeed> {
        return try {
            // Measure download speed using a small test file
            val testUrl = "https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png"
            val startTime = System.currentTimeMillis()
            
            val connection = java.net.URL(testUrl).openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.requestMethod = "GET"
            
            val bytes = connection.inputStream.use { it.readBytes() }
            val endTime = System.currentTimeMillis()
            
            val durationSeconds = (endTime - startTime) / 1000.0
            val bytesPerSecond = if (durationSeconds > 0) bytes.size / durationSeconds else 0.0
            val mbps = (bytesPerSecond * 8) / 1_000_000 // Convert to Mbps
            
            connection.disconnect()
            
            ServiceResult.Success(NetworkSpeed(
                downloadMbps = mbps.toFloat(),
                uploadMbps = 0.0f, // Upload speed measurement not implemented
                latencyMs = (endTime - startTime)
            ))
        } catch (e: Exception) {
            ServiceResult.Error("Failed to measure network speed: ${e.message}")
        }
    }
    
    override suspend fun isHostReachable(host: String, timeoutMs: Long): Boolean {
        return try {
            val address = InetAddress.getByName(host)
            address.isReachable(timeoutMs.toInt())
        } catch (e: Exception) {
            false
        }
    }
    
    private fun detectNetworkType(): NetworkType {
        return try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val ni = interfaces.nextElement()
                if (ni.isUp && !ni.isLoopback) {
                    val name = ni.name.lowercase()
                    return when {
                        name.contains("eth") || name.contains("en") -> NetworkType.ETHERNET
                        name.contains("wlan") || name.contains("wifi") || name.contains("wi-fi") -> NetworkType.WIFI
                        else -> NetworkType.ETHERNET // Default to ethernet for desktop
                    }
                }
            }
            NetworkType.NONE
        } catch (e: Exception) {
            NetworkType.ETHERNET // Default assumption for desktop
        }
    }
    
    private fun startNetworkMonitoring() {
        monitorJob?.cancel()
        monitorJob = scope.launch {
            while (running) {
                val newState = getNetworkState()
                if (newState != networkStateFlow.value) {
                    networkStateFlow.value = newState
                }
                delay(5000) // Check every 5 seconds
            }
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
