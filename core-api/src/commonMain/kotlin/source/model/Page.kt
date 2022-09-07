

package ireader.core.api.source.model

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
