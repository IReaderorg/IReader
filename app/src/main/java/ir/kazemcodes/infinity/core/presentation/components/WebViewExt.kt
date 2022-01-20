package ir.kazemcodes.infinity.core.presentation.components

import android.webkit.WebView
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.utils.Resource
import ir.kazemcodes.infinity.core.utils.UiEvent
import ir.kazemcodes.infinity.core.utils.UiText
import ir.kazemcodes.infinity.core.utils.getHtml
import ir.kazemcodes.infinity.feature_sources.sources.models.FetchType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import uy.kohesive.injekt.injectLazy

