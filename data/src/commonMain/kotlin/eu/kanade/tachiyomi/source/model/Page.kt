package eu.kanade.tachiyomi.source.model

import kotlinx.serialization.Serializable

/**
 * Minimal Page class shim for tsundoku extension compatibility.
 */
@Serializable
open class Page(
    val index: Int,
    val url: String = "",
    var imageUrl: String? = null,
) {
    var text: String? = null
    val number: Int get() = index + 1
}
