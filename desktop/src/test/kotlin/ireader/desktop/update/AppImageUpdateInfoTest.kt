package ireader.desktop.update

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNull

/**
 * TDD: RED phase - Tests for AppImageUpdateInfo
 * These tests will fail until we implement the actual classes
 */
class AppImageUpdateInfoTest {
    
    @Test
    fun `parseUpdateInfo should parse zsync transport correctly`() {
        // Arrange
        val updateInfoString = "zsync|https://github.com/IReaderorg/IReader/releases/latest/download/IReader-x86_64.AppImage.zsync"
        
        // Act
        val result = AppImageUpdateInfo.parse(updateInfoString)
        
        // Assert
        assertEquals(UpdateTransport.ZSYNC, result?.transport)
        assertEquals("https://github.com/IReaderorg/IReader/releases/latest/download/IReader-x86_64.AppImage.zsync", result?.url)
    }
    
    @Test
    fun `parseUpdateInfo should parse gh-releases-zsync transport correctly`() {
        // Arrange
        val updateInfoString = "gh-releases-zsync|IReaderorg|IReader|latest|IReader-x86_64.AppImage.zsync"
        
        // Act
        val result = AppImageUpdateInfo.parse(updateInfoString)
        
        // Assert
        assertEquals(UpdateTransport.GH_RELEASES_ZSYNC, result?.transport)
        assertEquals("IReaderorg", result?.owner)
        assertEquals("IReader", result?.repo)
        assertEquals("latest", result?.releaseTag)
        assertEquals("IReader-x86_64.AppImage.zsync", result?.filename)
    }
    
    @Test
    fun `parseUpdateInfo should return null for invalid format`() {
        // Arrange
        val invalidUpdateInfo = "invalid-format"
        
        // Act
        val result = AppImageUpdateInfo.parse(invalidUpdateInfo)
        
        // Assert
        assertNull(result)
    }
    
    @Test
    fun `parseUpdateInfo should return null for empty string`() {
        // Arrange
        val emptyUpdateInfo = ""
        
        // Act
        val result = AppImageUpdateInfo.parse(emptyUpdateInfo)
        
        // Assert
        assertNull(result)
    }
    
    @Test
    fun `toUpdateInfoString should format zsync correctly`() {
        // Arrange
        val updateInfo = AppImageUpdateInfo(
            transport = UpdateTransport.ZSYNC,
            url = "https://example.com/app.AppImage.zsync"
        )
        
        // Act
        val result = updateInfo.toUpdateInfoString()
        
        // Assert
        assertEquals("zsync|https://example.com/app.AppImage.zsync", result)
    }
    
    @Test
    fun `toUpdateInfoString should format gh-releases-zsync correctly`() {
        // Arrange
        val updateInfo = AppImageUpdateInfo(
            transport = UpdateTransport.GH_RELEASES_ZSYNC,
            owner = "IReaderorg",
            repo = "IReader",
            releaseTag = "latest",
            filename = "IReader-x86_64.AppImage.zsync"
        )
        
        // Act
        val result = updateInfo.toUpdateInfoString()
        
        // Assert
        assertEquals("gh-releases-zsync|IReaderorg|IReader|latest|IReader-x86_64.AppImage.zsync", result)
    }
    
    @Test
    fun `isValid should return true for valid zsync info`() {
        // Arrange
        val updateInfo = AppImageUpdateInfo(
            transport = UpdateTransport.ZSYNC,
            url = "https://example.com/app.AppImage.zsync"
        )
        
        // Act & Assert
        assertTrue(updateInfo.isValid())
    }
    
    @Test
    fun `isValid should return false for zsync without url`() {
        // Arrange
        val updateInfo = AppImageUpdateInfo(
            transport = UpdateTransport.ZSYNC,
            url = null
        )
        
        // Act & Assert
        assertFalse(updateInfo.isValid())
    }
    
    @Test
    fun `isValid should return true for valid gh-releases-zsync info`() {
        // Arrange
        val updateInfo = AppImageUpdateInfo(
            transport = UpdateTransport.GH_RELEASES_ZSYNC,
            owner = "IReaderorg",
            repo = "IReader",
            releaseTag = "latest",
            filename = "IReader-x86_64.AppImage.zsync"
        )
        
        // Act & Assert
        assertTrue(updateInfo.isValid())
    }
    
    @Test
    fun `isValid should return false for gh-releases-zsync with missing fields`() {
        // Arrange
        val updateInfo = AppImageUpdateInfo(
            transport = UpdateTransport.GH_RELEASES_ZSYNC,
            owner = "IReaderorg",
            repo = null,
            releaseTag = "latest",
            filename = "IReader-x86_64.AppImage.zsync"
        )
        
        // Act & Assert
        assertFalse(updateInfo.isValid())
    }
}
