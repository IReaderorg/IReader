package ireader.domain.models.entities

import ireader.core.source.model.Page
import kotlinx.serialization.Serializable

@Serializable
data class TranslatedChapter(
    val id: Long = 0,
    val chapterId: Long,
    val bookId: Long,
    val sourceLanguage: String,
    val targetLanguage: String,
    val translatorEngineId: Long,
    val translatedContent: List<Page>,
    val createdAt: Long,
    val updatedAt: Long
)
