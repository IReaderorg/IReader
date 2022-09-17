package ireader.common.models.entities

import java.util.*


data class History(
    val id: Long,
    val chapterId: Long,
    val readAt: Long?,
    val readDuration: Long,
)

