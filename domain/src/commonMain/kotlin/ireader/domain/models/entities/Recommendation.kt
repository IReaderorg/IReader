package ireader.domain.models.entities

import ireader.core.source.model.RecommendationInfo
import kotlinx.serialization.Serializable

@Serializable
data class Recommendation(
    val key: String,
    val title: String,
    val cover: String = "",
    val genres: List<String> = emptyList(),
    val sourceId: Long = 0L,
    val sourceName: String = ""
) {
    fun isValid(): Boolean = key.isNotBlank() && title.isNotBlank()

    companion object {
        fun fromSourceModel(model: RecommendationInfo): Recommendation {
            return Recommendation(
                key = model.key,
                title = model.title,
                cover = model.cover,
                genres = model.genres,
                sourceId = model.sourceId,
                sourceName = model.sourceName
            )
        }
    }
}
