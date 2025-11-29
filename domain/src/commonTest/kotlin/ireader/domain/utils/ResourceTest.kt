package ireader.domain.utils

import ireader.i18n.UiText
import kotlin.test.*

/**
 * Unit tests for Resource sealed class
 */
class ResourceTest {

    // ==================== Success Tests ====================

    @Test
    fun `Success contains data`() {
        val data = "Test Data"
        
        val resource = Resource.Success(data)
        
        assertEquals("Test Data", resource.data)
    }

    @Test
    fun `Success with null data`() {
        val resource = Resource.Success<String>(null)
        
        assertNull(resource.data)
    }

    @Test
    fun `Success has null uiText`() {
        val resource = Resource.Success("data")
        
        assertNull(resource.uiText)
    }

    @Test
    fun `Success with complex type`() {
        data class User(val id: Int, val name: String)
        val user = User(1, "John")
        
        val resource = Resource.Success(user)
        
        assertEquals(user, resource.data)
        assertEquals(1, resource.data?.id)
        assertEquals("John", resource.data?.name)
    }

    @Test
    fun `Success with list type`() {
        val list = listOf(1, 2, 3, 4, 5)
        
        val resource = Resource.Success(list)
        
        assertEquals(5, resource.data?.size)
        assertEquals(listOf(1, 2, 3, 4, 5), resource.data)
    }

    // ==================== Error Tests ====================

    @Test
    fun `Error contains uiText`() {
        val errorText = UiText.DynamicString("Error occurred")
        
        val resource = Resource.Error<String>(errorText)
        
        assertEquals(errorText, resource.uiText)
    }

    @Test
    fun `Error has null data by default`() {
        val errorText = UiText.DynamicString("Error")
        
        val resource = Resource.Error<String>(errorText)
        
        assertNull(resource.data)
    }

    @Test
    fun `Error can contain data`() {
        val errorText = UiText.DynamicString("Partial error")
        val partialData = "Partial Data"
        
        val resource = Resource.Error(errorText, partialData)
        
        assertEquals(errorText, resource.uiText)
        assertEquals("Partial Data", resource.data)
    }

    @Test
    fun `Error with complex type and partial data`() {
        data class Result(val items: List<String>, val total: Int)
        val errorText = UiText.DynamicString("Failed to load all items")
        val partialResult = Result(listOf("item1"), 100)
        
        val resource = Resource.Error(errorText, partialResult)
        
        assertEquals(partialResult, resource.data)
        assertEquals(1, resource.data?.items?.size)
    }

    // ==================== Type Checking Tests ====================

    @Test
    fun `Success is instance of Resource`() {
        val resource: Resource<String> = Resource.Success("data")
        
        assertTrue(resource is Resource.Success<*>)
        assertFalse(resource is Resource.Error<*>)
    }

    @Test
    fun `Error is instance of Resource`() {
        val resource: Resource<String> = Resource.Error(UiText.DynamicString("error"))
        
        assertTrue(resource is Resource.Error<*>)
        assertFalse(resource is Resource.Success<*>)
    }

    // ==================== When Expression Tests ====================

    @Test
    fun `when expression handles Success`() {
        val resource: Resource<Int> = Resource.Success(42)
        
        val result = when (resource) {
            is Resource.Success -> "Success: ${resource.data}"
            is Resource.Error -> "Error: ${resource.uiText}"
        }
        
        assertEquals("Success: 42", result)
    }

    @Test
    fun `when expression handles Error`() {
        val errorText = UiText.DynamicString("Something went wrong")
        val resource: Resource<Int> = Resource.Error(errorText)
        
        val result = when (resource) {
            is Resource.Success -> "Success: ${resource.data}"
            is Resource.Error -> "Error"
        }
        
        assertEquals("Error", result)
    }

    // ==================== Edge Cases ====================

    @Test
    fun `Success with empty string`() {
        val resource = Resource.Success("")
        
        assertEquals("", resource.data)
    }

    @Test
    fun `Success with empty list`() {
        val resource = Resource.Success(emptyList<Int>())
        
        assertTrue(resource.data?.isEmpty() == true)
    }

    @Test
    fun `Error with empty error message`() {
        val errorText = UiText.DynamicString("")
        
        val resource = Resource.Error<String>(errorText)
        
        assertNotNull(resource.uiText)
    }

    @Test
    fun `Multiple Success instances are independent`() {
        val resource1 = Resource.Success("data1")
        val resource2 = Resource.Success("data2")
        
        assertNotEquals(resource1.data, resource2.data)
    }

    @Test
    fun `Success with nullable generic type`() {
        val resource: Resource<String?> = Resource.Success(null)
        
        assertNull(resource.data)
    }

    // ==================== Practical Usage Tests ====================

    @Test
    fun `simulate API response success`() {
        data class ApiResponse(val id: Int, val message: String)
        
        fun fetchData(): Resource<ApiResponse> {
            return Resource.Success(ApiResponse(1, "Hello"))
        }
        
        val result = fetchData()
        
        assertTrue(result is Resource.Success)
        assertEquals(1, result.data?.id)
        assertEquals("Hello", result.data?.message)
    }

    @Test
    fun `simulate API response error`() {
        data class ApiResponse(val id: Int, val message: String)
        
        fun fetchData(): Resource<ApiResponse> {
            return Resource.Error(UiText.DynamicString("Network error"))
        }
        
        val result = fetchData()
        
        assertTrue(result is Resource.Error)
        assertNull(result.data)
    }

    @Test
    fun `simulate partial success with cached data`() {
        data class CachedData(val items: List<String>, val isFresh: Boolean)
        
        fun fetchWithCache(): Resource<CachedData> {
            val cachedData = CachedData(listOf("cached1", "cached2"), false)
            return Resource.Error(
                UiText.DynamicString("Network unavailable, showing cached data"),
                cachedData
            )
        }
        
        val result = fetchWithCache()
        
        assertTrue(result is Resource.Error)
        assertNotNull(result.data)
        assertEquals(2, result.data?.items?.size)
        assertFalse(result.data?.isFresh == true)
    }
}
