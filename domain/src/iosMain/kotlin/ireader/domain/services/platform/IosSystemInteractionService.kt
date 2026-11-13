package ireader.domain.services.platform

import ireader.domain.services.common.ServiceResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * iOS system controls stay with the OS. The reader still needs this service
 * so it can open.
 */
class IosSystemInteractionService : SystemInteractionService {

    private var brightness: Float = 1f
    private var running = false

    override suspend fun initialize() = Unit
    override suspend fun start() { running = true }
    override suspend fun stop() { running = false }
    override fun isRunning(): Boolean = running
    override suspend fun cleanup() = Unit

    override suspend fun getBrightness(): Float = brightness

    override suspend fun setBrightness(brightness: Float): ServiceResult<Unit> {
        this.brightness = brightness.coerceIn(0f, 1f)
        return ServiceResult.Success(Unit)
    }

    override fun isBrightnessControlSupported(): Boolean = false

    override suspend fun getVolume(): Float = 1f

    override suspend fun setVolume(volume: Float): ServiceResult<Unit> {
        return ServiceResult.Error("Volume control is not available on iOS")
    }

    override fun observeVolumeKeys(): Flow<VolumeKeyEvent> = emptyFlow()

    override suspend fun setSecureScreen(enabled: Boolean): ServiceResult<Unit> {
        return ServiceResult.Success(Unit)
    }

    override fun isSecureScreenSupported(): Boolean = false

    override suspend fun setKeepScreenOn(enabled: Boolean): ServiceResult<Unit> {
        return ServiceResult.Success(Unit)
    }

    override fun isLandscape(): Boolean = false

    override fun isTablet(): Boolean = false

    override fun observeOrientationChanges(): Flow<OrientationEvent> = emptyFlow()
}
