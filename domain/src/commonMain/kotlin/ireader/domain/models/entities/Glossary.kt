package ireader.domain.models.entities

import kotlinx.serialization.Serializable

@Serializable
data class Glossary(
    val id: Long = 0,
    val bookId: Long,
    val sourceTerm: String,
    val targetTerm: String,
    val termType: GlossaryTermType,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

enum class GlossaryTermType {
    CHARACTER,
    PLACE,
    ITEM,
    CUSTOM;
    
    companion object {
        fun fromString(value: String): GlossaryTermType {
            return when (value.lowercase()) {
                "character" -> CHARACTER
                "place" -> PLACE
                "item" -> ITEM
                "custom" -> CUSTOM
                else -> CUSTOM
            }
        }
    }
    
    override fun toString(): String {
        return when (this) {
            CHARACTER -> "character"
            PLACE -> "place"
            ITEM -> "item"
            CUSTOM -> "custom"
        }
    }
}

@Serializable
data class GlossaryExport(
    val bookId: Long,
    val bookTitle: String,
    val entries: List<Glossary>,
    val exportedAt: Long
)
