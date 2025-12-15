package ireader.domain.community.cloudflare

import ireader.core.log.Log
import ireader.domain.community.CommunityPreferences
import ireader.domain.data.engines.TranslateEngine
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
    private val translationRepository: CommunityTranslationRepository?
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
        if (!communityPreferences.autoShareTranslations().get()) {
            return false
        }
        
        // Check if AI-only mode is enabled
        if (communityPreferences.autoShareAiOnly().get() && !isAiEngine(engineId)) {
            Log.debug { "AutoShare: Skipping non-AI engine $engineId" }
            return false
        }
        
        // Check if Cloudflare is configured
        if (!communityPreferences.isCloudflareConfigured()) {
            Log.debug { "AutoShare: Cloudflare not configured" }
            return false
        }
        
        // Check if contributor name is set
        if (communityPreferences.contributorName().get().isBlank()) {
            Log.debug { "AutoShare: Contributor name not set" }
            return false
        }
        
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
        
        val contributorName = communityPreferences.contributorName().get()
        val contributorId = "" // TODO: Get from auth if available
        
        val engineName = TranslateEngine.valueOf(engineId).lowercase()
        
        Log.info { "AutoShare: Sharing translation for ${book.title} - ${chapter.name} ($engineName)" }
        
        return try {
            translationRepository.submitTranslation(
                originalContent = originalContent,
                translatedContent = translatedContent,
                bookTitle = book.title,
                bookAuthor = book.author,
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
