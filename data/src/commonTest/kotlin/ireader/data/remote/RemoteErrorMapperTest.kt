package ireader.data.remote

import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Comprehensive tests for RemoteErrorMapper
 */
class RemoteErrorMapperTest {
    
    @Test
    fun `withErrorMapping should return success for successful operation`() = runTest {
        // When
        val result = RemoteErrorMapper.withErrorMapping {
            "success"
        }
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals("success", result.getOrNull())
    }
    
    @Test
    fun `withErrorMapping should return failure for exception`() = runTest {
        // When
        val result = RemoteErrorMapper.withErrorMapping {
            throw RuntimeException("Test error")
        }
        
        // Then
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `withErrorMapping should map maintenance error`() = runTest {
        // When
        val result = RemoteErrorMapper.withErrorMapping {
            throw RuntimeException("Service under maintenance")
        }
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("maintenance") == true)
    }
    
    @Test
    fun `withErrorMapping should map 503 error`() = runTest {
        // When
        val result = RemoteErrorMapper.withErrorMapping {
            throw RuntimeException("503 Service Unavailable")
        }
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("maintenance") == true)
    }
    
    @Test
    fun `withErrorMapping should map network error`() = runTest {
        // When
        val result = RemoteErrorMapper.withErrorMapping {
            throw RuntimeException("Unable to resolve host")
        }
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Network error") == true)
    }
    
    @Test
    fun `withErrorMapping should map timeout error`() = runTest {
        // When
        val result = RemoteErrorMapper.withErrorMapping {
            throw RuntimeException("Connection timeout")
        }
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("timed out") == true)
    }
    
    @Test
    fun `withErrorMapping should map unauthorized error`() = runTest {
        // When
        val result = RemoteErrorMapper.withErrorMapping {
            throw RuntimeException("401 Unauthorized")
        }
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Authentication failed") == true)
    }
    
    @Test
    fun `withErrorMapping should map not found error`() = runTest {
        // When
        val result = RemoteErrorMapper.withErrorMapping {
            throw RuntimeException("404 Not Found")
        }
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("not found") == true)
    }
    
    @Test
    fun `withErrorMapping should map bad gateway error`() = runTest {
        // When
        val result = RemoteErrorMapper.withErrorMapping {
            throw RuntimeException("502 Bad Gateway")
        }
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("temporarily unavailable") == true)
    }
    
    @Test
    fun `withErrorMapping should handle cancelled request`() = runTest {
        // When
        val result = RemoteErrorMapper.withErrorMapping {
            throw RuntimeException("Request cancelled")
        }
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("cancelled") == true)
    }
    
    @Test
    fun `withErrorMapping should handle unknown error`() = runTest {
        // When
        val result = RemoteErrorMapper.withErrorMapping {
            throw RuntimeException("Some unknown error")
        }
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("temporarily unavailable") == true)
    }
    
    @Test
    fun `withErrorMapping should handle different return types`() = runTest {
        // Int
        val intResult = RemoteErrorMapper.withErrorMapping { 42 }
        assertEquals(42, intResult.getOrNull())
        
        // List
        val listResult = RemoteErrorMapper.withErrorMapping { listOf(1, 2, 3) }
        assertEquals(listOf(1, 2, 3), listResult.getOrNull())
        
        // Nullable
        val nullResult = RemoteErrorMapper.withErrorMapping<String?> { null }
        assertNull(nullResult.getOrNull())
    }
}
