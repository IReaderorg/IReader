package org.ireader.presentation.feature_reader.presentation.reader.viewmodel

//
//@AndroidEntryPoint()
//class TextReaderBroadcastReceiver : BroadcastReceiver() {
//
//    @Inject
//    lateinit var state: TextReaderScreenStateImpl
//
//    val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
//
//    override fun onReceive(p0: Context?, intent: Intent?) {
//        Timber.e(intent?.extras?.toString())
//        intent?.getIntExtra("PLAYER",-1)?.let {
//            scope.launch {
//                state.mediaPlayerNotification.emit(it)
//            }
//        }
//
//
//
//    }
//
//}