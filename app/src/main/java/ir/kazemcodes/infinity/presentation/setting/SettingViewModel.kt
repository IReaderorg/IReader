package ir.kazemcodes.infinity.presentation.setting

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.data.network.models.Dns
import ir.kazemcodes.infinity.domain.use_cases.datastore.DataStoreUseCase
import ir.kazemcodes.infinity.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class SettingViewModel(private val dataStoreUseCase: DataStoreUseCase) : ScopedServices.Registered {
    private val _state = mutableStateOf<SettingState>(SettingState())
    val state: State<SettingState> = _state

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)


    fun setDohPrfUpdate(prefCode: Int) {
        _state.value = state.value.copy(doh = prefCode)
        coroutineScope.launch(Dispatchers.IO) {
            dataStoreUseCase.saveDohPrefUseCase(prefCode)
        }
    }

    fun readDohPref() {
        coroutineScope.launch(Dispatchers.Main) {
            dataStoreUseCase.readDohPrefUseCase().collectLatest { result ->
                when (result) {
                    is Resource.Success -> {

                        _state.value = state.value
                            .copy(
                                doh = result.data ?: Dns.Disable.prefCode
                            )
                    }

                    is Resource.Error -> {
                        Timber.e("Timber: ReadDohPref  : ${result.message ?: ""}")
                    }
                    else -> {
                    }
                }

            }
        }
    }

    override fun onServiceRegistered() {
        readDohPref()
    }

    override fun onServiceUnregistered() {

    }
}