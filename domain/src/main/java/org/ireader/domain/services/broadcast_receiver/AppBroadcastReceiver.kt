package org.ireader.domain.services.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.ireader.domain.use_cases.services.ServiceUseCases
import org.ireader.presentation.feature_ttl.TTSStateImpl
import javax.inject.Inject

@AndroidEntryPoint()
class AppBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var state: TTSStateImpl

    @Inject
    lateinit var usecase: ServiceUseCases

    val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent) {
//        intent.getIntExtra("PLAYER", -1).let { command ->
//            usecase.startTTSServicesUseCase(command)
//
//        }
    }
}
