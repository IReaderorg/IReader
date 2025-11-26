package ireader.domain.di

import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import ireader.domain.services.common.ServiceResult
import ireader.domain.services.platform.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android-specific platform service implementations
 */
actual val platformServiceModule = module {
    
    single<DeviceInfoService> {
        AndroidDeviceInfoService(androidContext())
    }
    
    single<NetworkService> {
        AndroidNetworkService(androidContext())
    }
    
    single<BiometricService> {
        AndroidBiometricService(androidContext())
    }
    
    single<HapticService> {
        AndroidHapticService(androidContext())
    }
    
    single<SecureStorageService> {
        AndroidSecureStorageService(androidContext())
    }
    
    single<ClipboardService> {
        ireader.domain.services.platform.AndroidClipboardService(androidContext())
    }
    
    single<FileSystemService> {
        ireader.domain.services.platform.AndroidFileSystemService(androidContext())
    }
    
    single<ShareService> {
        ireader.domain.services.platform.AndroidShareService(androidContext())
    }
    
    single<SystemInteractionService> {
        ireader.domain.services.platform.AndroidSystemInteractionService(androidContext())
    }
    
    single<PlatformCapabilities> {
        ireader.domain.services.platform.AndroidPlatformCapabilities(androidContext())
    }
}

/**
 * Android implementation of DeviceInfoService
 */
class AndroidDeviceInfoService(
    private val context: Context
) : DeviceInfoService {
    
    private var running = false
    
    override suspend fun initialize() {}
    override suspend fun start() { running = true }
    override suspend fun stop() { running = false }
    override fun isRunning(): Boolean = running
    override suspend fun cleanup() {}
    
    override fun isTablet(): Boolean {
        val config = context.resources.configuration
        return config.smallestScreenWidthDp >= 600
    }
    
    override fun isLandscape(): Boolean {
        val config = context.resources.configuration
        return config.orientation == Configuration.ORIENTATION_LANDSCAPE
    }
    
    override fun getDeviceType(): DeviceType {
        return when {
            isTablet() -> DeviceType.TABLET
            else -> DeviceType.PHONE
        }
    }
    
    override fun getScreenSize(): ScreenSize {
        val displayMetrics = context.resources.displayMetrics
        val config = context.resources.configuration
        
        return ScreenSize(
            widthDp = config.screenWidthDp,
            heightDp = config.screenHeightDp,
            widthPx = displayMetrics.widthPixels,
            heightPx = displayMetrics.heightPixels,
            smallestWidthDp = config.smallestScreenWidthDp
        )
    }
    
    override fun getScreenDensity(): Float {
        return context.resources.displayMetrics.density
    }
    
    override fun observeOrientationChanges(): Flow<OrientationEvent> {
        // TODO: Implement orientation change observer
        return MutableStateFlow(OrientationEvent(isLandscape(), 0))
    }
    
    override fun getDeviceModel(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }
    
    override fun getOSVersion(): String {
        return "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    }
    
    override fun hasCapability(capability: DeviceCapability): Boolean {
        return when (capability) {
            DeviceCapability.BIOMETRIC_AUTH -> {
                val biometricManager = BiometricManager.from(context)
                biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == 
                    BiometricManager.BIOMETRIC_SUCCESS
            }
            DeviceCapability.HAPTIC_FEEDBACK -> {
                context.getSystemService(Context.VIBRATOR_SERVICE) != null
            }
            // Add more capabilities as needed
            else -> false
        }
    }
}

/**
 * Android implementation of NetworkService
 */
class AndroidNetworkService(
    private val context: Context
) : NetworkService {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var running = false
    
    override suspend fun initialize() {}
    override suspend fun start() { running = true }
    override suspend fun stop() { running = false }
    override fun isRunning(): Boolean = running
    override suspend fun cleanup() {}
    
    override fun isConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    
    override fun isWiFi(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
    
    override fun isMobile(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
    
    override fun isEthernet(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
    
    override fun isMetered(): Boolean {
        return connectivityManager.isActiveNetworkMetered
    }
    
    override fun getNetworkState(): NetworkState {
        return NetworkState(
            isConnected = isConnected(),
            type = when {
                isWiFi() -> NetworkType.WIFI
                isMobile() -> NetworkType.MOBILE
                isEthernet() -> NetworkType.ETHERNET
                else -> NetworkType.NONE
            },
            isMetered = isMetered()
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
        // TODO: Implement host reachability check
        return isConnected()
    }
}

/**
 * Android implementation of BiometricService
 */
class AndroidBiometricService(
    private val context: Context
) : BiometricService {
    
    private val biometricManager = BiometricManager.from(context)
    private var running = false
    
    override suspend fun initialize() {}
    override suspend fun start() { running = true }
    override suspend fun stop() { running = false }
    override fun isRunning(): Boolean = running
    override suspend fun cleanup() {}
    
    override suspend fun isBiometricAvailable(): Boolean {
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == 
            BiometricManager.BIOMETRIC_SUCCESS
    }
    
    override suspend fun isBiometricEnrolled(): Boolean {
        return isBiometricAvailable()
    }
    
    override suspend fun authenticate(
        title: String,
        subtitle: String?,
        description: String?,
        negativeButtonText: String,
        confirmationRequired: Boolean
    ): ServiceResult<BiometricResult> {
        // TODO: Implement biometric authentication
        // This requires Activity context and BiometricPrompt setup
        return ServiceResult.Error("Biometric authentication requires Activity context")
    }
    
    override fun getSupportedBiometricTypes(): List<BiometricType> {
        val types = mutableListOf<BiometricType>()
        
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == 
            BiometricManager.BIOMETRIC_SUCCESS) {
            types.add(BiometricType.FINGERPRINT)
        }
        
        return types
    }
    
    override fun getBiometricCapability(): BiometricCapability {
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricCapability.STRONG
            else -> when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
                BiometricManager.BIOMETRIC_SUCCESS -> BiometricCapability.WEAK
                else -> BiometricCapability.NONE
            }
        }
    }
}

/**
 * Android implementation of HapticService
 */
class AndroidHapticService(
    private val context: Context
) : HapticService {
    
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    private var running = false
    
    override suspend fun initialize() {}
    override suspend fun start() { running = true }
    override suspend fun stop() { running = false }
    override fun isRunning(): Boolean = running
    override suspend fun cleanup() {}
    
    override fun performHapticFeedback(type: HapticType) {
        if (vibrator == null || !vibrator.hasVibrator()) return
        
        val duration = when (type) {
            HapticType.LIGHT_IMPACT -> 10L
            HapticType.MEDIUM_IMPACT -> 20L
            HapticType.HEAVY_IMPACT -> 30L
            HapticType.SUCCESS -> 15L
            HapticType.WARNING -> 25L
            HapticType.ERROR -> 40L
            HapticType.SELECTION -> 5L
            HapticType.CLICK -> 10L
            HapticType.LONG_PRESS -> 50L
            HapticType.REJECT -> 30L
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }
    
    override fun isHapticSupported(): Boolean {
        return vibrator?.hasVibrator() == true
    }
    
    override fun isHapticEnabled(): Boolean {
        return isHapticSupported()
    }
    
    override fun performCustomVibration(pattern: LongArray, amplitudes: IntArray?) {
        if (vibrator == null || !vibrator.hasVibrator()) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && amplitudes != null) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }
    
    override fun cancelVibration() {
        vibrator?.cancel()
    }
}

/**
 * Android implementation of SecureStorageService using EncryptedSharedPreferences
 */
class AndroidSecureStorageService(
    private val context: Context
) : SecureStorageService {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "secure_storage",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    private var running = false
    
    override suspend fun initialize() {}
    override suspend fun start() { running = true }
    override suspend fun stop() { running = false }
    override fun isRunning(): Boolean = running
    override suspend fun cleanup() {}
    
    override suspend fun putString(key: String, value: String): ServiceResult<Unit> {
        return try {
            encryptedPrefs.edit().putString(key, value).apply()
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to store value: ${e.message}")
        }
    }
    
    override suspend fun getString(key: String): ServiceResult<String?> {
        return try {
            val value = encryptedPrefs.getString(key, null)
            ServiceResult.Success(value)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to retrieve value: ${e.message}")
        }
    }
    
    override suspend fun putBytes(key: String, value: ByteArray): ServiceResult<Unit> {
        return try {
            val base64 = android.util.Base64.encodeToString(value, android.util.Base64.DEFAULT)
            encryptedPrefs.edit().putString(key, base64).apply()
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to store bytes: ${e.message}")
        }
    }
    
    override suspend fun getBytes(key: String): ServiceResult<ByteArray?> {
        return try {
            val base64 = encryptedPrefs.getString(key, null)
            val bytes = base64?.let { android.util.Base64.decode(it, android.util.Base64.DEFAULT) }
            ServiceResult.Success(bytes)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to retrieve bytes: ${e.message}")
        }
    }
    
    override suspend fun remove(key: String): ServiceResult<Unit> {
        return try {
            encryptedPrefs.edit().remove(key).apply()
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to remove value: ${e.message}")
        }
    }
    
    override suspend fun contains(key: String): Boolean {
        return encryptedPrefs.contains(key)
    }
    
    override suspend fun clear(): ServiceResult<Unit> {
        return try {
            encryptedPrefs.edit().clear().apply()
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to clear storage: ${e.message}")
        }
    }
    
    override suspend fun getAllKeys(): List<String> {
        return encryptedPrefs.all.keys.toList()
    }
    
    override fun isSecureStorageAvailable(): Boolean {
        return true // EncryptedSharedPreferences is always available on Android
    }
    
    override fun getEncryptionLevel(): EncryptionLevel {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            EncryptionLevel.HARDWARE // Uses Android Keystore
        } else {
            EncryptionLevel.SOFTWARE
        }
    }
}
