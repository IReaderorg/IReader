package ireader.domain.services.tts_service.player

import kotlin.test.*

/**
 * Tests for GradioTTSPlayerState data class.
 * 
 * These tests verify the computed properties and state logic.
 */
class GradioTTSPlayerStateTest {
    
    // ==================== canPlay Tests ====================
    
    @Test
    fun `canPlay is true when has content and not at end`() {
        val state = GradioTTSPlayerState(
            hasContent = true,
            currentParagraph = 0,
            totalParagraphs = 5
        )
        
        assertTrue(state.canPlay)
    }
    
    @Test
    fun `canPlay is false when no content`() {
        val state = GradioTTSPlayerState(
            hasContent = false,
            currentParagraph = 0,
            totalParagraphs = 0
        )
        
        assertFalse(state.canPlay)
    }
    
    @Test
    fun `canPlay is false when at end`() {
        val state = GradioTTSPlayerState(
            hasContent = true,
            currentParagraph = 5,
            totalParagraphs = 5
        )
        
        assertFalse(state.canPlay)
    }
    
    // ==================== canNext Tests ====================
    
    @Test
    fun `canNext is true when not at last paragraph`() {
        val state = GradioTTSPlayerState(
            hasContent = true,
            currentParagraph = 2,
            totalParagraphs = 5
        )
        
        assertTrue(state.canNext)
    }
    
    @Test
    fun `canNext is false when at last paragraph`() {
        val state = GradioTTSPlayerState(
            hasContent = true,
            currentParagraph = 4,
            totalParagraphs = 5
        )
        
        assertFalse(state.canNext)
    }
    
    @Test
    fun `canNext is false when no content`() {
        val state = GradioTTSPlayerState(
            hasContent = false,
            currentParagraph = 0,
            totalParagraphs = 0
        )
        
        assertFalse(state.canNext)
    }
    
    // ==================== canPrevious Tests ====================
    
    @Test
    fun `canPrevious is true when not at first paragraph`() {
        val state = GradioTTSPlayerState(
            hasContent = true,
            currentParagraph = 2,
            totalParagraphs = 5
        )
        
        assertTrue(state.canPrevious)
    }
    
    @Test
    fun `canPrevious is false when at first paragraph`() {
        val state = GradioTTSPlayerState(
            hasContent = true,
            currentParagraph = 0,
            totalParagraphs = 5
        )
        
        assertFalse(state.canPrevious)
    }
    
    @Test
    fun `canPrevious is false when no content`() {
        val state = GradioTTSPlayerState(
            hasContent = false,
            currentParagraph = 0,
            totalParagraphs = 0
        )
        
        assertFalse(state.canPrevious)
    }
    
    // ==================== isActive Tests ====================
    
    @Test
    fun `isActive is true when playing`() {
        val state = GradioTTSPlayerState(
            isPlaying = true,
            isLoading = false
        )
        
        assertTrue(state.isActive)
    }
    
    @Test
    fun `isActive is true when loading`() {
        val state = GradioTTSPlayerState(
            isPlaying = false,
            isLoading = true
        )
        
        assertTrue(state.isActive)
    }
    
    @Test
    fun `isActive is true when both playing and loading`() {
        val state = GradioTTSPlayerState(
            isPlaying = true,
            isLoading = true
        )
        
        assertTrue(state.isActive)
    }
    
    @Test
    fun `isActive is false when neither playing nor loading`() {
        val state = GradioTTSPlayerState(
            isPlaying = false,
            isLoading = false
        )
        
        assertFalse(state.isActive)
    }
    
    // ==================== cacheProgress Tests ====================
    
    @Test
    fun `cacheProgress is 0 when no paragraphs`() {
        val state = GradioTTSPlayerState(
            totalParagraphs = 0,
            cachedParagraphs = emptySet()
        )
        
        assertEquals(0f, state.cacheProgress)
    }
    
    @Test
    fun `cacheProgress is 0 when no cached paragraphs`() {
        val state = GradioTTSPlayerState(
            totalParagraphs = 10,
            cachedParagraphs = emptySet()
        )
        
        assertEquals(0f, state.cacheProgress)
    }
    
    @Test
    fun `cacheProgress is correct percentage`() {
        val state = GradioTTSPlayerState(
            totalParagraphs = 10,
            cachedParagraphs = setOf(0, 1, 2, 3, 4)
        )
        
        assertEquals(0.5f, state.cacheProgress)
    }
    
    @Test
    fun `cacheProgress is 1 when all cached`() {
        val state = GradioTTSPlayerState(
            totalParagraphs = 5,
            cachedParagraphs = setOf(0, 1, 2, 3, 4)
        )
        
        assertEquals(1f, state.cacheProgress)
    }
    
    // ==================== Default Values Tests ====================
    
    @Test
    fun `default state has correct values`() {
        val state = GradioTTSPlayerState()
        
        assertFalse(state.isPlaying)
        assertFalse(state.isPaused)
        assertFalse(state.isLoading)
        assertEquals(0, state.currentParagraph)
        assertEquals(0, state.totalParagraphs)
        assertTrue(state.cachedParagraphs.isEmpty())
        assertTrue(state.loadingParagraphs.isEmpty())
        assertEquals(1.0f, state.speed)
        assertEquals(1.0f, state.pitch)
        assertNull(state.error)
        assertFalse(state.hasContent)
        assertEquals(0f, state.paragraphProgress)
        assertEquals("", state.engineName)
    }
    
    // ==================== Copy Tests ====================
    
    @Test
    fun `copy preserves unchanged values`() {
        val original = GradioTTSPlayerState(
            isPlaying = true,
            currentParagraph = 5,
            totalParagraphs = 10,
            speed = 1.5f
        )
        
        val copied = original.copy(isPaused = true)
        
        assertTrue(copied.isPlaying)
        assertTrue(copied.isPaused)
        assertEquals(5, copied.currentParagraph)
        assertEquals(10, copied.totalParagraphs)
        assertEquals(1.5f, copied.speed)
    }
}
