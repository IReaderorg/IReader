

package ireader.core.source.model

import kotlinx.serialization.Serializable
@Serializable
sealed class Page
@Serializable
data class PageUrl(val url: String) : Page()
@Serializable
sealed class PageComplete : Page()

@Serializable
data class ImageUrl(val url: String) : PageComplete()
@Serializable
data class ImageBase64(val data: String) : PageComplete()
@Serializable
data class Text(val text: String) : PageComplete()

// need to add a listOf string for subtitles
@Serializable
data class MovieUrl(val url: String) : PageComplete()
@Serializable
data class Subtitles(val url: String) : PageComplete()

// creating a customized encoding and decoding because kotlin serialization may cause some problem in future.
// Unlike tachiyomi, right now ireader is using saving files in app db
const val SEPARATOR = "##$$%%@@"
const val EQUAL = "##$$@@"

fun Page.encode() :String {
    val type = when(this) {
        is ImageUrl -> "image"
        is ImageBase64 -> "image64"
        is Text -> "text"
        is MovieUrl -> "movie"
        is Subtitles -> "subtitles"
        is PageUrl -> "page"
    }
    val key = when(this) {
        is ImageUrl -> this.url
        is ImageBase64 -> this.data
        is Text -> this.text
        is MovieUrl -> this.url
        is Subtitles -> this.url
        is PageUrl -> this.url
    }
    if(key.isBlank()) {
        return ""
    }
    return "${type}${EQUAL}${key}${SEPARATOR}"
}
fun String.decode() : List<Page> {
    return this.split(SEPARATOR).mapNotNull { text ->
        val type = text.substringBefore(EQUAL)
        val key = text.substringAfter(EQUAL).substringBefore(SEPARATOR)
        when {
            type.contains("image",true)  -> ImageUrl(key)
            type.contains("image64",true)  -> ImageBase64(key)
            type.contains("text" ,true) -> Text(key)
            type.contains("movie" ,true) -> MovieUrl(key)
            type.contains("subtitles",true)  -> Subtitles(key)
            type.contains("page",true)  -> PageUrl(key)
            else -> null
        }
    }
}

fun List<Page>.encode() :String{
   return this.joinToString { it.encode() }
}