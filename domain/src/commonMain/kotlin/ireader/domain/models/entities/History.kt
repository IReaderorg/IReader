package ireader.domain.models.entities

import androidx.compose.runtime.Immutable

@Immutable
data class History(
    val id: Long,
    val chapterId: Long,
    val readAt: Long?,
    val readDuration: Long,
    val progress: Float = 0f,
)
