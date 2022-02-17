package org.ireader.domain.feature_services.io

sealed class Page

data class PageUrl(val url: String) : Page()
sealed class PageComplete : Page()

data class ImageUrl(val url: String) : PageComplete()
data class ImageBase64(val data: String) : PageComplete()
data class Text(val text: String) : PageComplete()