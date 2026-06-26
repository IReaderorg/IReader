package eu.kanade.tachiyomi.util

import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Minimal Jsoup extensions shim for tsundoku extension compatibility.
 *
 * Tsundoku extensions use `response.asJsoup()` to parse HTML responses.
 */
fun Response.asJsoup(html: String? = null): Document {
    return Jsoup.parse(html ?: body?.string() ?: "", request.url.toString())
}
