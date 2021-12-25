package ir.kazemcodes.infinity.explore_feature.domain.util

import androidx.compose.foundation.lazy.LazyListState
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

fun encodeString(string: String): String {
    return URLEncoder.encode(string, StandardCharsets.UTF_8.name())
}

fun decodeString(string: String): String {
    return URLDecoder.decode(string, StandardCharsets.UTF_8.name())
}

fun LazyListState.isScrolledToTheEnd() =
    layoutInfo.visibleItemsInfo.lastOrNull()?.index == layoutInfo.totalItemsCount - 1