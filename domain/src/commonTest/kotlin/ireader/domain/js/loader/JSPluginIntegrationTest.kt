package ireader.domain.js.loader

import kotlin.test.*

/**
 * Integration tests for JS Plugin loading
 * Tests real plugin code parsing and validation
 */
class JSPluginIntegrationTest {

    // Sample plugin code from AllNovelFull plugin (minified)
    private val samplePluginCode = """
        var e=this&&this.__assign||function(){return e=Object.assign||function(e){for(var t,a=1,n=arguments.length;a<n;a++)for(var r in t=arguments[a])Object.prototype.hasOwnProperty.call(t,r)&&(e[r]=t[r]);return e},e.apply(this,arguments)};
        Object.defineProperty(exports,"__esModule",{value:!0});
        var l=new (function(){
            function s(e){this.id=e.id,this.name=e.sourceName,this.site=e.sourceSite,this.version="2.1.2"}
            return s;
        }())({id:"anf.net",sourceSite:"https://novgo.net/",sourceName:"AllNovelFull"});
        exports.default=l;
    """.trimIndent()

    @Test
    fun `plugin code validation accepts valid JS`() {
        // Given
        val validator = PluginCodeValidator()
        
        // When
        val result = validator.validate(samplePluginCode)
        
        // Then
        assertTrue(result.isValid)
        assertNull(result.error)
    }

    @Test
    fun `plugin code validation rejects empty code`() {
        // Given
        val validator = PluginCodeValidator()
        
        // When
        val result = validator.validate("")
        
        // Then
        assertFalse(result.isValid)
        assertNotNull(result.error)
    }

    @Test
    fun `plugin code validation rejects HTML error pages`() {
        // Given
        val validator = PluginCodeValidator()
        val htmlError = """
            <!DOCTYPE html>
            <html>
            <head><title>404 Not Found</title></head>
            <body><h1>Not Found</h1></body>
            </html>
        """.trimIndent()
        
        // When
        val result = validator.validate(htmlError)
        
        // Then
        assertFalse(result.isValid)
        assertTrue(result.error?.contains("HTML") == true)
    }

    @Test
    fun `plugin code validation rejects 404 responses`() {
        // Given
        val validator = PluginCodeValidator()
        val errorResponse = "404 Not Found"
        
        // When
        val result = validator.validate(errorResponse)
        
        // Then
        assertFalse(result.isValid)
    }

    @Test
    fun `plugin metadata extraction finds exports`() {
        // Given
        val extractor = PluginMetadataExtractor()
        
        // When
        val hasExports = extractor.hasExports(samplePluginCode)
        
        // Then
        assertTrue(hasExports)
    }

    @Test
    fun `plugin metadata extraction detects module pattern`() {
        // Given
        val extractor = PluginMetadataExtractor()
        
        // When
        val isModule = extractor.isModulePattern(samplePluginCode)
        
        // Then
        assertTrue(isModule)
    }

    @Test
    fun `plugin code with async await is valid`() {
        // Given
        val validator = PluginCodeValidator()
        val asyncCode = """
            async function fetchData() {
                const response = await fetch('https://example.com');
                return response.json();
            }
            exports.default = { fetchData };
        """.trimIndent()
        
        // When
        val result = validator.validate(asyncCode)
        
        // Then
        assertTrue(result.isValid)
    }

    @Test
    fun `plugin code with Promise is valid`() {
        // Given
        val validator = PluginCodeValidator()
        val promiseCode = """
            function getData() {
                return new Promise((resolve, reject) => {
                    setTimeout(() => resolve('data'), 1000);
                });
            }
            exports.default = { getData };
        """.trimIndent()
        
        // When
        val result = validator.validate(promiseCode)
        
        // Then
        assertTrue(result.isValid)
    }

    @Test
    fun `real plugin structure is detected`() {
        // Given - actual plugin structure from AllNovelFull
        val realPluginCode = """
            var l=new s({id:"anf.net",sourceSite:"https://novgo.net/",sourceName:"AllNovelFull",options:{latestPage:"latest-release-novel",searchPage:"search"}});
            exports.default=l;
        """.trimIndent()
        val extractor = PluginMetadataExtractor()
        
        // When
        val hasDefaultExport = extractor.hasDefaultExport(realPluginCode)
        
        // Then
        assertTrue(hasDefaultExport)
    }
}

/**
 * Helper class for plugin code validation
 */
class PluginCodeValidator {
    
    data class ValidationResult(
        val isValid: Boolean,
        val error: String? = null
    )
    
    fun validate(code: String): ValidationResult {
        // Check for empty/blank code
        if (code.isBlank()) {
            return ValidationResult(false, "Plugin code is empty")
        }
        
        // Check for HTML content (error pages)
        val trimmed = code.trim()
        if (trimmed.startsWith("<!DOCTYPE") || trimmed.startsWith("<html")) {
            return ValidationResult(false, "Plugin file contains HTML instead of JavaScript")
        }
        
        // Check for 404 error responses
        if (code.contains("404") && code.contains("Not Found") && code.length < 1000) {
            return ValidationResult(false, "Plugin file appears to be a 404 error response")
        }
        
        // Check for basic JavaScript structure
        if (!hasBasicJSStructure(code)) {
            return ValidationResult(false, "Plugin code does not appear to be valid JavaScript")
        }
        
        return ValidationResult(true)
    }
    
    private fun hasBasicJSStructure(code: String): Boolean {
        // Check for common JS patterns
        return code.contains("function") ||
               code.contains("var ") ||
               code.contains("const ") ||
               code.contains("let ") ||
               code.contains("exports") ||
               code.contains("module") ||
               code.contains("class ")
    }
}

/**
 * Helper class for extracting plugin metadata from code
 */
class PluginMetadataExtractor {
    
    fun hasExports(code: String): Boolean {
        return code.contains("exports.") || 
               code.contains("module.exports") ||
               code.contains("export ")
    }
    
    fun hasDefaultExport(code: String): Boolean {
        return code.contains("exports.default") ||
               code.contains("module.exports") ||
               code.contains("export default")
    }
    
    fun isModulePattern(code: String): Boolean {
        return code.contains("__esModule") ||
               code.contains("exports") ||
               code.contains("require(")
    }
    
    /**
     * Extract plugin ID from code if possible
     */
    fun extractPluginId(code: String): String? {
        // Look for id: "..." pattern
        val idPattern = Regex("""id\s*[:=]\s*["']([^"']+)["']""")
        return idPattern.find(code)?.groupValues?.getOrNull(1)
    }
    
    /**
     * Extract plugin name from code if possible
     */
    fun extractPluginName(code: String): String? {
        // Look for name: "..." or sourceName: "..." pattern
        val namePattern = Regex("""(?:name|sourceName)\s*[:=]\s*["']([^"']+)["']""")
        return namePattern.find(code)?.groupValues?.getOrNull(1)
    }
    
    /**
     * Extract plugin site from code if possible
     */
    fun extractPluginSite(code: String): String? {
        // Look for site: "..." or sourceSite: "..." pattern
        val sitePattern = Regex("""(?:site|sourceSite)\s*[:=]\s*["']([^"']+)["']""")
        return sitePattern.find(code)?.groupValues?.getOrNull(1)
    }
}

/**
 * Tests for PluginMetadataExtractor with real plugin code
 */
class PluginMetadataExtractorTest {

    private val realPluginCode = """
        var l=new s({id:"anf.net",sourceSite:"https://novgo.net/",sourceName:"AllNovelFull",options:{latestPage:"latest-release-novel"}});
        exports.default=l;
    """.trimIndent()

    @Test
    fun `extractPluginId finds id in real plugin`() {
        // Given
        val extractor = PluginMetadataExtractor()
        
        // When
        val id = extractor.extractPluginId(realPluginCode)
        
        // Then
        assertEquals("anf.net", id)
    }

    @Test
    fun `extractPluginName finds name in real plugin`() {
        // Given
        val extractor = PluginMetadataExtractor()
        
        // When
        val name = extractor.extractPluginName(realPluginCode)
        
        // Then
        assertEquals("AllNovelFull", name)
    }

    @Test
    fun `extractPluginSite finds site in real plugin`() {
        // Given
        val extractor = PluginMetadataExtractor()
        
        // When
        val site = extractor.extractPluginSite(realPluginCode)
        
        // Then
        assertEquals("https://novgo.net/", site)
    }

    @Test
    fun `extractPluginId returns null for code without id`() {
        // Given
        val extractor = PluginMetadataExtractor()
        val codeWithoutId = "var x = 1; exports.default = x;"
        
        // When
        val id = extractor.extractPluginId(codeWithoutId)
        
        // Then
        assertNull(id)
    }
}
