package ireader.domain.models.fonts

import ireader.domain.models.fonts.Item

data class FontResource(
    val items: List<Item>,
    val kind: String
)
