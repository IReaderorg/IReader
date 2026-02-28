package ireader.domain.usecases.translate

import ireader.domain.data.engines.TranslateEngine

/**
 * Gemini Nano - Google's on-device AI translation
 * 
 * Requires Android 14+ (API 34+)
 * Best quality offline translation available
 */
expect class GeminiNano() : TranslateEngine
