package ireader.domain.usecases.services

import android.content.Context
import android.content.Intent
import android.os.Build
import ireader.domain.services.tts_service.media_player.TTSService
import ireader.domain.services.tts_service.media_player.TTSService.Companion.COMMAND
import ireader.domain.services.tts_service.media_player.TTSService.Companion.TTS_BOOK_ID
import ireader.domain.services.tts_service.media_player.TTSService.Companion.TTS_Chapter_ID



actual class StartTTSServicesUseCase( private val context: Context) {
    actual operator fun invoke(
            command: Int,
            bookId: Long?,
            chapterId: Long?,
    ) {
        val intent = Intent(context, TTSService::class.java).apply {
            action = TTSService.ACTION_UPDATE
            if (chapterId != null) {
                putExtra(TTS_Chapter_ID, chapterId)
            }
            if (bookId != null) {
                putExtra(TTS_BOOK_ID, bookId)
            }
            putExtra(COMMAND, command)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}
