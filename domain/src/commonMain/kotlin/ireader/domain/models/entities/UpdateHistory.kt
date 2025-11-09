package ireader.domain.models.entities

data class UpdateHistory(
    val id: Long = 0,
    val bookId: Long,
    val bookTitle: String,
    val chaptersAdded: Int,
    val timestamp: Long
)
