package ir.kazemcodes.infinity.explore_feature.domain.util

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

fun encodeUrl(string: String) : String {
    return URLEncoder.encode(string , StandardCharsets.UTF_8.name())
}
fun decodeUrl(string: String) : String {
    return URLDecoder.decode(string , StandardCharsets.UTF_8.name())
}