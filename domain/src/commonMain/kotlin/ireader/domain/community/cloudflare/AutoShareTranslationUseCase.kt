package ireader.domain.community.cloudflare

import ireader.core.log.Log
import ireader.domain.community.CommunityPreferences
import ireader.domain.data.engines.TranslateEngine
import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter

/**
 * Use case for automatically sharing AI translations to the community.
 * 
 * This is triggered after a translation completes and checks:
 * 1. Auto-share is enabled
 * 2. The translation engine is AI-powered (not low-quality)
 * 3. Cloudflare is configured
 * 4. User has set a contributor name
 */
class AutoShareTranslationUseCase(
    private val communityPreferences: CommunityPreferences,
    private val translationRepository: CommunityTranslationRepository?,
    private val remoteRepository: RemoteRepository? = null
) {
    
    // AI engine IDs that produce high-quality translations
    private val aiEngineIds = setOf(
        TranslateEngine.OPENAI,      // 3L - OpenAI GPT
        TranslateEngine.DEEPSEEK,    // 4L - DeepSeek
        TranslateEngine.OLLAMA,      // 5L - Ollama (local AI)
        TranslateEngine.WEBSCRAPING, // 6L - ChatGPT/Gemini webscraping
        TranslateEngine.DEEPSEEK_WEBVIEW, // 7L - DeepSeek WebView
        TranslateEngine.GEMINI       // 8L - Google Gemini
    )
    
    /**
     * Check if the given engine is an AI engine.
     */
    fun isAiEngine(engineId: Long): Boolean {
        return engineId in aiEngineIds
    }
    
    /**
     * Check if auto-share should be triggered for this translation.
     */
    fun shouldAutoShare(engineId: Long): Boolean {
        // Check if auto-share is enabled
        if (!communityPreferences.autoShareTranslations().get()) return false
        
        // Check if AI-only mode is enabled
        val aiOnly = communityPreferences.autoShareAiOnly().get()
        if (aiOnly && !isAiEngine(engineId)) return false
        
        // Check if Cloudflare is configured
        if (!communityPreferences.isCloudflareConfigured()) return false
        
        // Check if contributor name is set
        if (communityPreferences.contributorName().get().isBlank()) return false
        
        return true
    }
    
    /**
     * Share a translation to the community.
     * 
     * @param book The book being translated
     * @param chapter The chapter being translated
     * @param originalContent Original chapter content (for hash)
     * @param translatedContent Translated content to share
     * @param sourceLanguage Source language code
     * @param targetLanguage Target language code
     * @param engineId Translation engine ID
     * @return Result with translation ID or error
     */
    suspend fun shareTranslation(
        book: Book,
        chapter: Chapter,
        originalContent: String,
        translatedContent: String,
        sourceLanguage: String,
        targetLanguage: String,
        engineId: Long
    ): Result<String> {
        if (translationRepository == null) {
            return Result.failure(Exception("Translation repository not available"))
        }
        
        if (!shouldAutoShare(engineId)) {
            return Result.failure(Exception("Auto-share conditions not met"))
        }
        
        // Get contributor info from Supabase if available
        var contributorId = ""
        var contributorName = communityPreferences.contributorName().get()
        
        try {
            val user = remoteRepository?.getCurrentUser()?.getOrNull()
            if (user != null) {
                contributorId = user.id
                // Use Supabase username if available, fallback to preference
                val supabaseUsername = user.username ?: user.email.substringBefore("@")
                if (supabaseUsername.isNotBlank()) {
                    contributorName = supabaseUsername
                }
            }
        } catch (e: Exception) {
            // Silently use preference name if Supabase user info unavailable
        }
        
        val engineName = TranslateEngine.valueOf(engineId).lowercase()
        
        return try {
            translationRepository.submitTranslation(
                originalContent = originalContent,
                translatedContent = translatedContent,
                bookTitle = book.title,
                bookAuthor = book.author,
                bookCover = book.cover,
                chapterName = chapter.name,
                chapterNumber = chapter.number,
                sourceLanguage = sourceLanguage,
                targetLanguage = targetLanguage,
                engineId = engineName,
                contributorId = contributorId,
                contributorName = contributorName
            )
        } catch (e: Exception) {
            Log.error("AutoShare: Failed to share translation", e)
            Result.failure(e)
        }
    }
    
    /**
     * Check if a community translation exists before translating.
     * Returns the existing translation if found.
     */
    suspend fun checkExistingTranslation(
        originalContent: String,
        targetLanguage: String,
        engineId: Long
    ): TranslationLookupResult {
        if (translationRepository == null) {
            return TranslationLookupResult(found = false)
        }
        
        if (!communityPreferences.checkCommunityFirst().get()) {
            return TranslationLookupResult(found = false)
        }
        
        if (!communityPreferences.isCloudflareConfigured()) {
            return TranslationLookupResult(found = false)
        }
        
        val engineName = TranslateEngine.valueOf(engineId).lowercase()
        
        return try {
            translationRepository.findExistingTranslation(
                originalContent = originalContent,
                targetLanguage = targetLanguage,
                engineId = engineName
            )
        } catch (e: Exception) {
            Log.error("AutoShare: Failed to check existing translation", e)
            TranslationLookupResult(found = false)
        }
    }
    
    /**
     * Check if a community translation exists by book and chapter.
     */
    suspend fun checkExistingByChapter(
        bookTitle: String,
        bookAuthor: String,
        chapterNumber: Float,
        targetLanguage: String
    ): TranslationLookupResult {
        if (translationRepository == null) {
            return TranslationLookupResult(found = false)
        }
        
        if (!communityPreferences.checkCommunityFirst().get()) {
            return TranslationLookupResult(found = false)
        }
        
        if (!communityPreferences.isCloudflareConfigured()) {
            return TranslationLookupResult(found = false)
        }
        
        return try {
            translationRepository.findByBookChapter(
                bookTitle = bookTitle,
                bookAuthor = bookAuthor,
                chapterNumber = chapterNumber,
                targetLanguage = targetLanguage
            )
        } catch (e: Exception) {
            Log.error("AutoShare: Failed to check existing translation by chapter", e)
            TranslationLookupResult(found = false)
        }
    }
}
