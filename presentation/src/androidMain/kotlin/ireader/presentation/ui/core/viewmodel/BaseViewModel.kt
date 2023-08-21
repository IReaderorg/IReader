package ireader.presentation.ui.core.viewmodel

//actual abstract class BaseViewModel : ScreenModel {
//
//    actual val scope: CoroutineScope
//        get() = this.coroutineScope
//
//    private val activeScope = MutableStateFlow<CoroutineScope?>(null)
//
//    protected val _eventFlow = MutableSharedFlow<UiEvent>()
//    actual open val eventFlow = _eventFlow.asSharedFlow()
//
//    actual open fun showSnackBar(message: UiText?) {
//        scope.launch {
//            _eventFlow.showSnackBar(message ?: UiText.MStringResource{ xml->
//            xml.errorUnknown
//        })
//        }
//    }
//
//    //    final override fun onCleared() {
////        onDestroy()
////    }
//    override fun onDispose() {
//        onDestroy()
//        super.onDispose()
//    }
//
//    actual open fun onDestroy() {
//    }
//
//    actual fun <T> Preference<T>.asState() = PreferenceMutableState(this, scope)
//
//    actual fun <T> Preference<T>.asState(onChange: (T) -> Unit): PreferenceMutableState<T> {
//        this.changes()
//            .onEach { onChange(it) }
//            .launchIn(scope)
//        return PreferenceMutableState(this, scope)
//    }
//
//    actual fun <T> Flow<T>.asState(initialValue: T, onChange: (T) -> Unit): State<T> {
//        val state = mutableStateOf(initialValue)
//        scope.launch {
//            collect {
//                state.value = it
//                onChange(it)
//            }
//        }
//        return state
//    }
//
//    actual fun <T> StateFlow<T>.asState(): State<T> {
//        val state = mutableStateOf(value)
//        scope.launch {
//            collect { state.value = it }
//        }
//        return state
//    }
//
//    actual fun <T> Flow<T>.launchWhileActive(): Job {
//        return activeScope
//            .filterNotNull()
//            .onEach { launchIn(it) }
//            .launchIn(scope)
//    }
//
//    @OptIn(ExperimentalCoroutinesApi::class)
//    fun <T> Flow<T>.launchWhenActive() = channelFlow<T> {
//        scope.launch {
//            activeScope
//            this@launchWhenActive.filterNotNull()
//                .first {
//                    send(it)
//                    true
//                }
//        }
//    }
//
//    internal fun setActive() {
//        val currScope = activeScope.value
//        if (currScope != null) return
//        activeScope.value = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
//    }
//
//    internal fun setInactive() {
//        val currScope = activeScope.value
//        currScope?.cancel()
//        activeScope.value = null
//    }
//
//
//}
//suspend fun MutableSharedFlow<UiEvent>.showSnackBar(message: UiText?) {
//    this.emit(
//        UiEvent.ShowSnackbar(
//            uiText = message ?: UiText.MStringResource{ xml->
//            xml.errorUnknown
//        }
//        )
//    )
//}
