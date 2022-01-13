package ir.kazemcodes.infinity.presentation.webview

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.zhuinden.simplestack.ScopedServices

class WebViewViewModel(url : String) : ScopedServices.Registered {

    private val _state = mutableStateOf(WebViewState(url = url))
    val state: State<WebViewState> = _state

    override fun onServiceRegistered() {

    }

    override fun onServiceUnregistered() {

    }
}

data class WebViewState(
    val url : String
)