package ir.kazemcodes.infinity.domain.model.book

data class ChapterRemote(
    val title: String,
    val link: String,
    val index: Int? = null,
    val premium: Boolean = false
)
