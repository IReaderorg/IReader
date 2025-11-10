package ireader.data.chapterhealth

import data.ChapterHealth as ChapterHealthEntity
import ireader.domain.models.entities.ChapterHealth
import ireader.domain.services.BreakReason

val chapterHealthMapper: (
    chapter_id: Long,
    is_broken: Boolean,
    break_reason: String?,
    checked_at: Long,
    repair_attempted_at: Long?,
    repair_successful: Boolean?,
    replacement_source_id: Long?
) -> ChapterHealth = { chapterId, isBroken, breakReason, checkedAt, repairAttemptedAt, repairSuccessful, replacementSourceId ->
    ChapterHealth(
        chapterId = chapterId,
        isBroken = isBroken,
        breakReason = breakReason?.let { BreakReason.valueOf(it) },
        checkedAt = checkedAt,
        repairAttemptedAt = repairAttemptedAt,
        repairSuccessful = repairSuccessful,
        replacementSourceId = replacementSourceId
    )
}
