//package ireader.domain.services.tts_service
//
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.test.runTest
//import kotlin.test.Test
//import kotlin.test.assertEquals
//import kotlin.test.assertFalse
//import kotlin.test.assertTrue
//
///**
// * Tests for TTS Translated Content Feature
// *
// * These tests verify:
// * - Switching between original and translated content
// * - Paragraph position preservation during content switch
// * - Content restoration when disabling translation
// */
//@OptIn(ExperimentalCoroutinesApi::class)
//class TTSTranslatedContentTest {
//
//    /**
//     * Test: TTS can switch to translated content
//     */
//    @Test
//    fun `TTS switches to translated content when enabled`() = runTest {
//        val service = MockTTSService()
//        val originalContent = listOf("Hello", "World", "Test")
//        val translatedContent = listOf("مرحبا", "عالم", "اختبار")
//
//        service.setContent(originalContent)
//        assertEquals(originalContent, service.state.currentContent.value)
//
//        service.setCustomContent(translatedContent)
//        assertEquals(translatedContent, service.state.currentContent.value)
//    }
//
//    /**
//     * Test: TTS restores original content when translation disabled
//     */
//    @Test
//    fun `TTS restores original content when translation disabled`() = runTest {
//        val service = MockTTSService()
//        val originalContent = listOf("Hello", "World")
//        val translatedContent = listOf("مرحبا", "عالم")
//
//        service.setContent(originalContent)
//        service.setCustomContent(translatedContent)
//        service.setCustomContent(null) // Disable translation
//
//        assertEquals(originalContent, service.state.currentContent.value)
//    }
//
//    /**
//     * Test: Paragraph position is preserved when switching content
//     */
//    @Test
//    fun `paragraph position preserved when content has same length`() = runTest {
//        val service = MockTTSService()
//        val originalContent = listOf("P1", "P2", "P3", "P4", "P5")
//        val translatedContent = listOf("T1", "T2", "T3", "T4", "T5")
//
//        service.setContent(originalContent)
//        service.jumpToParagraph(3)
//        assertEquals(3, service.state.currentParagraph.value)
//
//        service.setCustomContent(translatedContent)
//        assertEquals(3, service.state.currentParagraph.value, "Position should be preserved")
//    }
//
//    /**
//     * Test: Paragraph position resets when translated content is shorter
//     */
//    @Test
//    fun `paragraph position resets when translated content is shorter`() = runTest {
//        val service = MockTTSService()
//        val originalContent = listOf("P1", "P2", "P3", "P4", "P5")
//        val translatedContent = listOf("T1", "T2") // Shorter
//
//        service.setContent(originalContent)
//        service.jumpToParagraph(4) // At position 4
//
//        service.setCustomContent(translatedContent)
//        assertTrue(service.state.currentParagraph.value < 2, "Position should be within bounds")
//    }
//
//    /**
//     * Test: Total paragraphs updates when switching content
//     */
//    @Test
//    fun `total paragraphs updates when switching content`() = runTest {
//        val service = MockTTSService()
//        val originalContent = listOf("P1", "P2", "P3")
//        val translatedContent = listOf("T1", "T2", "T3", "T4", "T5")
//
//        service.setContent(originalContent)
//        assertEquals(3, service.state.totalParagraphs.value)
//
//        service.setCustomContent(translatedContent)
//        assertEquals(5, service.state.totalParagraphs.value)
//    }
//
//    /**
//     * Test: Empty translated content is handled gracefully
//     */
//    @Test
//    fun `empty translated content does not crash`() = runTest {
//        val service = MockTTSService()
//        val originalContent = listOf("P1", "P2", "P3")
//
//        service.setContent(originalContent)
//
//        // Empty list should not change content
//        service.setCustomContent(emptyList())
//        assertEquals(originalContent, service.state.currentContent.value)
//    }
//
//    /**
//     * Test: Multiple content switches work correctly
//     */
//    @Test
//    fun `multiple content switches work correctly`() = runTest {
//        val service = MockTTSService()
//        val original = listOf("Original 1", "Original 2")
//        val arabic = listOf("عربي 1", "عربي 2")
//        val french = listOf("Français 1", "Français 2")
//
//        service.setContent(original)
//
//        // Switch to Arabic
//        service.setCustomContent(arabic)
//        assertEquals(arabic, service.state.currentContent.value)
//
//        // Switch to French (should still restore to original when null)
//        service.setCustomContent(french)
//        assertEquals(french, service.state.currentContent.value)
//
//        // Restore original
//        service.setCustomContent(null)
//        assertEquals(original, service.state.currentContent.value)
//    }
//
//    /**
//     * Test: Playing state is maintained during content switch
//     */
//    @Test
//    fun `playing state maintained during content switch`() = runTest {
//        val service = MockTTSService()
//        val original = listOf("P1", "P2")
//        val translated = listOf("T1", "T2")
//
//        service.setContent(original)
//        service.play()
//        assertTrue(service.state.isPlaying.value)
//
//        service.setCustomContent(translated)
//        assertTrue(service.state.isPlaying.value, "Should still be playing after content switch")
//    }
//}
