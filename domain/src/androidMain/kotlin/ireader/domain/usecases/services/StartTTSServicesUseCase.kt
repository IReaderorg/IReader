package ireader.domain.usecases.services

import android.content.Context
import ireader.domain.services.tts_service.v2.TTSV2Service

/**
 * Android implementation of StartTTSServicesUseCase
 * 
 * This is a legacy interface kept for backward compatibility.
 * New code should use TTSV2ServiceStarter directly.
 */
actual class StartTTSServicesUseCase(private val context: Context) {
    actual operator fun invoke(
        command: Int,
        bookId: Long?,
        chapterId: Long?,
    ) {
        // Legacy interface - v2 service is started via TTSV2ServiceStarter
        // This is kept for backward compatibility with ServiceUseCases
        if (bookId != null && chapterId != null) {
            val intent = TTSV2Service.createIntent(context, bookId, chapterId, 0)
            context.startForegroundService(intent)
        }
    }
}
