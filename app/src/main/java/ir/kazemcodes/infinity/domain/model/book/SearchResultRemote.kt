package ir.kazemcodes.infinity.domain.model.book

data class SearchResultRemote(
    val name: String,
    val link: String,
    val tags: List<String>,
    val rating: Float,
    val desc: String,
) {
    val id: String
        get() = link.substringAfterLast("_")
}
