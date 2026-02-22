package ireader.domain.services.sync

import java.io.File

/**
 * Desktop-specific test factory for KeyStorageService.
 */
actual fun createKeyStorageService(): KeyStorageService {
    // Use a temporary keystore for testing
    val tempDir = System.getProperty("java.io.tmpdir")
    val testKeystorePath = File(tempDir, "test_sync_keystore_${System.currentTimeMillis()}.jks").absolutePath
    val testPassword = "test_password".toCharArray()
    
    return DesktopKeyStorageService(testKeystorePath, testPassword)
}
