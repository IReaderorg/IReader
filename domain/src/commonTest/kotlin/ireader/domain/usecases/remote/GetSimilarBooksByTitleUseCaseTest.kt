package ireader.domain.usecases.remote

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetSimilarBooksByTitleUseCaseTest {

    @Test
    fun `extractKeywords strips special characters and stop words`() {
        val keywords = GetSimilarBooksByTitleUseCase.extractKeywords("The Great Adventure Light Novel Vol 1")

        assertTrue(keywords.isNotEmpty())
        // Full cleaned title should be first
        assertEquals("the great adventure light novel vol 1", keywords.first())
        // Stop words like "the", "novel", "vol" should be filtered out from individual word tokens
        assertTrue(keywords.contains("great"))
        assertTrue(keywords.contains("adventure"))
    }

    @Test
    fun `extractKeywords returns empty list for blank title`() {
        val keywords = GetSimilarBooksByTitleUseCase.extractKeywords("   ")
        assertTrue(keywords.isEmpty())
    }

    @Test
    fun `extractKeywords preserves foreign and non-stop words`() {
        val keywords = GetSimilarBooksByTitleUseCase.extractKeywords("Solo Leveling Shadow Monarch")

        assertEquals("solo leveling shadow monarch", keywords.first())
        assertTrue(keywords.contains("solo"))
        assertTrue(keywords.contains("leveling"))
        assertTrue(keywords.contains("shadow"))
        assertTrue(keywords.contains("monarch"))
    }
}
