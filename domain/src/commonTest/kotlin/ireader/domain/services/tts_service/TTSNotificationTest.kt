package ireader.domain.services.tts_service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for TTS Notification functionality
 * 
 * These tests verify:
 * - Notification data structure
 * - Notification state updates
 * - Notification callbacks
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TTSNotificationTest {

    /**
     * Test: Notification data is created correctly
     */
    @Test
    fun `notification data is created correctly`() = runTest {
        val data = TTSNotificationData(
            title = "Chapter 1",
            subtitle = "Test Book",
            coverUrl = "https://example.com/cover.jpg",
            isPlaying = true,
            isLoading = false,
            currentParagraph = 5,
            totalParagraphs = 20,
            bookId = 1L,
            chapterId = 10L,
            sourceId = 100L
        )
        
        assertEquals("Chapter 1", data.title)
        assertEquals("Test Book", data.subtitle)
        assertEquals("https://example.com/cover.jpg", data.coverUrl)
        assertTrue(data.isPlaying)
        assertFalse(data.isLoading)
        assertEquals(5, data.currentParagraph)
        assertEquals(20, data.totalParagraphs)
        assertEquals(1L, data.bookId)
        assertEquals(10L, data.chapterId)
        assertEquals(100L, data.sourceId)
    }

    /**
     * Test: Mock notification tracks show calls
     */
    @Test
    fun `mock notification tracks show calls`() = runTest {
        val notification = MockTTSNotification()
        
        assertFalse(notification.isShowing)
        
        notification.show(createTestNotificationData())
        
        assertTrue(notification.isShowing)
        assertEquals(1, notification.showCallCount)
    }

    /**
     * Test: Mock notification tracks hide calls
     */
    @Test
    fun `mock notification tracks hide calls`() = runTest {
        val notification = MockTTSNotification()
        
        notification.show(createTestNotificationData())
        assertTrue(notification.isShowing)
        
        notification.hide()
        
        assertFalse(notification.isShowing)
        assertEquals(1, notification.hideCallCount)
    }

    /**
     * Test: Notification playback state updates
     */
    @Test
    fun `notification playback state updates`() = runTest {
        val notification = MockTTSNotification()
        
        notification.show(createTestNotificationData(isPlaying = false))
        assertFalse(notification.lastData?.isPlaying ?: true)
        
        notification.updatePlaybackState(true)
        assertTrue(notification.lastPlaybackState)
    }

    /**
     * Test: Notification progress updates
     */
    @Test
    fun `notification progress updates`() = runTest {
        val notification = MockTTSNotification()
        
        notification.show(createTestNotificationData(currentParagraph = 0, totalParagraphs = 10))
        
        notification.updateProgress(5, 10)
        
        assertEquals(5, notification.lastProgressCurrent)
        assertEquals(10, notification.lastProgressTotal)
    }

    /**
     * Test: Notification callback is invoked
     */
    @Test
    fun `notification callback is invoked`() = runTest {
        var playCalled = false
        var pauseCalled = false
        var nextCalled = false
        var previousCalled = false
        
        val callback = object : TTSNotificationCallback {
            override fun onPlay() { playCalled = true }
            override fun onPause() { pauseCalled = true }
            override fun onNext() { nextCalled = true }
            override fun onPrevious() { previousCalled = true }
            override fun onNextParagraph() {}
            override fun onPreviousParagraph() {}
            override fun onClose() {}
            override fun onNotificationClick() {}
        }
        
        callback.onPlay()
        assertTrue(playCalled)
        
        callback.onPause()
        assertTrue(pauseCalled)
        
        callback.onNext()
        assertTrue(nextCalled)
        
        callback.onPrevious()
        assertTrue(previousCalled)
    }

    /**
     * Test: Notification handles null cover URL
     */
    @Test
    fun `notification handles null cover URL`() = runTest {
        val data = TTSNotificationData(
            title = "Chapter 1",
            subtitle = "Test Book",
            coverUrl = null,
            isPlaying = true,
            isLoading = false,
            currentParagraph = 0,
            totalParagraphs = 10,
            bookId = 1L,
            chapterId = 1L,
            sourceId = 1L
        )
        
        val notification = MockTTSNotification()
        notification.show(data)
        
        assertTrue(notification.isShowing)
        assertEquals(null, notification.lastData?.coverUrl)
    }

    /**
     * Test: Notification handles loading state
     */
    @Test
    fun `notification handles loading state`() = runTest {
        val notification = MockTTSNotification()
        
        notification.show(createTestNotificationData(isLoading = true, isPlaying = false))
        
        assertTrue(notification.lastData?.isLoading ?: false)
        assertFalse(notification.lastData?.isPlaying ?: true)
    }

    /**
     * Test: Multiple show calls update data
     */
    @Test
    fun `multiple show calls update data`() = runTest {
        val notification = MockTTSNotification()
        
        notification.show(createTestNotificationData(title = "Chapter 1"))
        assertEquals("Chapter 1", notification.lastData?.title)
        
        notification.show(createTestNotificationData(title = "Chapter 2"))
        assertEquals("Chapter 2", notification.lastData?.title)
        assertEquals(2, notification.showCallCount)
    }

    // Helper function to create test notification data
    private fun createTestNotificationData(
        title: String = "Test Chapter",
        subtitle: String = "Test Book",
        coverUrl: String? = "https://example.com/cover.jpg",
        isPlaying: Boolean = false,
        isLoading: Boolean = false,
        currentParagraph: Int = 0,
        totalParagraphs: Int = 10
    ) = TTSNotificationData(
        title = title,
        subtitle = subtitle,
        coverUrl = coverUrl,
        isPlaying = isPlaying,
        isLoading = isLoading,
        currentParagraph = currentParagraph,
        totalParagraphs = totalParagraphs,
        bookId = 1L,
        chapterId = 1L,
        sourceId = 1L
    )
}

/**
 * Mock TTS Notification for testing
 */
class MockTTSNotification : TTSNotification {
    var isShowing = false
        private set
    var showCallCount = 0
        private set
    var hideCallCount = 0
        private set
    var lastData: TTSNotificationData? = null
        private set
    var lastPlaybackState = false
        private set
    var lastProgressCurrent = 0
        private set
    var lastProgressTotal = 0
        private set
    
    override fun show(data: TTSNotificationData) {
        isShowing = true
        showCallCount++
        lastData = data
    }
    
    override fun hide() {
        isShowing = false
        hideCallCount++
    }
    
    override fun updatePlaybackState(isPlaying: Boolean) {
        lastPlaybackState = isPlaying
    }
    
    override fun updateProgress(current: Int, total: Int) {
        lastProgressCurrent = current
        lastProgressTotal = total
    }
}
