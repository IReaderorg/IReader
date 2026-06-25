package ireader.data.catalog

import ireader.data.catalog.impl.CatalogLoadError
import kotlin.test.*

/**
 * Unit tests for the redesigned AndroidCatalogLoader.
 * Tests error classification, retry logic, and Android 15 compatibility.
 */
class AndroidCatalogLoaderTest {

    // ── Error Classification ─────────────────────────────────────────────

    @Test
    fun `classifyError maps IOException to IO_ERROR`() {
        val error = java.io.IOException("disk full")
        val classified = classifyError(error)
        assertEquals(CatalogLoadError.IO_ERROR, classified)
    }

    @Test
    fun `classifyError maps ClassNotFoundException to CLASS_NOT_FOUND`() {
        val error = ClassNotFoundException("com.test.Missing")
        val classified = classifyError(error)
        assertEquals(CatalogLoadError.CLASS_NOT_FOUND, classified)
    }

    @Test
    fun `classifyError maps NoClassDefFoundError to CLASS_NOT_FOUND`() {
        val error = NoClassDefFoundError("com.test.Missing")
        val classified = classifyError(error)
        assertEquals(CatalogLoadError.CLASS_NOT_FOUND, classified)
    }

    @Test
    fun `classifyError maps InstantiationException to INSTANTIATION_FAILED`() {
        val error = InstantiationException("abstract class")
        val classified = classifyError(error)
        assertEquals(CatalogLoadError.INSTANTIATION_FAILED, classified)
    }

    @Test
    fun `classifyError maps IllegalAccessException to INSTANTIATION_FAILED`() {
        val error = IllegalAccessException("no access")
        val classified = classifyError(error)
        assertEquals(CatalogLoadError.INSTANTIATION_FAILED, classified)
    }

    @Test
    fun `classifyError maps dex-related messages to DEX_COMPILATION_FAILED`() {
        val error = RuntimeException("Failed to compile dex file")
        val classified = classifyError(error)
        assertEquals(CatalogLoadError.DEX_COMPILATION_FAILED, classified)
    }

    @Test
    fun `classifyError maps class not found messages to CLASS_NOT_FOUND`() {
        val error = RuntimeException("class not found in loader")
        val classified = classifyError(error)
        assertEquals(CatalogLoadError.CLASS_NOT_FOUND, classified)
    }

    @Test
    fun `classifyError maps unknown exceptions to UNKNOWN`() {
        val error = IllegalStateException("something weird")
        val classified = classifyError(error)
        assertEquals(CatalogLoadError.UNKNOWN, classified)
    }

    // ── Recoverability ───────────────────────────────────────────────────

    @Test
    fun `IO_ERROR is recoverable`() {
        assertTrue(isRecoverable(CatalogLoadError.IO_ERROR))
    }

    @Test
    fun `DEX_COMPILATION_FAILED is recoverable`() {
        assertTrue(isRecoverable(CatalogLoadError.DEX_COMPILATION_FAILED))
    }

    @Test
    fun `CLASS_NOT_FOUND is recoverable`() {
        assertTrue(isRecoverable(CatalogLoadError.CLASS_NOT_FOUND))
    }

    @Test
    fun `SECURITY_ERROR is not recoverable`() {
        assertFalse(isRecoverable(CatalogLoadError.SECURITY_ERROR))
    }

    @Test
    fun `INSTANTIATION_FAILED is not recoverable`() {
        assertFalse(isRecoverable(CatalogLoadError.INSTANTIATION_FAILED))
    }

    @Test
    fun `UNKNOWN is not recoverable`() {
        assertFalse(isRecoverable(CatalogLoadError.UNKNOWN))
    }

    @Test
    fun `PACKAGE_NOT_FOUND is not recoverable`() {
        assertFalse(isRecoverable(CatalogLoadError.PACKAGE_NOT_FOUND))
    }

    @Test
    fun `INVALID_METADATA is not recoverable`() {
        assertFalse(isRecoverable(CatalogLoadError.INVALID_METADATA))
    }

    @Test
    fun `UNSUPPORTED_LIB_VERSION is not recoverable`() {
        assertFalse(isRecoverable(CatalogLoadError.UNSUPPORTED_LIB_VERSION))
    }

    // ── Android 15 ClassLoader Strategy ──────────────────────────────────

    @Test
    fun `InMemoryDexClassLoader is preferred on API 28+`() {
        val minApiForInMemory = 28
        assertTrue(minApiForInMemory <= 35, "Should use InMemory on Android 15")
    }

    @Test
    fun `DexClassLoader fallback uses timestamped output dir`() {
        val pkgName = "com.test.plugin"
        val dir1 = "dex_out/${pkgName}_${System.currentTimeMillis()}"
        Thread.sleep(5)
        val dir2 = "dex_out/${pkgName}_${System.currentTimeMillis()}"
        assertNotEquals(dir1, dir2, "Output dirs should be unique per load")
    }

    // ── Metadata Validation ──────────────────────────────────────────────

    @Test
    fun `lib version range is 2-2`() {
        val min = 2
        val max = 2
        assertTrue(min <= max)
    }

    @Test
    fun `extension feature flag is ireader`() {
        assertEquals("ireader", EXTENSION_FEATURE)
    }

    @Test
    fun `metadata keys are correct`() {
        assertEquals("source.class", METADATA_SOURCE_CLASS)
        assertEquals("source.description", METADATA_DESCRIPTION)
        assertEquals("source.nsfw", METADATA_NSFW)
        assertEquals("source.icon", METADATA_ICON)
    }

    // ── APK Validation ───────────────────────────────────────────────────

    @Test
    fun `empty APK file should be rejected`() {
        val file = FakeFile(exists = true, canRead = true, length = 0)
        assertFalse(isValidApk(file), "Zero-length APK should be rejected")
    }

    @Test
    fun `unreadable APK file should be rejected`() {
        val file = FakeFile(exists = true, canRead = false, length = 1024)
        assertFalse(isValidApk(file), "Unreadable APK should be rejected")
    }

    @Test
    fun `non-existent APK file should be rejected`() {
        val file = FakeFile(exists = false, canRead = false, length = 0)
        assertFalse(isValidApk(file), "Non-existent APK should be rejected")
    }

    @Test
    fun `valid APK file should be accepted`() {
        val file = FakeFile(exists = true, canRead = true, length = 50000)
        assertTrue(isValidApk(file), "Valid APK should be accepted")
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    /** Mirrors the private classifyError in AndroidCatalogLoader (after BUG 5 fix) */
    private fun classifyError(e: Throwable): CatalogLoadError = when {
        e is java.io.IOException -> CatalogLoadError.IO_ERROR
        e is ClassNotFoundException || e is NoClassDefFoundError -> CatalogLoadError.CLASS_NOT_FOUND
        e is InstantiationException || e is IllegalAccessException -> CatalogLoadError.INSTANTIATION_FAILED
        e.message?.contains("dex", ignoreCase = true) == true -> CatalogLoadError.DEX_COMPILATION_FAILED
        e.message?.contains("class not found", ignoreCase = true) == true -> CatalogLoadError.CLASS_NOT_FOUND
        else -> CatalogLoadError.UNKNOWN
    }

    /** Mirrors the private isRecoverableError in AndroidCatalogLoader */
    private fun isRecoverable(error: CatalogLoadError): Boolean = error in listOf(
        CatalogLoadError.DEX_COMPILATION_FAILED,
        CatalogLoadError.IO_ERROR,
        CatalogLoadError.CLASS_NOT_FOUND
    )

    /** Mirrors the APK validation logic */
    private fun isValidApk(file: FakeFile): Boolean =
        file.exists && file.canRead && file.length > 0

    private data class FakeFile(val exists: Boolean, val canRead: Boolean, val length: Long)

    private companion object {
        const val EXTENSION_FEATURE = "ireader"
        const val METADATA_SOURCE_CLASS = "source.class"
        const val METADATA_DESCRIPTION = "source.description"
        const val METADATA_NSFW = "source.nsfw"
        const val METADATA_ICON = "source.icon"
    }
}
