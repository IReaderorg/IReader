package ireader.domain.usecases.translate

import ireader.domain.data.repository.TranslatedChapterRepository
import ireader.domain.models.entities.TranslatedChapter
import ireader.domain.usecases.translation.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Comprehensive tests for translation use cases
 * Tests translation storage, retrieval, and glossary application
 */
class TranslationUseCasesTest {
    
    private lateinit var saveTranslatedChapterUseCase: SaveTranslatedChapterUseCase
    private lateinit var getTranslatedChapterUseCase: GetTranslatedChapterUseCase
    private lateinit var deleteTranslatedChapterUseCase: DeleteTranslatedChapterUseCase
    private lateinit var getAllTranslationsForChapterUseCase: GetAllTranslationsForChapterUseCase
    private lateinit var applyGlossaryToTextUseCase: ApplyGlossaryToTextUseCase
    private lateinit var repository: TranslatedChapterRepository
    
    @BeforeTest
    fun setup() {
        repository = mockk()
        saveTranslatedChapterUseCase = SaveTranslatedChapterUseCase(repository)
        getTranslatedChapterUseCase = GetTranslatedChapterUseCase(repository)
        deleteTranslatedChapterUseCase = DeleteTranslatedChapterUseCase(repository)
        getAllTranslationsForChapterUseCase = GetAllTranslationsForChapterUseCase(repository)
        applyGlossaryToTextUseCase = ApplyGlossaryToTextUseCase()
    }
    
    @AfterTest
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `saveTranslatedChapter should store translation`() = runTest {
        // Given
        val chapterId = 1L
        val targetLanguage = "es"
        val translatedContent = "Contenido traducido"
        
        coEvery { repository.insertTranslation(any()) } just Runs
        
        // When
        val result = saveTranslatedChapterUseCase.execute(
            chapterId, targetLanguage, translatedContent
        )
        
        // Then
        assertTrue(result.isSuccess)
        coVerify {
            repository.insertTranslation(match { translation ->
                translation.chapterId == chapterId &&
                translation.targetLanguage == targetLanguage &&
                translation.translatedContent == translatedContent
            })
        }
    }
    
    @Test
    fun `saveTranslatedChapter should handle errors`() = runTest {
        // Given
        val chapterId = 1L
        val error = Exception("Database error")
        
        coEvery { repository.insertTranslation(any()) } throws error
        
        // When
        val result = saveTranslatedChapterUseCase.execute(
            chapterId, "es", "content"
        )
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
    
    @Test
    fun `getTranslatedChapter should retrieve translation`() = runTest {
        // Given
        val chapterId = 1L
        val targetLanguage = "es"
        val expectedTranslation = TranslatedChapter(
            id = 1L,
            chapterId = chapterId,
            targetLanguage = targetLanguage,
            translatedContent = "Contenido traducido",
            translatedAt = System.currentTimeMillis()
        )
        
        coEvery { repository.getTranslation(chapterId, targetLanguage) } returns expectedTranslation
        
        // When
        val result = getTranslatedChapterUseCase.execute(chapterId, targetLanguage)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedTranslation, result.getOrNull())
    }
    
    @Test
    fun `getTranslatedChapter should return null when translation not found`() = runTest {
        // Given
        val chapterId = 1L
        val targetLanguage = "es"
        
        coEvery { repository.getTranslation(chapterId, targetLanguage) } returns null
        
        // When
        val result = getTranslatedChapterUseCase.execute(chapterId, targetLanguage)
        
        // Then
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }
    
    @Test
    fun `deleteTranslatedChapter should remove translation`() = runTest {
        // Given
        val chapterId = 1L
        val targetLanguage = "es"
        
        coEvery { repository.deleteTranslation(chapterId, targetLanguage) } just Runs
        
        // When
        val result = deleteTranslatedChapterUseCase.execute(chapterId, targetLanguage)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { repository.deleteTranslation(chapterId, targetLanguage) }
    }
    
    @Test
    fun `getAllTranslationsForChapter should return all translations`() = runTest {
        // Given
        val chapterId = 1L
        val translations = listOf(
            TranslatedChapter(1L, chapterId, "es", "Spanish content", 0L),
            TranslatedChapter(2L, chapterId, "fr", "French content", 0L),
            TranslatedChapter(3L, chapterId, "de", "German content", 0L)
        )
        
        coEvery { repository.getAllTranslationsForChapter(chapterId) } returns translations
        
        // When
        val result = getAllTranslationsForChapterUseCase.execute(chapterId)
        
        // Then
        assertEquals(3, result.size)
        assertEquals(translations, result)
    }
    
    @Test
    fun `getAllTranslationsForChapter should return empty list when no translations exist`() = runTest {
        // Given
        val chapterId = 1L
        
        coEvery { repository.getAllTranslationsForChapter(chapterId) } returns emptyList()
        
        // When
        val result = getAllTranslationsForChapterUseCase.execute(chapterId)
        
        // Then
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun `applyGlossaryToText should replace terms`() {
        // Given
        val text = "The cultivator reached the Foundation Establishment stage."
        val glossary = mapOf(
            "cultivator" to "修真者",
            "Foundation Establishment" to "筑基期"
        )
        
        // When
        val result = applyGlossaryToTextUseCase.execute(text, glossary)
        
        // Then
        assertTrue(result.contains("修真者"))
        assertTrue(result.contains("筑基期"))
    }
    
    @Test
    fun `applyGlossaryToText should handle empty glossary`() {
        // Given
        val text = "Original text"
        val glossary = emptyMap<String, String>()
        
        // When
        val result = applyGlossaryToTextUseCase.execute(text, glossary)
        
        // Then
        assertEquals(text, result)
    }
    
    @Test
    fun `applyGlossaryToText should be case insensitive`() {
        // Given
        val text = "The CULTIVATOR and the cultivator"
        val glossary = mapOf("cultivator" to "修真者")
        
        // When
        val result = applyGlossaryToTextUseCase.execute(text, glossary)
        
        // Then
        assertTrue(result.contains("修真者"))
    }
    
    @Test
    fun `applyGlossaryToText should handle multiple occurrences`() {
        // Given
        val text = "cultivator meets cultivator, cultivator fights"
        val glossary = mapOf("cultivator" to "修真者")
        
        // When
        val result = applyGlossaryToTextUseCase.execute(text, glossary)
        
        // Then
        val count = result.split("修真者").size - 1
        assertEquals(3, count)
    }
    
    @Test
    fun `saveTranslatedChapter should update existing translation`() = runTest {
        // Given
        val chapterId = 1L
        val targetLanguage = "es"
        val newContent = "Updated content"
        
        coEvery { repository.insertTranslation(any()) } just Runs
        
        // When
        saveTranslatedChapterUseCase.execute(chapterId, targetLanguage, newContent)
        
        // Then
        coVerify {
            repository.insertTranslation(match { it.translatedContent == newContent })
        }
    }
}

/**
 * Tests for TranslationEnginesManager
 */
class TranslationEnginesManagerTest {
    
    private lateinit var translationEnginesManager: TranslationEnginesManager
    
    @BeforeTest
    fun setup() {
        translationEnginesManager = TranslationEnginesManager()
    }
    
    @Test
    fun `should register translation engine`() {
        // Given
        val engineName = "TestEngine"
        val engine = mockk<TranslateEngine>()
        
        // When
        translationEnginesManager.registerEngine(engineName, engine)
        
        // Then
        assertTrue(translationEnginesManager.hasEngine(engineName))
    }
    
    @Test
    fun `should get registered engine`() {
        // Given
        val engineName = "TestEngine"
        val engine = mockk<TranslateEngine>()
        translationEnginesManager.registerEngine(engineName, engine)
        
        // When
        val retrieved = translationEnginesManager.getEngine(engineName)
        
        // Then
        assertEquals(engine, retrieved)
    }
    
    @Test
    fun `should return null for unregistered engine`() {
        // When
        val retrieved = translationEnginesManager.getEngine("NonExistent")
        
        // Then
        assertNull(retrieved)
    }
    
    @Test
    fun `should list all registered engines`() {
        // Given
        translationEnginesManager.registerEngine("Engine1", mockk())
        translationEnginesManager.registerEngine("Engine2", mockk())
        translationEnginesManager.registerEngine("Engine3", mockk())
        
        // When
        val engines = translationEnginesManager.listEngines()
        
        // Then
        assertEquals(3, engines.size)
        assertTrue(engines.contains("Engine1"))
        assertTrue(engines.contains("Engine2"))
        assertTrue(engines.contains("Engine3"))
    }
    
    @Test
    fun `should unregister engine`() {
        // Given
        val engineName = "TestEngine"
        translationEnginesManager.registerEngine(engineName, mockk())
        
        // When
        translationEnginesManager.unregisterEngine(engineName)
        
        // Then
        assertFalse(translationEnginesManager.hasEngine(engineName))
    }
}
