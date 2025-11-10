package ireader.domain.usecases.services

import ireader.domain.services.tts_service.DesktopTTSService
import ireader.domain.services.tts_service.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

actual class StartTTSServicesUseCase(
    private val ttsService: DesktopTTSService
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    actual operator fun invoke(command: Int, bookId: Long?, chapterId: Long?) {
        scope.launch {
            when (command) {
                Player.PLAY -> {
                    if (bookId != null && chapterId != null) {
                        ttsService.startReading(bookId, chapterId)
                    } else {
                        ttsService.startService(DesktopTTSService.ACTION_PLAY)
                    }
                }
                Player.PAUSE -> ttsService.startService(DesktopTTSService.ACTION_PAUSE)
                Player.CANCEL -> ttsService.startService(DesktopTTSService.ACTION_STOP)
                Player.SKIP_NEXT -> ttsService.startService(DesktopTTSService.ACTION_SKIP_NEXT)
                Player.SKIP_PREV -> ttsService.startService(DesktopTTSService.ACTION_SKIP_PREV)
                Player.NEXT_PAR -> ttsService.startService(DesktopTTSService.ACTION_NEXT_PAR)
                Player.PREV_PAR -> ttsService.startService(DesktopTTSService.ACTION_PREV_PAR)
            }
        }
    }
}