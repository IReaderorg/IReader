package ireader.core.source.model

import kotlinx.serialization.Serializable

/**
 * Model for a recommended novel/manga entry returned by a source.
 */
@Serializable
data class RecommendationInfo(
    val key: String,
    val title: String,
    val cover: String = "",
    val genres: List<String> = emptyList(),
    val sourceId: Long = 0L,
    val sourceName: String = ""
) {
    fun isValid(): Boolean = key.isNotBlank() && title.isNotBlank()
}
