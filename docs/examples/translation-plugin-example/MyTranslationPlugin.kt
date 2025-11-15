package com.example.mytranslation

import ireader.domain.plugins.Plugin
import ireader.domain.plugins.PluginContext
import ireader.domain.plugins.PluginManifest
import ireader.domain.plugins.TranslationPlugin
import ireader.domain.plugins.LanguagePair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Example translation plugin demonstrating integration with a translation API.
 * 
 * This plugin uses a hypothetical translation service to translate text between languages.
 * In a real implementation, you would integrate with services like Google Translate,
 * DeepL, or your own translation backend.
 */
class MyTranslationPlugin : TranslationPlugin {
    
    private lateinit var context: PluginContext
    private val cache = mutableMapOf<String, String>()
    
    override val manifest: PluginManifest by lazy {
        PluginManifest(
            id = "com.example.mytranslation",
            name = "Example Translator",
            version = "1.0.0",
            versionCode = 1,
            description = "High-quality translation service supporting 50+ languages",
            author = PluginAuthor(
                name = "Example Developer",
                email = "dev@example.com",
                website = "https://example.com"
            ),
            type = PluginType.TRANSLATION,
            permissions = listOf(PluginPermission.NETWORK, PluginPermission.PREFERENCES),
            minIReaderVersion = "1.0.0",
            platforms = listOf(Platform.ANDROID, Platform.IOS, Platform.DESKTOP),
            monetization = PluginMonetization.Freemium(
                features = listOf(
                    PremiumFeature(
                        id = "unlimited_translations",
                        name = "Unlimited Translations",
                        description = "Remove daily translation limit",
                        price = 4.99,
                        currency = "USD"
                    )
                )
            ),
            iconUrl = "icon.png",
            screenshotUrls = listOf("screenshot1.png")
        )
    }
    
    override fun initialize(context: PluginContext) {
        this.context = context
        context.logger.info("Translation plugin initialized")
    }
    
    override fun cleanup() {
        cache.clear()
    }
    
    override suspend fun translate(text: String, from: String, to: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Validate inputs
                if (text.isEmpty()) {
                    return@withContext Result.success("")
                }
                
                if (text.length > MAX_TEXT_LENGTH) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Text exceeds maximum length of $MAX_TEXT_LENGTH characters")
                    )
                }
                
                if (!isValidLanguageCode(from) || !isValidLanguageCode(to)) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Invalid language code")
                    )
                }
                
                // Check cache
                val cacheKey = "$text:$from:$to"
                cache[cacheKey]?.let {
                    context.logger.debug("Cache hit for: $cacheKey")
                    return@withContext Result.success(it)
                }
                
                // Check rate limits for free users
                if (!isPremiumUser() && !checkRateLimit()) {
                    return@withContext Result.failure(
                        RateLimitException("Daily translation limit reached. Upgrade to premium for unlimited translations.")
                    )
                }
                
                // Perform translation
                context.logger.debug("Translating: $text from $from to $to")
                val translated = performTranslation(text, from, to)
                
                // Cache result
                cache[cacheKey] = translated
                
                // Update usage counter
                incrementUsageCounter()
                
                Result.success(translated)
            } catch (e: NetworkException) {
                context.logger.error("Network error during translation", e)
                Result.failure(TranslationException("Network error. Please check your connection.", e))
            } catch (e: Exception) {
                context.logger.error("Translation failed", e)
                Result.failure(TranslationException("Translation failed: ${e.message}", e))
            }
        }
    }
    
    override suspend fun translateBatch(
        texts: List<String>,
        from: String,
        to: String
    ): Result<List<String>> {
        return withContext(Dispatchers.IO) {
            try {
                if (texts.isEmpty()) {
                    return@withContext Result.success(emptyList())
                }
                
                // Check if batch translation is available
                if (!isPremiumUser() && texts.size > FREE_BATCH_LIMIT) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Free users can translate up to $FREE_BATCH_LIMIT texts at once")
                    )
                }
                
                context.logger.debug("Batch translating ${texts.size} texts from $from to $to")
                
                // Use API's batch endpoint for efficiency
                val apiKey = getApiKey()
                val response = context.networkClient.post("https://api.example.com/translate/batch") {
                    headers {
                        "Authorization" to "Bearer $apiKey"
                        "Content-Type" to "application/json"
                    }
                    json {
                        "texts" to texts
                        "from" to from
                        "to" to to
                    }
                }
                
                if (response.isSuccess) {
                    val translations = response.body.getArray("translations")
                    Result.success(translations)
                } else {
                    Result.failure(TranslationException("Batch translation failed: ${response.code}"))
                }
            } catch (e: Exception) {
                context.logger.error("Batch translation failed", e)
                Result.failure(TranslationException("Batch translation failed: ${e.message}", e))
            }
        }
    }
    
    override fun getSupportedLanguages(): List<LanguagePair> {
        // Return all supported language pairs
        val languages = listOf(
            "en", "es", "fr", "de", "it", "pt", "ru", "ja", "ko", "zh",
            "ar", "hi", "tr", "pl", "nl", "sv", "da", "fi", "no", "cs"
        )
        
        return languages.flatMap { from ->
            languages.filter { to -> from != to }.map { to ->
                LanguagePair(from, to)
            }
        }
    }
    
    override fun requiresApiKey(): Boolean = true
    
    override fun configureApiKey(key: String) {
        context.preferences.putString(PREF_API_KEY, key)
        context.logger.info("API key configured")
    }
    
    // Private helper methods
    
    private fun getApiKey(): String {
        return context.preferences.getString(PREF_API_KEY, "")
            .takeIf { it.isNotEmpty() }
            ?: throw IllegalStateException("API key not configured")
    }
    
    private suspend fun performTranslation(text: String, from: String, to: String): String {
        val apiKey = getApiKey()
        
        val response = context.networkClient.post("https://api.example.com/translate") {
            headers {
                "Authorization" to "Bearer $apiKey"
                "Content-Type" to "application/json"
            }
            json {
                "text" to text
                "from" to from
                "to" to to
            }
        }
        
        return if (response.isSuccess) {
            response.body.getString("translated")
        } else {
            throw TranslationException("API returned error: ${response.code}")
        }
    }
    
    private fun isValidLanguageCode(code: String): Boolean {
        return code.length == 2 && code.all { it.isLowerCase() }
    }
    
    private fun isPremiumUser(): Boolean {
        return context.monetization.isFeaturePurchased(
            manifest.id,
            "unlimited_translations"
        )
    }
    
    private fun checkRateLimit(): Boolean {
        val today = getCurrentDate()
        val lastDate = context.preferences.getString(PREF_LAST_DATE, "")
        val count = context.preferences.getInt(PREF_USAGE_COUNT, 0)
        
        return if (lastDate != today) {
            // New day, reset counter
            context.preferences.putString(PREF_LAST_DATE, today)
            context.preferences.putInt(PREF_USAGE_COUNT, 0)
            true
        } else {
            count < FREE_DAILY_LIMIT
        }
    }
    
    private fun incrementUsageCounter() {
        val count = context.preferences.getInt(PREF_USAGE_COUNT, 0)
        context.preferences.putInt(PREF_USAGE_COUNT, count + 1)
    }
    
    private fun getCurrentDate(): String {
        return java.time.LocalDate.now().toString()
    }
    
    companion object {
        private const val MAX_TEXT_LENGTH = 5000
        private const val FREE_DAILY_LIMIT = 100
        private const val FREE_BATCH_LIMIT = 10
        
        private const val PREF_API_KEY = "api_key"
        private const val PREF_LAST_DATE = "last_date"
        private const val PREF_USAGE_COUNT = "usage_count"
    }
}

// Custom exceptions
class TranslationException(message: String, cause: Throwable? = null) : Exception(message, cause)
class RateLimitException(message: String) : Exception(message)
class NetworkException(message: String) : Exception(message)
