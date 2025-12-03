package ireader.domain.usecases.translate

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter

/**
 * iOS implementation of GoogleTranslateML
 * 
 * TODO: Implement using Apple's Translation framework (iOS 17.4+)
 * or a third-party translation API
 */
actual class GoogleTranslateML : TranslateEngine {
    actual override suspend fun translate(
        texts: List<String>,
        source: String,
        target: String
    ): List<String> {
        // TODO: Implement using Apple Translation framework
        return texts
    }
    
    override suspend fun translate(
        book: Book,
        chapters: List<Chapter>,
        source: String,
        target: String
    ): List<Chapter> {
        // TODO: Implement
        return chapters
    }
    
    override fun isAvailable(): Boolean = false
    
    override fun supportedLanguages(): List<String> = emptyList()
}
