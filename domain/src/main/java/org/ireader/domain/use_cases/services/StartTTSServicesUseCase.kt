package org.ireader.domain.use_cases.services

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import org.ireader.domain.services.tts_service.TTSService
import javax.inject.Inject

class StartTTSServicesUseCase @Inject constructor(@ApplicationContext private val context: Context) {
    operator fun invoke(
        command:Int,
        bookId:Long? = null,
        chapterId:Long? = null,
    ) {
        val work  =
            OneTimeWorkRequestBuilder<TTSService>().apply {
                setInputData(
                    Data.Builder().apply {
                        if (chapterId != null) {
                            putLong(TTSService.TTS_Chapter_ID, chapterId)
                        }
                        if (bookId != null) {
                            putLong(TTSService.TTS_BOOK_ID, bookId)
                        }
                        putInt(TTSService.COMMAND, command)
                    }.build()
                )
                addTag(TTSService.TTS_SERVICE_NAME)
            }.build()
        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            TTSService.TTS_SERVICE_NAME,
            ExistingWorkPolicy.REPLACE,
            work
        )
    }
}