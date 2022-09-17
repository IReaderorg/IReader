package ireader.common.models.fonts

data class Item(
    val category: String,
    val family: String,
    val files: Files,
    val kind: String,
    val lastModified: String,
    val subsets: List<String>,
    val variants: List<String>,
    val version: String
)
