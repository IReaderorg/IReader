package ireader.domain.usecases.translate

import ireader.domain.data.engines.TranslateEngine

/**
 * Free Google Translate Web API Engine (expect declaration)
 * 
 * Uses the free Google Translate API that Chrome uses.
 * Available on all platforms.
 */
expect class GoogleTranslateFree() : TranslateEngine
