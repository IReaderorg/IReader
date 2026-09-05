package ireader.domain.usecases.translate

import ireader.domain.data.engines.TranslateEngine
import ireader.domain.preferences.prefs.ReaderPreferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TranslationContextSizeTest {

    @Test
    fun testDefaultGlobalContextSizeIsZero() {
        val store = MockPreferenceStore()
        val prefs = ReaderPreferences(store)

        assertEquals(0, prefs.translationContextSize().get())
    }

    @Test
    fun testGlobalContextSizePersistence() {
        val store = MockPreferenceStore()
        val prefs = ReaderPreferences(store)

        prefs.translationContextSize().set(16000)
        assertEquals(16000, prefs.translationContextSize().get())
    }

    @Test
    fun testGetEffectiveContextSizeFallbackToDefault() {
        val store = MockPreferenceStore()
        val prefs = ReaderPreferences(store)

        // Neither engine nor global set -> fallback to default
        val effective = prefs.getEffectiveContextSize(engineId = 2L, defaultSize = 4000)
        assertEquals(4000, effective)
    }

    @Test
    fun testGetEffectiveContextSizeUsesGlobalWhenEngineNotSet() {
        val store = MockPreferenceStore()
        val prefs = ReaderPreferences(store)

        prefs.translationContextSize().set(16000)
        val effective = prefs.getEffectiveContextSize(engineId = 2L, defaultSize = 4000)
        assertEquals(16000, effective)
    }

    @Test
    fun testGetEffectiveContextSizePerEngineOverridesGlobal() {
        val store = MockPreferenceStore()
        val prefs = ReaderPreferences(store)

        prefs.translationContextSize().set(16000)
        prefs.engineContextSize(engineId = 2L).set(32000)

        // Engine 2 has its own override
        val effectiveEngine2 = prefs.getEffectiveContextSize(engineId = 2L, defaultSize = 4000)
        assertEquals(32000, effectiveEngine2)

        // Engine 5 has no override -> uses global
        val effectiveEngine5 = prefs.getEffectiveContextSize(engineId = 5L, defaultSize = 4000)
        assertEquals(16000, effectiveEngine5)
    }

    @Test
    fun testChunkTextsByMaxCharsEmptyList() {
        val chunks = TranslateEngine.chunkTextsByMaxChars(emptyList(), 4000)
        assertTrue(chunks.isEmpty())
    }

    @Test
    fun testChunkTextsByMaxCharsAllFitInSingleChunk() {
        val texts = listOf("Paragraph 1", "Paragraph 2", "Paragraph 3")
        val chunks = TranslateEngine.chunkTextsByMaxChars(texts, 4000)

        assertEquals(1, chunks.size)
        assertEquals(texts, chunks[0])
    }

    @Test
    fun testChunkTextsByMaxCharsSplitsCorrectly() {
        val p1 = "A".repeat(100)
        val p2 = "B".repeat(100)
        val p3 = "C".repeat(100)
        val texts = listOf(p1, p2, p3)

        // Max 150 chars per chunk -> p1 fits, p2 exceeds (100+100 > 150) so new chunk, p3 exceeds -> 3 chunks
        val chunks = TranslateEngine.chunkTextsByMaxChars(texts, 150)
        assertEquals(3, chunks.size)
        assertEquals(listOf(p1), chunks[0])
        assertEquals(listOf(p2), chunks[1])
        assertEquals(listOf(p3), chunks[2])

        // Flattening produces identical original sequence
        assertEquals(texts, chunks.flatten())
    }

    @Test
    fun testChunkTextsByMaxCharsGroupsMultipleItemsUnderBudget() {
        val p1 = "A".repeat(100)
        val p2 = "B".repeat(100)
        val p3 = "C".repeat(100)
        val p4 = "D".repeat(100)
        val texts = listOf(p1, p2, p3, p4)

        // Max 250 chars per chunk -> [p1, p2] (200), [p3, p4] (200) -> 2 chunks
        val chunks = TranslateEngine.chunkTextsByMaxChars(texts, 250)
        assertEquals(2, chunks.size)
        assertEquals(listOf(p1, p2), chunks[0])
        assertEquals(listOf(p3, p4), chunks[1])
        assertEquals(texts, chunks.flatten())
    }

    @Test
    fun testChunkTextsByMaxCharsHandlesItemLargerThanBudget() {
        val oversized = "X".repeat(500)
        val normal = "Y".repeat(50)
        val texts = listOf(normal, oversized, normal)

        val chunks = TranslateEngine.chunkTextsByMaxChars(texts, 100)
        // normal fits (50), oversized exceeds (50+500 > 100) -> starts new chunk with oversized (500), normal starts new chunk (50)
        assertEquals(3, chunks.size)
        assertEquals(listOf(normal), chunks[0])
        assertEquals(listOf(oversized), chunks[1])
        assertEquals(listOf(normal), chunks[2])
        assertEquals(texts, chunks.flatten())
    }
}
