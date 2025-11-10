package ireader.domain.models.entities

import ireader.domain.services.BreakReason

/**
 * Data model for tracking chapter health status
 */
data class ChapterHealth(
    val chapterId: Long,
    val isBroken: Boolean,
    val breakReason: BreakReason?,
    val checkedAt: Long,
    val repairAttemptedAt: Long? = null,
    val repairSuccessful: Boolean? = null,
    val replacementSourceId: Long? = null
)
