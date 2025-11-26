package ireader.domain.di

import ireader.domain.services.platform.*
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Platform-specific service module
 * 
 * This module should be implemented in each platform module (Android, Desktop, iOS)
 * to provide platform-specific implementations of services.
 * 
 * Example for Android (androidMain):
 * ```kotlin
 * actual val platformServiceModule = module {
 *     single<DeviceInfoService> { AndroidDeviceInfoService(androidContext()) }
 *     single<NetworkService> { AndroidNetworkService(androidContext()) }
 *     single<BiometricService> { AndroidBiometricService(androidContext()) }
 *     single<HapticService> { AndroidHapticService(androidContext()) }
 *     single<SecureStorageService> { AndroidSecureStorageService(androidContext()) }
 * }
 * ```
 * 
 * Example for Desktop (desktopMain):
 * ```kotlin
 * actual val platformServiceModule = module {
 *     single<DeviceInfoService> { DesktopDeviceInfoService() }
 *     single<NetworkService> { DesktopNetworkService() }
 *     single<BiometricService> { NoOpBiometricService() } // Desktop doesn't have biometric
 *     single<HapticService> { NoOpHapticService() } // Desktop doesn't have haptic
 *     single<SecureStorageService> { DesktopSecureStorageService() }
 * }
 * ```
 */
expect val platformServiceModule: Module

/**
 * No-op implementations for platforms that don't support certain features
 */

/**
 * No-op biometric service for platforms without biometric support
 */
class NoOpBiometricService : BiometricService {
    private var running = false
    
    override suspend fun initialize() {}
    override suspend fun start() { running = true }
    override suspend fun stop() { running = false }
    override fun isRunning(): Boolean = running
    override suspend fun cleanup() {}
    
    override suspend fun isBiometricAvailable() = false
    override suspend fun isBiometricEnrolled() = false
    
    override suspend fun authenticate(
        title: String,
        subtitle: String?,
        description: String?,
        negativeButtonText: String,
        confirmationRequired: Boolean
    ) = ireader.domain.services.common.ServiceResult.Error("Biometric not supported on this platform")
    
    override fun getSupportedBiometricTypes() = emptyList<BiometricType>()
    override fun getBiometricCapability() = BiometricCapability.NONE
}

/**
 * No-op haptic service for platforms without haptic support
 */
class NoOpHapticService : HapticService {
    private var running = false
    
    override suspend fun initialize() {}
    override suspend fun start() { running = true }
    override suspend fun stop() { running = false }
    override fun isRunning(): Boolean = running
    override suspend fun cleanup() {}
    
    override fun performHapticFeedback(type: HapticType) {
        // No-op
    }
    
    override fun isHapticSupported() = false
    override fun isHapticEnabled() = false
    
    override fun performCustomVibration(pattern: LongArray, amplitudes: IntArray?) {
        // No-op
    }
    
    override fun cancelVibration() {
        // No-op
    }
}
