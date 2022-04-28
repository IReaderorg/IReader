package org.ireader.domain.use_cases.services

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import org.ireader.common_resources.K
import org.ireader.domain.services.tts_service.media_player.TTSService
import org.ireader.domain.services.tts_service.media_player.TTSService.Companion.COMMAND
import org.ireader.domain.services.tts_service.media_player.TTSService.Companion.TTS_BOOK_ID
import org.ireader.domain.services.tts_service.media_player.TTSService.Companion.TTS_Chapter_ID
import javax.inject.Inject

class StartTTSServicesUseCase @Inject constructor(@ApplicationContext private val context: Context) {
    operator fun invoke(
        command: Int,
        bookId: Long? = null,
        chapterId: Long? = null,
    ) {

        val intent = Intent(context, Class.forName(K.TTSService)).apply {
            action = TTSService.ACTION_UPDATE
            if (chapterId != null) {
                putExtra(TTS_Chapter_ID, chapterId)
            }
            if (bookId != null) {
                putExtra(TTS_BOOK_ID, bookId)
            }
            putExtra(COMMAND, command)
        }

        context.startService(intent)

//        val work  =
//            OneTimeWorkRequestBuilder<TTSService>().apply {
//                setInputData(
//                    Data.Builder().apply {
//                        if (chapterId != null) {
//                            putLong(TTSService.TTS_Chapter_ID, chapterId)
//                        }
//                        if (bookId != null) {
//                            putLong(TTSService.TTS_BOOK_ID, bookId)
//                        }
//                        putInt(TTSService.COMMAND, command)
//                    }.build()
//                )
//                addTag(TTSService.TTS_SERVICE_NAME)
//            }.build()
//        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
//            TTSService.TTS_SERVICE_NAME,
//            ExistingWorkPolicy.REPLACE,
//            work
//        )
    }
}
