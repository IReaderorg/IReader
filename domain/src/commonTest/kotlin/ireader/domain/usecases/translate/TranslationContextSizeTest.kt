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

    @Test
    fun testChunkTextsSplitsByMaxParagraphs() {
        val sixtyOneParagraphs = (1..61).map { "Paragraph $it with some text content" }
        // Even though 61 paragraphs fit under 6000 chars, maxParagraphs = 12 splits them into 6 chunks
        val chunks = TranslateEngine.chunkTexts(sixtyOneParagraphs, maxChars = 6000, maxParagraphs = 12)
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
    fun testChunkTextsByMaxCharsDefaultMaxParagraphsSplitsSixtyOneParagraphs() {
        val sixtyOneParagraphs = (1..61).map { "Paragraph $it with some text content" }
        val chunks = TranslateEngine.chunkTextsByMaxChars(sixtyOneParagraphs, maxChars = 6000)
        // Default maxParagraphs (12) splits 61 paragraphs into 6 chunks instead of bundling all 61 in 1 chunk
        assertEquals(6, chunks.size)
        assertEquals(sixtyOneParagraphs, chunks.flatten())
    }

    @Test
    fun testChunkTextsDualConstraintPrefersCharacterBudgetIfExceededFirst() {
        val p1 = "A".repeat(2000)
        val p2 = "B".repeat(2000)
        val p3 = "C".repeat(2000)
        val texts = listOf(p1, p2, p3)

        // Even though paragraph count is 3 (< 12), maxChars = 3000 forces splits because p1+p2 = 4000 > 3000
        val chunks = TranslateEngine.chunkTexts(texts, maxChars = 3000, maxParagraphs = 12)
        assertEquals(3, chunks.size)
        assertEquals(listOf(p1), chunks[0])
        assertEquals(listOf(p2), chunks[1])
        assertEquals(listOf(p3), chunks[2])
        assertEquals(texts, chunks.flatten())
    }

    @Test
    fun testParagraphChunkSizePreferences() {
        val store = MockPreferenceStore()
        val prefs = ReaderPreferences(store)

        // Default global is 0 (auto) -> effective returns default (12)
        assertEquals(0, prefs.translationParagraphChunkSize().get())
        assertEquals(12, prefs.getEffectiveParagraphChunkSize(engineId = 2L, defaultSize = 12))

        // Global set to 5 -> effective returns 5
        prefs.translationParagraphChunkSize().set(5)
        assertEquals(5, prefs.getEffectiveParagraphChunkSize(engineId = 2L, defaultSize = 12))

        // Engine-specific override set to 8 -> engine 2 returns 8, engine 3 returns global (5)
        prefs.engineParagraphChunkSize(engineId = 2L).set(8)
        assertEquals(8, prefs.getEffectiveParagraphChunkSize(engineId = 2L, defaultSize = 12))
        assertEquals(5, prefs.getEffectiveParagraphChunkSize(engineId = 3L, defaultSize = 12))
    }

    @Test
    fun testSanitizeParagraphBreakVariations() {
        // Test variations like --PARAGRAPH_BREAK__, __PARAGRAPH_BREAK__, [PARAGRAPH_BREAK], etc.
        val dirtyText1 = "This is translated text.--PARAGRAPH_BREAK__"
        val clean1 = TranslateEngine.sanitizeParagraphBreakMarkers(dirtyText1)
        assertEquals("This is translated text.", clean1)

        val dirtyText2 = "Hello world\n__PARAGRAPH_BREAK__\nMore text"
        val clean2 = TranslateEngine.sanitizeParagraphBreakMarkers(dirtyText2)
        assertTrue(!clean2.contains("PARAGRAPH_BREAK"))

        val dirtyText3 = "Prefix [PARAGRAPH_BREAK] Suffix"
        val clean3 = TranslateEngine.sanitizeParagraphBreakMarkers(dirtyText3)
        assertTrue(!clean3.contains("PARAGRAPH_BREAK"))
    }

    @Test
    fun testSplitByParagraphMarkersWithVariations() {
        val response = "First paragraph\n--PARAGRAPH_BREAK__\nSecond paragraph\n__PARAGRAPH_BREAK__\nThird paragraph"
        val split = TranslateEngine.splitByParagraphMarkers(response, expectedCount = 3)
        assertEquals(3, split.size)
        assertEquals("First paragraph", split[0])
        assertEquals("Second paragraph", split[1])
        assertEquals("Third paragraph", split[2])
    }

    @Test
    fun testSplitByParagraphMarkersNewlinePartitioningWhenMarkersMissing() {
        // When AI dropped the markers but returned text with newlines
        val response = "Paragraph 1\nParagraph 2\nParagraph 3"
        val split = TranslateEngine.splitByParagraphMarkers(response, expectedCount = 3)
        assertEquals(3, split.size)
        assertEquals("Paragraph 1", split[0])
        assertEquals("Paragraph 2", split[1])
        assertEquals("Paragraph 3", split[2])
    }

    @Test
    fun testAdjustParagraphCountExactMatch() {
        val translated = listOf("Trans 1", "Trans 2", "Trans 3")
        val original = listOf("Orig 1", "Orig 2", "Orig 3")
        val adjusted = TranslateEngine.adjustParagraphCount(translated, original)
        assertEquals(translated, adjusted)
    }

    @Test
    fun testAdjustParagraphCountSingleParagraphWithLinesPartitionsLines() {
        // AI combined everything into 1 item with newline separation
        val translated = listOf("Line 1\nLine 2\nLine 3")
        val original = listOf("Orig 1", "Orig 2", "Orig 3")
        val adjusted = TranslateEngine.adjustParagraphCount(translated, original)
        assertEquals(3, adjusted.size)
        assertEquals("Line 1", adjusted[0])
        assertEquals("Line 2", adjusted[1])
        assertEquals("Line 3", adjusted[2])
    }

    @Test
    fun testAdjustParagraphCountTooFewWithoutLinesPadsWithOriginal() {
        val translated = listOf("Trans 1")
        val original = listOf("Orig 1", "Orig 2", "Orig 3")
        val adjusted = TranslateEngine.adjustParagraphCount(translated, original)
        assertEquals(3, adjusted.size)
        assertEquals("Trans 1", adjusted[0])
        assertEquals("Orig 2", adjusted[1])
        assertEquals("Orig 3", adjusted[2])
    }

    @Test
    fun testAdjustParagraphCountTooManyTrimsExtras() {
        val translated = listOf("Trans 1", "Trans 2", "Trans 3", "Trans 4")
        val original = listOf("Orig 1", "Orig 2")
        val adjusted = TranslateEngine.adjustParagraphCount(translated, original)
        assertEquals(2, adjusted.size)
        assertEquals(listOf("Trans 1", "Trans 2"), adjusted)
    }

    @Test
    fun testSanitizeTranslatedParagraphsStripsAllMarkers() {
        val dirty = listOf(
            "Clean text 1",
            "Text with --PARAGRAPH_BREAK__ trailing",
            "__PARAGRAPH_BREAK__ at start",
            "Mixed [PARAGRAPH_BREAK] inside text"
        )
        val sanitized = TranslateEngine.sanitizeTranslatedParagraphs(dirty)
        assertTrue(sanitized.none { it.contains("PARAGRAPH_BREAK") })
    }
}


