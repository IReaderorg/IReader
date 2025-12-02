package ireader.domain.preferences.prefs

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore

/**
 * Preferences for the Reading Buddy feature
 */
class ReadingBuddyPreferences(
    private val preferenceStore: PreferenceStore
) {
    
    fun buddyEnabled(): Preference<Boolean> =
        preferenceStore.getBoolean("reading_buddy_enabled", true)
    
    fun buddyLevel(): Preference<Int> =
        preferenceStore.getInt("reading_buddy_level", 1)
    
    fun buddyExperience(): Preference<Int> =
        preferenceStore.getInt("reading_buddy_experience", 0)
    
    fun totalBooksRead(): Preference<Int> =
        preferenceStore.getInt("reading_buddy_books_read", 0)
    
    fun totalChaptersRead(): Preference<Int> =
        preferenceStore.getInt("reading_buddy_chapters_read", 0)
    
    fun currentStreak(): Preference<Int> =
        preferenceStore.getInt("reading_buddy_current_streak", 0)
    
    fun longestStreak(): Preference<Int> =
        preferenceStore.getInt("reading_buddy_longest_streak", 0)
    
    fun lastReadDate(): Preference<Long> =
        preferenceStore.getLong("reading_buddy_last_read_date", 0L)
    
    fun lastInteractionTime(): Preference<Long> =
        preferenceStore.getLong("reading_buddy_last_interaction", 0L)
    
    fun unlockedAchievements(): Preference<String> =
        preferenceStore.getString("reading_buddy_achievements", "")
    
    fun showDailyQuote(): Preference<Boolean> =
        preferenceStore.getBoolean("show_daily_quote", true)
    
    fun lastDailyQuoteDate(): Preference<Long> =
        preferenceStore.getLong("last_daily_quote_date", 0L)
    
    fun savedQuotesJson(): Preference<String> =
        preferenceStore.getString("saved_quotes_json", "[]")
    
    fun preferredCardStyle(): Preference<String> =
        preferenceStore.getString("quote_card_style", "GRADIENT_SUNSET")
}
