package ireader.domain.usecases.level

import ireader.domain.models.entities.ReaderLevel

/**
 * Use case for calculating a reader's level from their total reading time.
 * 
 * Level system:
 * - Level = floor(total_reading_time_minutes / 60) + 1
 * - XP = total_reading_time_minutes % 60 (progress to next level)
 * - Each level requires 60 minutes of reading
 * 
 * Level titles:
 * - 1-5: Novice Reader
 * - 6-15: Curious Reader
 * - 16-30: Avid Reader
 * - 31-50: Bookworm
 * - 51-100: Master Reader
 * - 101-200: Literary Legend
 * - 200+: Reading Deity
 */
class CalculateLevelUseCase {
    operator fun invoke(totalMinutes: Long): ReaderLevel {
        return ReaderLevel.fromMinutes(totalMinutes)
    }
}
