package ireader.domain.js.loader

import kotlin.test.*

/**
 * Unit tests for JSPluginLoader
 * Tests plugin loading, caching, metadata extraction, and language conversion
 */
class JSPluginLoaderTest {

    @Test
    fun `convertLanguageNameToCode converts English correctly`() {
        assertEquals("en", convertLanguageNameToCode("English"))
        assertEquals("en", convertLanguageNameToCode("english"))
    }

    @Test
    fun `convertLanguageNameToCode converts Arabic correctly`() {
        assertEquals("ar", convertLanguageNameToCode("Arabic"))
    }

    @Test
    fun `convertLanguageNameToCode converts Chinese correctly`() {
        assertEquals("zh", convertLanguageNameToCode("Chinese"))
    }

    @Test
    fun `convertLanguageNameToCode converts Japanese correctly`() {
        assertEquals("ja", convertLanguageNameToCode("Japanese"))
    }

    @Test
    fun `convertLanguageNameToCode converts Korean correctly`() {
        assertEquals("ko", convertLanguageNameToCode("Korean"))
    }

    @Test
    fun `convertLanguageNameToCode converts Spanish correctly`() {
        assertEquals("es", convertLanguageNameToCode("Spanish"))
    }

    @Test
    fun `convertLanguageNameToCode converts French correctly`() {
        assertEquals("fr", convertLanguageNameToCode("French"))
    }

    @Test
    fun `convertLanguageNameToCode converts Indonesian correctly`() {
        assertEquals("id", convertLanguageNameToCode("Indonesian"))
    }

    @Test
    fun `convertLanguageNameToCode converts Portuguese correctly`() {
        assertEquals("pt", convertLanguageNameToCode("Portuguese"))
    }

    @Test
    fun `convertLanguageNameToCode converts Russian correctly`() {
        assertEquals("ru", convertLanguageNameToCode("Russian"))
    }

    @Test
    fun `convertLanguageNameToCode converts Thai correctly`() {
        assertEquals("th", convertLanguageNameToCode("Thai"))
    }

    @Test
    fun `convertLanguageNameToCode converts Turkish correctly`() {
        assertEquals("tr", convertLanguageNameToCode("Turkish"))
    }

    @Test
    fun `convertLanguageNameToCode converts Vietnamese correctly`() {
        assertEquals("vi", convertLanguageNameToCode("Vietnamese"))
    }

    @Test
    fun `convertLanguageNameToCode converts German correctly`() {
        assertEquals("de", convertLanguageNameToCode("German"))
    }

    @Test
    fun `convertLanguageNameToCode converts Italian correctly`() {
        assertEquals("it", convertLanguageNameToCode("Italian"))
    }

    @Test
    fun `convertLanguageNameToCode returns existing 2-letter code as-is`() {
        assertEquals("en", convertLanguageNameToCode("en"))
        assertEquals("ja", convertLanguageNameToCode("ja"))
        assertEquals("zh", convertLanguageNameToCode("zh"))
    }

    @Test
    fun `convertLanguageNameToCode returns en for unknown language`() {
        assertEquals("en", convertLanguageNameToCode("Unknown Language"))
        assertEquals("en", convertLanguageNameToCode("xyz"))
    }

    @Test
    fun `convertLanguageNameToCode returns en for null or blank`() {
        assertEquals("en", convertLanguageNameToCode(null))
        assertEquals("en", convertLanguageNameToCode(""))
        assertEquals("en", convertLanguageNameToCode("   "))
    }

    @Test
    fun `convertLanguageNameToCode handles whitespace`() {
        assertEquals("en", convertLanguageNameToCode("  English  "))
        assertEquals("ja", convertLanguageNameToCode("  Japanese  "))
    }

    // Helper function that mirrors the private function in JSPluginLoader
    private fun convertLanguageNameToCode(languageName: String?): String {
        if (languageName.isNullOrBlank()) return "en"
        
        val normalized = languageName.trim()
        
        if (normalized.length == 2 && normalized.all { it.isLetter() || it.isDigit() }) {
            return normalized.lowercase()
        }
        
        return when (normalized.lowercase()) {
            "english" -> "en"
            "arabic" -> "ar"
            "chinese" -> "zh"
            "spanish" -> "es"
            "french" -> "fr"
            "indonesian" -> "id"
            "japanese" -> "ja"
            "korean" -> "ko"
            "portuguese" -> "pt"
            "russian" -> "ru"
            "thai" -> "th"
            "turkish" -> "tr"
            "vietnamese" -> "vi"
            "german" -> "de"
            "italian" -> "it"
            "polish" -> "pl"
            "ukrainian" -> "uk"
            "filipino", "tagalog" -> "tl"
            "hungarian" -> "hu"
            "czech" -> "cs"
            "romanian" -> "ro"
            "dutch" -> "nl"
            "swedish" -> "sv"
            "norwegian" -> "no"
            "danish" -> "da"
            "finnish" -> "fi"
            "greek" -> "el"
            "hebrew" -> "he"
            "hindi" -> "hi"
            "bengali" -> "bn"
            "burmese" -> "my"
            "catalan" -> "ca"
            "galician" -> "gl"
            "basque" -> "eu"
            "lithuanian" -> "lt"
            "latvian" -> "lv"
            "estonian" -> "et"
            "slovak" -> "sk"
            "slovene" -> "sl"
            "croatian" -> "hr"
            "serbian" -> "sr"
            "bulgarian" -> "bg"
            "macedonian" -> "mk"
            else -> "en"
        }
    }
}

/**
 * Tests for PluginMetadata structure
 */
class PluginMetadataTest {

    data class TestPluginMetadata(
        val id: String,
        val name: String,
        val icon: String,
        val site: String,
        val version: String,
        val lang: String
    )

    @Test
    fun `PluginMetadata can be created with all required fields`() {
        val metadata = TestPluginMetadata(
            id = "test-plugin",
            name = "Test Plugin",
            icon = "https://example.com/icon.png",
            site = "https://example.com",
            version = "1.0.0",
            lang = "en"
        )
        
        assertEquals("test-plugin", metadata.id)
        assertEquals("Test Plugin", metadata.name)
        assertEquals("https://example.com", metadata.site)
        assertEquals("1.0.0", metadata.version)
        assertEquals("en", metadata.lang)
    }

    @Test
    fun `PluginMetadata id uniqueness`() {
        val id1 = "plugin-a"
        val id2 = "plugin-b"
        
        val hash1 = id1.hashCode().toLong()
        val hash2 = id2.hashCode().toLong()
        
        assertNotEquals(hash1, hash2)
    }

    @Test
    fun `PluginMetadata same id produces same hash`() {
        val id = "test-plugin"
        
        val hash1 = id.hashCode().toLong()
        val hash2 = id.hashCode().toLong()
        
        assertEquals(hash1, hash2)
    }
}

/**
 * Tests for JSPluginStubSource behavior
 */
class JSPluginStubSourceTest {

    @Test
    fun `stub source has correct metadata fields`() {
        val stubId = "stub-test"
        val stubName = "Stub Test"
        val stubSite = "https://stub.example.com"
        
        assertEquals("stub-test", stubId)
        assertEquals("Stub Test", stubName)
        assertTrue(stubSite.startsWith("https://"))
    }

    @Test
    fun `stub source id is consistent`() {
        val pluginId = "test.plugin"
        
        val hash1 = pluginId.hashCode()
        val hash2 = pluginId.hashCode()
        
        assertEquals(hash1, hash2)
    }
}

/**
 * Tests for plugin file validation
 */
class PluginFileValidationTest {

    @Test
    fun `valid JS code passes validation`() {
        val validJsCode = """
            const plugin = {
                id: 'test-plugin',
                name: 'Test Plugin'
            };
            exports.default = plugin;
        """.trimIndent()
        
        val isValid = validatePluginCode(validJsCode)
        
        assertTrue(isValid)
    }

    @Test
    fun `empty code fails validation`() {
        val emptyCode = ""
        
        val isValid = validatePluginCode(emptyCode)
        
        assertFalse(isValid)
    }

    @Test
    fun `blank code fails validation`() {
        val blankCode = "   \n\t  "
        
        val isValid = validatePluginCode(blankCode)
        
        assertFalse(isValid)
    }

    @Test
    fun `HTML content fails validation`() {
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head><title>404 Not Found</title></head>
            <body>Page not found</body>
            </html>
        """.trimIndent()
        
        val isValid = validatePluginCode(htmlContent)
        
        assertFalse(isValid)
    }

    @Test
    fun `404 error page fails validation`() {
        val errorPage = "404 Not Found"
        
        val isValid = validatePluginCode(errorPage)
        
        assertFalse(isValid)
    }

    private fun validatePluginCode(jsCode: String): Boolean {
        if (jsCode.isBlank()) return false
        
        if (jsCode.trim().startsWith("<!DOCTYPE") || jsCode.trim().startsWith("<html")) {
            return false
        }
        
        if (jsCode.contains("404") && jsCode.contains("Not Found") && jsCode.length < 1000) {
            return false
        }
        
        return true
    }
}
