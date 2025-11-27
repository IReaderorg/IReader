package ireader.domain.usecases.translate

import io.mockk.*
import ireader.core.source.model.Text
import ireader.domain.data.engines.ContentType
import ireader.domain.data.engines.ToneType
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.TranslatedChapter
import ireader.domain.usecases.glossary.GetGlossaryAsMapUseCase
import ireader.domain.usecases.translation.ApplyGlossaryToTextUseCase
import ireader.domain.usecases.translation.GetTranslatedChapterUseCase
import ireader.domain.usecases.translation.SaveTranslatedChapterUseCase
import ireader.i18n.UiText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TranslateChapterWithStorageUseCaseTest {

    private lateinit var useCase: TranslateChapterWithStorageUseCase
    private lateinit var translationEnginesManager: TranslationEnginesManager
    private lateinit var saveTranslatedChapterUseCase: SaveTranslatedChapterUseCase
    private lateinit var getTranslatedChapterUseCase: GetTranslatedChapterUseCase
    private lateinit var getGlossaryAsMapUseCase: GetGlossaryAsMapUseCase
    private lateinit var applyGlossaryToTextUseCase: ApplyGlossaryToTextUseCase

    @BeforeTest
    fun setup() {
        translationEnginesManager = mockk(relaxed = true)
        saveTranslatedChapterUseCase = mockk(relaxed = true)
        getTranslatedChapterUseCase = mockk(relaxed = true)
        getGlossaryAsMapUseCase = mockk(relaxed = true)
        applyGlossaryToTextUseCase = mockk(relaxed = true)

        useCase = TranslateChapterWithStorageUseCase(
            translationEnginesManager,
            saveTranslatedChapterUseCase,
            getTranslatedChapterUseCase,
            getGlossaryAsMapUseCase,
            applyGlossaryToTextUseCase
        )
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `execute should return existing translation when available`() = runTest {
        // Given
        val chapter = createTestChapter()
        val translatedChapter = createTestTranslatedChapter(chapter.id)
        val engineId = 1L
        
        every { translationEnginesManager.get().id } returns engineId
        coEvery { getTranslatedChapterUseCase.execute(chapter.id, "es", engineId) } returns translatedChapter

        // When
        var result: TranslatedChapter? = null
        useCase.execute(
            chapter = chapter,
            sourceLanguage = "en",
            targetLanguage = "es",
            scope = CoroutineScope(Dispatchers.Unconfined),
            onSuccess = { result = it },
            onError = { }
        )

        // Then
        assertEquals(translatedChapter, result)
        coVerify(exactly = 0) { translationEnginesManager.translateWithContext(any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `execute should translate and save when no existing translation`() = runTest {
        // Given
        val chapter = createTestChapter()
        val engineId = 1L
        val translatedText = "Hola Mundo"
        val translatedChapter = createTestTranslatedChapter(chapter.id, content = listOf(Text(translatedText)))

        every { translationEnginesManager.get().id } returns engineId
        coEvery { getTranslatedChapterUseCase.execute(chapter.id, "es", engineId) } returns null andThen translatedChapter
        
        // Mock translation success
        coEvery { 
            translationEnginesManager.translateWithContext(any(), any(), any(), any(), any(), any(), any(), any(), any()) 
        } answers {
            val onSuccess = arg<(List<String>) -> Unit>(7)
            onSuccess(listOf(translatedText))
        }

        // When
        var result: TranslatedChapter? = null
        useCase.execute(
            chapter = chapter,
            sourceLanguage = "en",
            targetLanguage = "es",
            scope = CoroutineScope(Dispatchers.Unconfined),
            onSuccess = { result = it },
            onError = { }
        )

        // Then
        assertEquals(translatedChapter, result)
        coVerify { saveTranslatedChapterUseCase.execute(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `execute should apply glossary when enabled`() = runTest {
        // Given
        val chapter = createTestChapter()
        val engineId = 1L
        val glossaryMap = mapOf("Hello" to "Hola")
        
        every { translationEnginesManager.get().id } returns engineId
        coEvery { getTranslatedChapterUseCase.execute(chapter.id, "es", engineId) } returns null
        coEvery { getGlossaryAsMapUseCase.execute(chapter.bookId) } returns glossaryMap
        every { applyGlossaryToTextUseCase.execute(any(), glossaryMap) } returns "Hola World"

        // Mock translation
        coEvery { 
            translationEnginesManager.translateWithContext(any(), any(), any(), any(), any(), any(), any(), any(), any()) 
        } answers {
            val texts = arg<List<String>>(0)
            assertEquals("Hola World", texts[0]) // Verify glossary was applied
            val onSuccess = arg<(List<String>) -> Unit>(7)
            onSuccess(listOf("Hola Mundo"))
        }
        
        // Mock getting the saved translation
        coEvery { getTranslatedChapterUseCase.execute(chapter.id, "es", engineId) } returns createTestTranslatedChapter(chapter.id)

        // When
        useCase.execute(
            chapter = chapter,
            sourceLanguage = "en",
            targetLanguage = "es",
            applyGlossary = true,
            scope = CoroutineScope(Dispatchers.Unconfined),
            onSuccess = { },
            onError = { }
        )

        // Then
        coVerify { getGlossaryAsMapUseCase.execute(chapter.bookId) }
        verify { applyGlossaryToTextUseCase.execute(any(), glossaryMap) }
    }

    @Test
    fun `execute should handle errors gracefully`() = runTest {
        // Given
        val chapter = createTestChapter()
        val engineId = 1L
        val errorMessage = "Network error"
        
        every { translationEnginesManager.get().id } returns engineId
        coEvery { getTranslatedChapterUseCase.execute(chapter.id, "es", engineId) } returns null
        
        // Mock translation error
        coEvery { 
            translationEnginesManager.translateWithContext(any(), any(), any(), any(), any(), any(), any(), any(), any()) 
        } answers {
            val onError = arg<(UiText) -> Unit>(8)
            onError(UiText.DynamicString(errorMessage))
        }

        // When
        var errorResult: UiText? = null
        useCase.execute(
            chapter = chapter,
            sourceLanguage = "en",
            targetLanguage = "es",
            scope = CoroutineScope(Dispatchers.Unconfined),
            onSuccess = { },
            onError = { errorResult = it }
        )

        // Then
        assertTrue(errorResult is UiText.DynamicString)
        assertEquals(errorMessage, (errorResult as UiText.DynamicString).text)
    }

    private fun createTestChapter(id: Long = 1L): Chapter {
        return Chapter(
            id = id,
            bookId = 100L,
            key = "http://example.com",
            name = "Chapter 1",
            content = listOf(Text("Hello World")),
            dateUpload = 0L,
            number = 1f,
            translator = "Translator"
        )
    }

    private fun createTestTranslatedChapter(id: Long, content: List<ireader.core.source.model.Page> = emptyList()): TranslatedChapter {
        return TranslatedChapter(
            id = id,
            chapterId = id,
            bookId = 100L,
            translatedContent = content,
            sourceLanguage = "en",
            targetLanguage = "es",
            translatorEngineId = 1L,
            createdAt = 0L,
            updatedAt = 0L
        )
    }
}
