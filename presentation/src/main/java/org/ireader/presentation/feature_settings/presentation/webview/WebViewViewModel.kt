package org.ireader.presentation.feature_settings.presentation.webview

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.catalog.service.CatalogStore
import org.ireader.domain.ui.NavigationArgs
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

/**This is fake Alert **/
@SuppressLint("StaticFieldLeak")
@HiltViewModel
class WebViewPageModel @Inject constructor(
    private val insertUseCases: LocalInsertUseCases,
    private val getBookUseCases: org.ireader.domain.use_cases.local.LocalGetBookUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val extensions: CatalogStore,
    private val webpageImpl: WebViewPageStateImpl,
) : BaseViewModel(), WebViewPageState by webpageImpl {



    init {
        val url = URLDecoder.decode(savedStateHandle.get<String>(NavigationArgs.url.name),
            StandardCharsets.UTF_8.name())
        updateUrl(url)
        updateWebUrl(url = url)
    }




    fun toggleLoading(loading: Boolean) {
        isLoading = loading
    }

    fun updateUrl(url: String) {
        this.url = url
    }

    fun updateWebUrl(url: String) {
        webUrl = url

    }


}

interface WebViewPageState {
    var url: String
    var webUrl: String
    var isLoading: Boolean

}

open class WebViewPageStateImpl @Inject constructor() : WebViewPageState {
    override var url: String by mutableStateOf("")
    override var webUrl: String by mutableStateOf("")

    override var isLoading: Boolean by mutableStateOf(false)


}

