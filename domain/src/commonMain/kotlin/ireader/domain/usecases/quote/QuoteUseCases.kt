package ireader.domain.usecases.quote

import ireader.domain.models.quote.QuoteCardStyle
import ireader.domain.preferences.prefs.ReadingBuddyPreferences

/**
 * Use cases for managing quote preferences (card styles, etc.)
 * 
 * Note: Quote storage and retrieval is handled by QuoteRepository (Supabase)
 */
class QuoteUseCases(
    private val preferences: ReadingBuddyPreferences
) {
    
    /**
     * Set preferred card style
     */
    suspend fun setCardStyle(style: QuoteCardStyle) {
        preferences.preferredCardStyle().set(style.name)
    }
    
    /**
     * Get preferred card style
     */
    fun getCardStyle(): QuoteCardStyle {
        val styleStr = preferences.preferredCardStyle().get()
        return try {
            QuoteCardStyle.valueOf(styleStr)
        } catch (e: Exception) {
            QuoteCardStyle.GRADIENT_SUNSET
        }
    }
    
    /**
     * Check if daily quote should be shown
     */
    fun shouldShowDailyQuote(): Boolean {
        return preferences.showDailyQuote().get()
    }
    
    /**
     * Set whether to show daily quote
     */
    suspend fun setShowDailyQuote(show: Boolean) {
        preferences.showDailyQuote().set(show)
    }
}
