package ireader.domain.usecases.translate

import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.deepseek_api_key_invalid
import ireader.i18n.resources.deepseek_api_key_not_set
import ireader.i18n.resources.deepseek_payment_required
import ireader.i18n.resources.gemini_api_key_invalid
import ireader.i18n.resources.gemini_api_key_not_set
import ireader.i18n.resources.gemini_payment_required
import ireader.i18n.resources.no_text_to_translate
import ireader.i18n.resources.openai_api_key_invalid
import ireader.i18n.resources.openai_api_key_not_set
import ireader.i18n.resources.openai_quota_exceeded
import ireader.i18n.resources.sign_in_to_chatgpt
import ireader.i18n.resources.sign_in_to_deepseek
import ireader.i18n.resources.translation_api_key_invalid
import ireader.i18n.resources.translation_api_key_not_set
import ireader.i18n.resources.translation_auth_required
import ireader.i18n.resources.translation_captcha_required
import ireader.i18n.resources.translation_empty_response
import ireader.i18n.resources.translation_engine_not_available
import ireader.i18n.resources.translation_language_model_not_available
import ireader.i18n.resources.translation_language_pair_not_supported
import ireader.i18n.resources.translation_model_not_found
import ireader.i18n.resources.translation_model_not_found_with_name
import ireader.i18n.resources.translation_network_error
import ireader.i18n.resources.translation_parse_error
import ireader.i18n.resources.translation_plugin_error
import ireader.i18n.resources.translation_quota_exceeded
import ireader.i18n.resources.translation_rate_limit
import ireader.i18n.resources.translation_rate_limit_with_retry
import ireader.i18n.resources.translation_same_as_original
import ireader.i18n.resources.translation_server_error
import ireader.i18n.resources.translation_server_error_with_code
import ireader.i18n.resources.translation_text_too_long
import ireader.i18n.resources.translation_text_too_long_with_limit
import ireader.i18n.resources.translation_timeout
import ireader.i18n.resources.translation_timeout_with_seconds
import ireader.i18n.resources.translation_unknown_error
import ireader.i18n.resources.translation_unknown_error_with_details

/**
 * Sealed class representing specific translation errors with user-friendly messages.
 * Each error type provides clear information about what went wrong and how to fix it.
 */
sealed class TranslationError {
    
    /**
     * API key is missing or not configured
     */
    data class ApiKeyNotSet(
        val engineName: String
    ) : TranslationError() {
        override fun toUiText(): UiText = when (engineName) {
            "OpenAI" -> UiText.MStringResource(Res.string.openai_api_key_not_set)
            "DeepSeek" -> UiText.MStringResource(Res.string.deepseek_api_key_not_set)
            "Google Gemini" -> UiText.MStringResource(Res.string.gemini_api_key_not_set)
            else -> UiText.MStringResource(Res.string.translation_api_key_not_set, arrayOf(engineName))
        }
    }
    
    /**
     * API key is invalid or expired
     */
    data class ApiKeyInvalid(
        val engineName: String
    ) : TranslationError() {
        override fun toUiText(): UiText = when (engineName) {
            "OpenAI" -> UiText.MStringResource(Res.string.openai_api_key_invalid)
            "DeepSeek" -> UiText.MStringResource(Res.string.deepseek_api_key_invalid)
            "Google Gemini" -> UiText.MStringResource(Res.string.gemini_api_key_invalid)
            else -> UiText.MStringResource(Res.string.translation_api_key_invalid, arrayOf(engineName))
        }
    }
    
    /**
     * API quota exceeded or rate limited
     */
    data class QuotaExceeded(
        val engineName: String
    ) : TranslationError() {
        override fun toUiText(): UiText = when (engineName) {
            "OpenAI" -> UiText.MStringResource(Res.string.openai_quota_exceeded)
            "DeepSeek" -> UiText.MStringResource(Res.string.deepseek_payment_required)
            "Google Gemini" -> UiText.MStringResource(Res.string.gemini_payment_required)
            else -> UiText.MStringResource(Res.string.translation_quota_exceeded, arrayOf(engineName))
        }
    }
    
    /**
     * Rate limit exceeded - need to wait
     */
    data class RateLimitExceeded(
        val engineName: String,
        val retryAfterSeconds: Int? = null
    ) : TranslationError() {
        override fun toUiText(): UiText = if (retryAfterSeconds != null) {
            UiText.MStringResource(Res.string.translation_rate_limit_with_retry, arrayOf(engineName, retryAfterSeconds))
        } else {
            UiText.MStringResource(Res.string.translation_rate_limit, arrayOf(engineName))
        }
    }
    
    /**
     * Network connectivity issue
     */
    data class NetworkError(
        val engineName: String,
        val details: String? = null
    ) : TranslationError() {
        override fun toUiText(): UiText = UiText.MStringResource(
            Res.string.translation_network_error,
            arrayOf(engineName)
        )
    }
    
    /**
     * Server error from the translation service
     */
    data class ServerError(
        val engineName: String,
        val statusCode: Int? = null
    ) : TranslationError() {
        override fun toUiText(): UiText = if (statusCode != null) {
            UiText.MStringResource(Res.string.translation_server_error_with_code, arrayOf(engineName, statusCode))
        } else {
            UiText.MStringResource(Res.string.translation_server_error, arrayOf(engineName))
        }
    }
    
    /**
     * Language model not downloaded (for offline engines like Google ML Kit)
     */
    data class LanguageModelNotAvailable(
        val sourceLanguage: String,
        val targetLanguage: String
    ) : TranslationError() {
        override fun toUiText(): UiText = UiText.MStringResource(
            Res.string.translation_language_model_not_available,
            arrayOf(sourceLanguage, targetLanguage)
        )
    }
    
    /**
     * Language pair not supported by the engine
     */
    data class LanguagePairNotSupported(
        val engineName: String,
        val sourceLanguage: String,
        val targetLanguage: String
    ) : TranslationError() {
        override fun toUiText(): UiText = UiText.MStringResource(
            Res.string.translation_language_pair_not_supported,
            arrayOf(engineName, sourceLanguage, targetLanguage)
        )
    }
    
    /**
     * Empty or no text to translate
     */
    object NoTextToTranslate : TranslationError() {
        override fun toUiText(): UiText = UiText.MStringResource(Res.string.no_text_to_translate)
    }
    
    /**
     * Empty response from translation service
     */
    data class EmptyResponse(
        val engineName: String
    ) : TranslationError() {
        override fun toUiText(): UiText = UiText.MStringResource(
            Res.string.translation_empty_response,
            arrayOf(engineName)
        )
    }
    
    /**
     * Response parsing failed
     */
    data class ResponseParseError(
        val engineName: String,
        val details: String? = null
    ) : TranslationError() {
        override fun toUiText(): UiText = UiText.MStringResource(
            Res.string.translation_parse_error,
            arrayOf(engineName)
        )
    }
    
    /**
     * Authentication required (for WebView-based engines)
     */
    data class AuthenticationRequired(
        val engineName: String
    ) : TranslationError() {
        override fun toUiText(): UiText = when (engineName) {
            "ChatGPT WebView" -> UiText.MStringResource(Res.string.sign_in_to_chatgpt)
            "DeepSeek WebView" -> UiText.MStringResource(Res.string.sign_in_to_deepseek)
            else -> UiText.MStringResource(Res.string.translation_auth_required, arrayOf(engineName))
        }
    }
    
    /**
     * CAPTCHA verification required
     */
    data class CaptchaRequired(
        val engineName: String
    ) : TranslationError() {
        override fun toUiText(): UiText = UiText.MStringResource(Res.string.translation_captcha_required, arrayOf(engineName))
    }
    
    /**
     * Translation timeout
     */
    data class Timeout(
        val engineName: String,
        val timeoutSeconds: Int? = null
    ) : TranslationError() {
        override fun toUiText(): UiText = if (timeoutSeconds != null) {
            UiText.MStringResource(Res.string.translation_timeout_with_seconds, arrayOf(engineName, timeoutSeconds))
        } else {
            UiText.MStringResource(Res.string.translation_timeout, arrayOf(engineName))
        }
    }
    
    /**
     * Text too long for the engine to process
     */
    data class TextTooLong(
        val engineName: String,
        val maxChars: Int? = null
    ) : TranslationError() {
        override fun toUiText(): UiText = if (maxChars != null) {
            UiText.MStringResource(Res.string.translation_text_too_long_with_limit, arrayOf(engineName, maxChars))
        } else {
            UiText.MStringResource(Res.string.translation_text_too_long, arrayOf(engineName))
        }
    }
    
    /**
     * Engine not available in this build (e.g., ML Kit on F-Droid)
     */
    data class EngineNotAvailable(
        val engineName: String
    ) : TranslationError() {
        override fun toUiText(): UiText = UiText.MStringResource(
            Res.string.translation_engine_not_available,
            arrayOf(engineName)
        )
    }
    
    /**
     * Model not found (for Gemini models)
     */
    data class ModelNotFound(
        val engineName: String,
        val modelName: String? = null
    ) : TranslationError() {
        override fun toUiText(): UiText = if (modelName != null) {
            UiText.MStringResource(Res.string.translation_model_not_found_with_name, arrayOf(engineName, modelName))
        } else {
            UiText.MStringResource(Res.string.translation_model_not_found, arrayOf(engineName))
        }
    }
    
    /**
     * Plugin error
     */
    data class PluginError(
        val pluginName: String,
        val details: String? = null
    ) : TranslationError() {
        override fun toUiText(): UiText = UiText.MStringResource(
            Res.string.translation_plugin_error,
            arrayOf(pluginName)
        )
    }
    
    /**
     * Translation returned same text as original (translation may have failed silently)
     */
    data class SameAsOriginal(
        val engineName: String
    ) : TranslationError() {
        override fun toUiText(): UiText = UiText.MStringResource(
            Res.string.translation_same_as_original,
            arrayOf(engineName)
        )
    }
    
    /**
     * Unknown/unexpected error
     */
    data class Unknown(
        val engineName: String,
        val exception: Exception? = null
    ) : TranslationError() {
        override fun toUiText(): UiText = if (exception != null) {
            UiText.MStringResource(Res.string.translation_unknown_error_with_details, arrayOf(engineName, exception.message ?: "Unknown error"))
        } else {
            UiText.MStringResource(Res.string.translation_unknown_error, arrayOf(engineName))
        }
    }
    
    /**
     * Convert to UiText for display in the UI
     */
    abstract fun toUiText(): UiText
    
    /**
     * Get a user-friendly title for this error
     */
    fun getTitle(): String = when (this) {
        is ApiKeyNotSet -> "API Key Required"
        is ApiKeyInvalid -> "Invalid API Key"
        is QuotaExceeded -> "Quota Exceeded"
        is RateLimitExceeded -> "Rate Limited"
        is NetworkError -> "Network Error"
        is ServerError -> "Server Error"
        is LanguageModelNotAvailable -> "Language Model Required"
        is LanguagePairNotSupported -> "Language Not Supported"
        is NoTextToTranslate -> "No Content"
        is EmptyResponse -> "Empty Response"
        is ResponseParseError -> "Response Error"
        is AuthenticationRequired -> "Sign In Required"
        is CaptchaRequired -> "Verification Required"
        is Timeout -> "Request Timeout"
        is TextTooLong -> "Text Too Long"
        is EngineNotAvailable -> "Engine Unavailable"
        is ModelNotFound -> "Model Not Found"
        is PluginError -> "Plugin Error"
        is SameAsOriginal -> "Translation Unchanged"
        is Unknown -> "Translation Error"
    }
    
    companion object {
        /**
         * Parse an exception and return the appropriate TranslationError
         */
        fun fromException(
            exception: Exception,
            engineName: String,
            sourceLanguage: String? = null,
            targetLanguage: String? = null
        ): TranslationError {
            val message = exception.message?.lowercase() ?: ""
            
            return when {
                // API Key errors
                message.contains("api key not set") || message.contains("api_key_not_set") ->
                    ApiKeyNotSet(engineName)
                
                message.contains("401") || message.contains("unauthorized") || 
                message.contains("invalid api key") || message.contains("api key is invalid") ->
                    ApiKeyInvalid(engineName)
                
                // Quota and rate limit errors
                message.contains("402") || message.contains("payment required") ||
                message.contains("quota exceeded") || message.contains("quota") ->
                    QuotaExceeded(engineName)
                
                message.contains("429") || message.contains("rate limit") ->
                    RateLimitExceeded(engineName)
                
                // Network errors
                message.contains("network") || message.contains("connection") ||
                message.contains("socket") || message.contains("timeout") ->
                    NetworkError(engineName, exception.message)
                
                // Server errors
                message.contains("500") || message.contains("501") || 
                message.contains("502") || message.contains("503") ->
                    ServerError(engineName, extractStatusCode(message))
                
                // Language model errors
                message.contains("language model") || message.contains("model not downloaded") ->
                    LanguageModelNotAvailable(sourceLanguage ?: "unknown", targetLanguage ?: "unknown")
                
                // Language pair errors
                message.contains("language pair") || message.contains("not supported") ->
                    LanguagePairNotSupported(engineName, sourceLanguage ?: "unknown", targetLanguage ?: "unknown")
                
                // Empty text
                message.contains("no text") || message.contains("empty text") ->
                    NoTextToTranslate
                
                // Empty response
                message.contains("empty response") || message.contains("no response") ->
                    EmptyResponse(engineName)
                
                // Parse errors
                message.contains("parse") || message.contains("json") ||
                message.contains("deserialize") ->
                    ResponseParseError(engineName, exception.message)
                
                // Authentication errors
                message.contains("sign in") || message.contains("login") ||
                message.contains("logged out") ->
                    AuthenticationRequired(engineName)
                
                // CAPTCHA
                message.contains("captcha") || message.contains("verification") ->
                    CaptchaRequired(engineName)
                
                // Timeout
                message.contains("timed out") || message.contains("timeout") ->
                    Timeout(engineName)
                
                // Text too long
                message.contains("too long") || message.contains("text length") ||
                message.contains("max_tokens") ->
                    TextTooLong(engineName)
                
                // Engine not available
                message.contains("not available") || message.contains("f-droid") ||
                message.contains("play store") ->
                    EngineNotAvailable(engineName)
                
                // Model not found
                message.contains("model not found") || message.contains("404") ->
                    ModelNotFound(engineName)
                
                // Plugin errors
                message.contains("plugin") ->
                    PluginError(engineName, exception.message)
                
                // Default to unknown
                else -> Unknown(engineName, exception)
            }
        }
        
        private fun extractStatusCode(message: String): Int? {
            val codes = listOf(400, 401, 402, 403, 404, 429, 500, 501, 502, 503)
            return codes.find { message.contains(it.toString()) }
        }
    }
}
