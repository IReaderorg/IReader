package ireader.domain.usecases.spiritstone

import ireader.domain.models.entities.SpiritStoneTransaction
import ireader.domain.models.entities.TransactionType
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Use case for awarding Spirit Stones to users based on their activity.
 * 
 * Earning rates:
 * - 1 Spirit Stone per 30 minutes of reading
 * - 5 Spirit Stones per book completed
 * - 10 Spirit Stones per 7-day streak milestone
 * - 3 Spirit Stones per chapter read
 * - 1 Spirit Stone per daily login
 */
class EarnSpiritStonesUseCase {

    fun fromReading(minutes: Long): SpiritStoneTransaction {
        val stones = minutes / 30
        return SpiritStoneTransaction(
            id = "reading_${currentTimeToLong()}",
            userId = "",
            amount = stones,
            type = TransactionType.READING_REWARD,
            description = "Earned $stones Spirit Stones from reading ${minutes}min",
            timestamp = currentTimeToLong()
        )
    }

    fun fromBookCompleted(): SpiritStoneTransaction {
        return SpiritStoneTransaction(
            id = "book_${currentTimeToLong()}",
            userId = "",
            amount = 5,
            type = TransactionType.BOOK_COMPLETED,
            description = "Earned 5 Spirit Stones for completing a book",
            timestamp = currentTimeToLong()
        )
    }

    fun fromStreak(streakDays: Int): SpiritStoneTransaction {
        return SpiritStoneTransaction(
            id = "streak_${currentTimeToLong()}",
            userId = "",
            amount = 10,
            type = TransactionType.STREAK_MILESTONE,
            description = "Earned 10 Spirit Stones for a $streakDays-day streak",
            timestamp = currentTimeToLong()
        )
    }

    fun fromChapterRead(): SpiritStoneTransaction {
        return SpiritStoneTransaction(
            id = "chapter_${currentTimeToLong()}",
            userId = "",
            amount = 3,
            type = TransactionType.CHAPTER_READ,
            description = "Earned 3 Spirit Stones for reading a chapter",
            timestamp = currentTimeToLong()
        )
    }

    fun fromDailyLogin(): SpiritStoneTransaction {
        return SpiritStoneTransaction(
            id = "login_${currentTimeToLong()}",
            userId = "",
            amount = 1,
            type = TransactionType.DAILY_LOGIN,
            description = "Earned 1 Spirit Stone for daily login",
            timestamp = currentTimeToLong()
        )
    }
}
