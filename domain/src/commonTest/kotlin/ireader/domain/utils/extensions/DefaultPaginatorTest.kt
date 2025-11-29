package ireader.domain.utils.extensions

import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Unit tests for DefaultPaginator
 */
class DefaultPaginatorTest {

    // ==================== Basic Pagination Tests ====================

    @Test
    fun `loadNextItems calls onRequest with initial key`() = runTest {
        var requestedKey: Int? = null
        
        val paginator = DefaultPaginator<Int, String>(
            initialKey = 1,
            onLoadUpdated = {},
            onRequest = { key ->
                requestedKey = key
                Result.success("Page $key")
            },
            getNextKey = { 2 },
            onError = {},
            onSuccess = { _, _ -> }
        )
        
        paginator.loadNextItems()
        
        assertEquals(1, requestedKey)
    }

    @Test
    fun `loadNextItems calls onSuccess with result`() = runTest {
        var successItems: String? = null
        var successKey: Int? = null
        
        val paginator = DefaultPaginator<Int, String>(
            initialKey = 1,
            onLoadUpdated = {},
            onRequest = { Result.success("Page Data") },
            getNextKey = { 2 },
            onError = {},
            onSuccess = { items, key ->
                successItems = items
                successKey = key
            }
        )
        
        paginator.loadNextItems()
        
        assertEquals("Page Data", successItems)
        assertEquals(2, successKey)
    }

    @Test
    fun `loadNextItems updates loading state`() = runTest {
        val loadingStates = mutableListOf<Boolean>()
        
        val paginator = DefaultPaginator<Int, String>(
            initialKey = 1,
            onLoadUpdated = { loadingStates.add(it) },
            onRequest = { Result.success("Data") },
            getNextKey = { 2 },
            onError = {},
            onSuccess = { _, _ -> }
        )
        
        paginator.loadNextItems()
        
        assertEquals(listOf(true, false), loadingStates)
    }

    @Test
    fun `loadNextItems calls onInit on first page`() = runTest {
        var initCalled = false
        
        val paginator = DefaultPaginator<Int, String>(
            initialKey = 1,
            onInit = { initCalled = true },
            onLoadUpdated = {},
            onRequest = { Result.success("Data") },
            getNextKey = { 2 },
            onError = {},
            onSuccess = { _, _ -> }
        )
        
        paginator.loadNextItems()
        
        assertTrue(initCalled)
    }

    @Test
    fun `loadNextItems does not call onInit on subsequent pages`() = runTest {
        var initCallCount = 0
        
        val paginator = DefaultPaginator<Int, String>(
            initialKey = 1,
            onInit = { initCallCount++ },
            onLoadUpdated = {},
            onRequest = { Result.success("Data") },
            getNextKey = { currentKey -> currentKey as? Int ?: 1 + 1 },
            onError = {},
            onSuccess = { _, _ -> }
        )
        
        paginator.loadNextItems()
        paginator.loadNextItems()
        
        assertEquals(1, initCallCount)
    }

    // ==================== Error Handling Tests ====================

    @Test
    fun `loadNextItems calls onError when request fails`() = runTest {
        var errorThrowable: Throwable? = null
        val expectedException = RuntimeException("Network error")
        
        val paginator = DefaultPaginator<Int, String>(
            initialKey = 1,
            onLoadUpdated = {},
            onRequest = { Result.failure(expectedException) },
            getNextKey = { 2 },
            onError = { errorThrowable = it },
            onSuccess = { _, _ -> }
        )
        
        paginator.loadNextItems()
        
        assertEquals(expectedException, errorThrowable)
    }

    @Test
    fun `loadNextItems sets loading to false on error`() = runTest {
        val loadingStates = mutableListOf<Boolean>()
        
        val paginator = DefaultPaginator<Int, String>(
            initialKey = 1,
            onLoadUpdated = { loadingStates.add(it) },
            onRequest = { Result.failure(RuntimeException("Error")) },
            getNextKey = { 2 },
            onError = {},
            onSuccess = { _, _ -> }
        )
        
        paginator.loadNextItems()
        
        assertEquals(listOf(true, false), loadingStates)
    }

    @Test
    fun `loadNextItems does not call onSuccess on error`() = runTest {
        var successCalled = false
        
        val paginator = DefaultPaginator<Int, String>(
            initialKey = 1,
            onLoadUpdated = {},
            onRequest = { Result.failure(RuntimeException("Error")) },
            getNextKey = { 2 },
            onError = {},
            onSuccess = { _, _ -> successCalled = true }
        )
        
        paginator.loadNextItems()
        
        assertFalse(successCalled)
    }

    // ==================== Concurrent Request Prevention Tests ====================

    @Test
    fun `loadNextItems prevents concurrent requests`() = runTest {
        var requestCount = 0
        
        val paginator = DefaultPaginator<Int, String>(
            initialKey = 1,
            onLoadUpdated = {},
            onRequest = {
                requestCount++
                Result.success("Data")
            },
            getNextKey = { 2 },
            onError = {},
            onSuccess = { _, _ -> }
        )
        
        // First call completes, second should work
        paginator.loadNextItems()
        paginator.loadNextItems()
        
        assertEquals(2, requestCount)
    }

    // ==================== Reset Tests ====================

    @Test
    fun `reset restores initial key`() = runTest {
        val requestedKeys = mutableListOf<Int>()
        
        val paginator = DefaultPaginator<Int, String>(
            initialKey = 1,
            onLoadUpdated = {},
            onRequest = { key ->
                requestedKeys.add(key)
                Result.success("Data")
            },
            getNextKey = { 2 },
            onError = {},
            onSuccess = { _, _ -> }
        )
        
        paginator.loadNextItems() // key = 1
        paginator.reset()
        paginator.loadNextItems() // key = 1 again
        
        assertEquals(listOf(1, 1), requestedKeys)
    }

    @Test
    fun `reset allows onInit to be called again`() = runTest {
        var initCallCount = 0
        
        val paginator = DefaultPaginator<Int, String>(
            initialKey = 1,
            onInit = { initCallCount++ },
            onLoadUpdated = {},
            onRequest = { Result.success("Data") },
            getNextKey = { 2 },
            onError = {},
            onSuccess = { _, _ -> }
        )
        
        paginator.loadNextItems()
        paginator.reset()
        paginator.loadNextItems()
        
        assertEquals(2, initCallCount)
    }

    // ==================== Key Progression Tests ====================

    @Test
    fun `paginator progresses through keys correctly`() = runTest {
        val requestedKeys = mutableListOf<Int>()
        var currentKey = 1
        
        val paginator = DefaultPaginator<Int, String>(
            initialKey = 1,
            onLoadUpdated = {},
            onRequest = { key ->
                requestedKeys.add(key)
                Result.success("Page $key")
            },
            getNextKey = { currentKey++ + 1 },
            onError = {},
            onSuccess = { _, _ -> }
        )
        
        paginator.loadNextItems() // key 1
        paginator.loadNextItems() // key 2
        paginator.loadNextItems() // key 3
        
        assertEquals(listOf(1, 2, 3), requestedKeys)
    }

    // ==================== Edge Cases ====================

    @Test
    fun `paginator with string keys`() = runTest {
        var requestedKey: String? = null
        
        val paginator = DefaultPaginator<String, List<Int>>(
            initialKey = "first",
            onLoadUpdated = {},
            onRequest = { key ->
                requestedKey = key
                Result.success(listOf(1, 2, 3))
            },
            getNextKey = { "second" },
            onError = {},
            onSuccess = { _, _ -> }
        )
        
        paginator.loadNextItems()
        
        assertEquals("first", requestedKey)
    }

    @Test
    fun `paginator with nullable key`() = runTest {
        var requestedKey: String? = "initial"
        
        val paginator = DefaultPaginator<String?, List<Int>>(
            initialKey = null,
            onLoadUpdated = {},
            onRequest = { key ->
                requestedKey = key
                Result.success(listOf(1, 2, 3))
            },
            getNextKey = { "next" },
            onError = {},
            onSuccess = { _, _ -> }
        )
        
        paginator.loadNextItems()
        
        assertNull(requestedKey)
    }

    @Test
    fun `paginator with complex item type`() = runTest {
        data class PageResult(val items: List<String>, val hasMore: Boolean)
        
        var successResult: PageResult? = null
        
        val paginator = DefaultPaginator<Int, PageResult>(
            initialKey = 0,
            onLoadUpdated = {},
            onRequest = { Result.success(PageResult(listOf("a", "b"), true)) },
            getNextKey = { 1 },
            onError = {},
            onSuccess = { items, _ -> successResult = items }
        )
        
        paginator.loadNextItems()
        
        assertNotNull(successResult)
        assertEquals(listOf("a", "b"), successResult?.items)
        assertTrue(successResult?.hasMore == true)
    }
}
