package ireader.domain.models.entities


data class History(
    val id: Long,
    val chapterId: Long,
    val readAt: Long?,
    val readDuration: Long,
    val progress: Float = 0f,
)

