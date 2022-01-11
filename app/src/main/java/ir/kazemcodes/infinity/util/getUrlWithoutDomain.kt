package ir.kazemcodes.infinity.util

import java.net.URI
import java.net.URISyntaxException

fun getUrlWithoutDomain(orig: String): String {
    return try {
        val uri = URI(orig.replace(" ", "%20"))
        var out = uri.path
        if (uri.query != null) {
            out += "?" + uri.query
        }
        if (uri.fragment != null) {
            out += "#" + uri.fragment
        }
        out
    } catch (e: URISyntaxException) {
        orig
    }
}