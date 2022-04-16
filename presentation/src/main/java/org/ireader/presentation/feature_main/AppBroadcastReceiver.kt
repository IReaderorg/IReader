package org.ireader.presentation.feature_main


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.ireader.domain.services.downloaderService.NotificationStates
import org.ireader.domain.services.tts_service.TTSService
import org.ireader.domain.services.tts_service.TTSService.Companion.TTS_SERVICE_NAME
import javax.inject.Inject

@AndroidEntryPoint()
class AppBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var state: NotificationStates

    val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    lateinit var ttsWork: OneTimeWorkRequest
    override fun onReceive(context: Context, intent: Intent) {
        intent.getIntExtra("PLAYER", -1).let { command ->
            ttsWork = OneTimeWorkRequestBuilder<TTSService>().apply {
                setInputData(
                    Data.Builder().apply {
                        putInt(TTSService.COMMAND, command)
                    }.build()
                )
                addTag(TTSService.TTS_SERVICE_NAME)
            }.build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                TTS_SERVICE_NAME,
                ExistingWorkPolicy.REPLACE,
                ttsWork
            )
        }
    }

}