package ireader.domain.services.platform

import ireader.domain.services.common.ServiceResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Fake implementation of SystemInteractionService for testing
 */
class FakeSystemInteractionService : SystemInteractionService {
    
    var brightness: Float = 0.5f
        private set
    
    var volume: Float = 0.5f
        private set
    
    var isSecureScreenEnabled: Boolean = false
        private set
    
    var isKeepScreenOnEnabled: Boolean = false
        private set
    
    var isLandscapeValue: Boolean = false
    var isTabletValue: Boolean = false
    
    private val volumeKeyFlow = MutableSharedFlow<VolumeKeyEvent>()
    private val orientationFlow = MutableSharedFlow<OrientationEvent>()
    
    override suspend fun initialize() {
        // No-op for testing
    }
    
    override suspend fun cleanup() {
        // No-op for testing
    }
    
    override suspend fun getBrightness(): Float = brightness
    
    override suspend fun setBrightness(brightness: Float): ServiceResult<Unit> {
        this.brightness = brightness.coerceIn(0f, 1f)
        return ServiceResult.Success(Unit)
    }
    
    override fun isBrightnessControlSupported(): Boolean = true
    
    override suspend fun getVolume(): Float = volume
    
    override suspend fun setVolume(volume: Float): ServiceResult<Unit> {
        this.volume = volume.coerceIn(0f, 1f)
        return ServiceResult.Success(Unit)
    }
    
    override fun observeVolumeKeys(): Flow<VolumeKeyEvent> = volumeKeyFlow
    
    override suspend fun setSecureScreen(enabled: Boolean): ServiceResult<Unit> {
        isSecureScreenEnabled = enabled
        return ServiceResult.Success(Unit)
    }
    
    override fun isSecureScreenSupported(): Boolean = true
    
    override suspend fun setKeepScreenOn(enabled: Boolean): ServiceResult<Unit> {
        isKeepScreenOnEnabled = enabled
        return ServiceResult.Success(Unit)
    }
    
    override fun isLandscape(): Boolean = isLandscapeValue
    
    override fun isTablet(): Boolean = isTabletValue
    
    override fun observeOrientationChanges(): Flow<OrientationEvent> = orientationFlow
    
    // Test helpers
    suspend fun emitVolumeKeyEvent(event: VolumeKeyEvent) {
        volumeKeyFlow.emit(event)
    }
    
    suspend fun emitOrientationEvent(event: OrientationEvent) {
        orientationFlow.emit(event)
    }
    
    fun reset() {
        brightness = 0.5f
        volume = 0.5f
        isSecureScreenEnabled = false
        isKeepScreenOnEnabled = false
        isLandscapeValue = false
        isTabletValue = false
    }
}
