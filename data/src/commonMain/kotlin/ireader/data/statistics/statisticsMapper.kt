package ireader.data.statistics

data class ReadingStatisticsDb(
    val id: Long,
    val totalChaptersRead: Long,
    val totalReadingTimeMinutes: Long,
    val readingStreak: Long,
    val lastReadDate: Long?,
    val totalWordsRead: Long
)

val statisticsMapper: (
    _id: Long,
    total_chapters_read: Long,
    total_reading_time_minutes: Long,
    reading_streak: Long,
    last_read_date: Long?,
    total_words_read: Long
) -> ReadingStatisticsDb = { id, chaptersRead, readingTime, streak, lastRead, wordsRead ->
    ReadingStatisticsDb(
        id = id,
        totalChaptersRead = chaptersRead,
        totalReadingTimeMinutes = readingTime,
        readingStreak = streak,
        lastReadDate = lastRead,
        totalWordsRead = wordsRead
    )
}
