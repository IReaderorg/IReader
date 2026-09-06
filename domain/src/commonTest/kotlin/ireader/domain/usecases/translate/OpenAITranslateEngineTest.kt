package ireader.domain.usecases.translate

import ireader.core.http.HttpClients
import ireader.domain.data.engines.TranslateEngine
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.i18n.UiText
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OpenAITranslateEngineTest {

    @Test
    fun testOpenAIEngineProperties() {
        val store = MockPreferenceStore()
        val prefs = ReaderPreferences(store)
        val httpClients = HttpClients(store)
        val engine = OpenAITranslateEngine(httpClients, prefs)

        assertEquals(2L, engine.id)
        assertEquals("OpenAI (GPT)", engine.engineName)
        assertTrue(engine.supportsAI)
        assertTrue(engine.supportsContextAwareTranslation)
        assertEquals(3500, engine.defaultMaxCharsPerRequest)
        assertEquals(12, engine.maxParagraphsPerRequest)
    }

    @Test
    fun testOpenAIChunksSixtyOneParagraphsIntoSmallChunks() {
        val store = MockPreferenceStore()
        val prefs = ReaderPreferences(store)
        val httpClients = HttpClients(store)
        val engine = OpenAITranslateEngine(httpClients, prefs)

        val sixtyOneParagraphs = (1..61).map { "Paragraph $it: A novel sentence with some text." }
        val chunks = TranslateEngine.chunkTexts(sixtyOneParagraphs, engine.maxCharsPerRequest, engine.maxParagraphsPerRequest)

        // 61 paragraphs with max 12 per chunk -> 6 chunks (5 of 12 + 1 of 1)
        assertEquals(6, chunks.size)
        assertEquals(12, chunks[0].size)
        assertEquals(12, chunks[1].size)
        assertEquals(12, chunks[2].size)
        assertEquals(12, chunks[3].size)
        assertEquals(12, chunks[4].size)
        assertEquals(1, chunks[5].size)
        assertEquals(sixtyOneParagraphs, chunks.flatten())
    }

    @Test
    fun testOpenAIEmptyTextsReturnsError() = runTest {
        val store = MockPreferenceStore()
        val prefs = ReaderPreferences(store)
        val httpClients = HttpClients(store)
        val engine = OpenAITranslateEngine(httpClients, prefs)

        var errorReceived: UiText? = null
        engine.translate(
            texts = emptyList(),
            source = "en",
            target = "es",
            onSuccess = {},
            onError = { errorReceived = it }
        )

        assertNotNull(errorReceived)
    }
}
