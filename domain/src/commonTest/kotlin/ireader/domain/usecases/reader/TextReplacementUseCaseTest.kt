package ireader.domain.usecases.reader

import ireader.domain.data.repository.TextReplacementRepository
import ireader.domain.models.entities.TextReplacement
import ireader.domain.preferences.prefs.ReaderPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for TextReplacementUseCase focusing on:
 * - Issue #10: runBlocking in suspend context should be removed
 * - Issue #17: Regex detection logic duplication should be extracted
 */
class TextReplacementUseCaseTest {
    
    private class FakeTextReplacementRepository : TextReplacementRepository {
        var replacements: List<TextReplacement> = emptyList()
        var enabledGlobalReplacements: List<TextReplacement> = emptyList()
        var enabledBookReplacements: Map<Long, List<TextReplacement>> = emptyMap()
        
        override fun getGlobalReplacements(): Flow<List<TextReplacement>> = flowOf(replacements)
        
        override fun getReplacementsForBook(bookId: Long): Flow<List<TextReplacement>> = 
            flowOf(replacements.filter { it.bookId == bookId })
        
        override suspend fun getEnabledGlobalReplacements(): List<TextReplacement> = 
            enabledGlobalReplacements
        
        override suspend fun getEnabledReplacementsForBook(bookId: Long): List<TextReplacement> = 
            enabledBookReplacements[bookId] ?: emptyList()
        
        override suspend fun insert(replacement: TextReplacement): Long = 1L
        
        override suspend fun insertWithId(replacement: TextReplacement) {}
        
        override suspend fun update(replacement: TextReplacement) {}
        
        override suspend fun toggleEnabled(id: Long) {}
        
        override suspend fun delete(id: Long) {}
    }
    
    private class FakeReaderPreferences : ReaderPreferences {
        // Minimal implementation for testing
        override fun paragraphIndent() = throw NotImplementedError()
        override fun geminiApiKey() = throw NotImplementedError()
        override fun pollinationsApiKey() = throw NotImplementedError()
    }
    
    // Test for Issue #10: getEnabledReplacements should be suspend, not use runBlocking
    @Test
    fun `getEnabledReplacements should work in suspend context without blocking`() = runTest {
        // Arrange
        val repository = FakeTextReplacementRepository()
        repository.enabledGlobalReplacements = listOf(
            TextReplacement(
                id = 1,
                name = "Test",
                findText = "old",
                replaceText = "new",
                enabled = true
            )
        )
        val useCase = TextReplacementUseCase(FakeReaderPreferences(), repository)
        
        // Act - This should work in suspend context
        val result = useCase.applyReplacementsToText("old text", bookId = null)
        
        // Assert
        assertEquals("new text", result)
    }
    
    // Test for Issue #17: Regex detection logic should be extracted to helper
    @Test
    fun `isRegexPattern helper should detect regex metacharacters`() = runTest {
        // Arrange
        val repository = FakeTextReplacementRepository()
        val useCase = TextReplacementUseCase(FakeReaderPreferences(), repository)
        
        // Test with regex pattern
        repository.enabledGlobalReplacements = listOf(
            TextReplacement(
                id = 1,
                name = "Regex test",
                findText = "test.*pattern",
                replaceText = "replaced",
                enabled = true
            )
        )
        
        // Act
        val result = useCase.applyReplacementsToText("test some pattern here", bookId = null)
        
        // Assert
        assertTrue(result.contains("replaced"))
    }
    
    @Test
    fun `isRegexPattern helper should handle literal strings`() = runTest {
        // Arrange
        val repository = FakeTextReplacementRepository()
        repository.enabledGlobalReplacements = listOf(
            TextReplacement(
                id = 1,
                name = "Literal test",
                findText = "simple",
                replaceText = "replaced",
                enabled = true
            )
        )
        val useCase = TextReplacementUseCase(FakeReaderPreferences(), repository)
        
        // Act
        val result = useCase.applyReplacementsToText("simple text", bookId = null)
        
        // Assert
        assertEquals("replaced text", result)
    }
    
    @Test
    fun `testReplacement should use same regex detection logic`() {
        // Arrange
        val useCase = TextReplacementUseCase(FakeReaderPreferences(), null)
        
        // Act - Test with regex
        val regexResult = useCase.testReplacement(
            text = "test123pattern",
            findText = "test\\d+pattern",
            replaceText = "replaced",
            caseSensitive = false
        )
        
        // Act - Test with literal
        val literalResult = useCase.testReplacement(
            text = "simple text",
            findText = "simple",
            replaceText = "replaced",
            caseSensitive = false
        )
        
        // Assert
        assertEquals("replaced", regexResult)
        assertEquals("replaced text", literalResult)
    }
}
