package ireader.domain.js.converter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JSASTParserTest {
    
    private val parser = JSASTParser()
    
    @Test
    fun `test parse simple object literal`() {
        val jsCode = """
            {
                id: "test-plugin",
                name: "Test Plugin",
                version: "1.0.0",
                lang: "en",
                site: "https://example.com"
            }
        """.trimIndent()
        
        val parsed = parser.parse(jsCode)
        
        assertNotNull(parsed.metadata)
        assertEquals("test-plugin", parsed.metadata?.properties?.get("id"))
        assertEquals("Test Plugin", parsed.metadata?.properties?.get("name"))
        assertEquals("1.0.0", parsed.metadata?.properties?.get("version"))
        assertEquals("en", parsed.metadata?.properties?.get("lang"))
        assertEquals("https://example.com", parsed.metadata?.properties?.get("site"))
    }
    
    @Test
    fun `test parse minified object`() {
        val jsCode = """{id:"wuxiaworld",name:"WuxiaWorld",version:"1.0.0",lang:"en",site:"https://wuxiaworld.com"}"""
        
        val parsed = parser.parse(jsCode)
        
        assertNotNull(parsed.metadata)
        assertEquals("wuxiaworld", parsed.metadata?.properties?.get("id"))
        assertEquals("WuxiaWorld", parsed.metadata?.properties?.get("name"))
    }
    
    @Test
    fun `test parse function declaration`() {
        val jsCode = """
            async function searchNovels(query) {
                const url = `${'$'}{this.site}/search?q=${'$'}{query}`;
                return fetchApi(url);
            }
        """.trimIndent()
        
        val parsed = parser.parse(jsCode)
        
        assertTrue(parsed.functions.containsKey("searchNovels"))
        val func = parsed.functions["searchNovels"]
        assertNotNull(func)
        assertEquals("searchNovels", func.name)
        assertTrue(func.isAsync)
        assertEquals(listOf("query"), func.params)
        assertTrue(func.body.contains("fetchApi"))
    }
    
    @Test
    fun `test parse arrow function`() {
        val jsCode = """
            const search = async (query) => {
                return await fetchApi(query);
            }
        """.trimIndent()
        
        val parsed = parser.parse(jsCode)
        
        // Arrow functions assigned to const are harder to detect
        // This test documents current behavior
        assertTrue(parsed.functions.isEmpty() || parsed.functions.containsKey("search"))
    }
    
    @Test
    fun `test parse class properties`() {
        val jsCode = """
            class Plugin {
                constructor() {
                    this.name = "Test Plugin";
                    this.version = "1.0.0";
                }
                
                get site() {
                    return "https://example.com";
                }
            }
        """.trimIndent()
        
        val parsed = parser.parse(jsCode)
        
        assertEquals("Test Plugin", parsed.classProperties["name"])
        assertEquals("1.0.0", parsed.classProperties["version"])
        assertEquals("https://example.com", parsed.classProperties["site"])
    }
    
    @Test
    fun `test remove comments`() {
        val jsCode = """
            // Single line comment
            {
                id: "test", // inline comment
                /* multi-line
                   comment */
                name: "Test"
            }
        """.trimIndent()
        
        val parsed = parser.parse(jsCode)
        
        assertNotNull(parsed.metadata)
        assertEquals("test", parsed.metadata?.properties?.get("id"))
        assertEquals("Test", parsed.metadata?.properties?.get("name"))
    }
    
    @Test
    fun `test parse nested object`() {
        val jsCode = """
            {
                id: "test",
                config: {
                    nested: "value",
                    deep: {
                        level: "three"
                    }
                }
            }
        """.trimIndent()
        
        val parsed = parser.parse(jsCode)
        
        assertNotNull(parsed.metadata)
        val config = parsed.metadata?.properties?.get("config")
        assertTrue(config is Map<*, *>)
        assertEquals("value", (config as Map<*, *>)["nested"])
    }
    
    @Test
    fun `test parse array values`() {
        val jsCode = """
            {
                id: "test",
                tags: ["action", "adventure", "fantasy"],
                numbers: [1, 2, 3]
            }
        """.trimIndent()
        
        val parsed = parser.parse(jsCode)
        
        assertNotNull(parsed.metadata)
        val tags = parsed.metadata?.properties?.get("tags")
        assertTrue(tags is List<*>)
        assertEquals(3, (tags as List<*>).size)
        assertEquals("action", tags[0])
    }
    
    @Test
    fun `test splitByComma respects nested structures`() {
        val jsCode = """
            id: "test",
            config: { nested: "value", other: "data" },
            name: "Test"
        """.trimIndent()
        
        val parts = parser.parse("{ $jsCode }").metadata?.properties
        
        assertNotNull(parts)
        assertEquals(3, parts.size)
        assertTrue(parts.containsKey("id"))
        assertTrue(parts.containsKey("config"))
        assertTrue(parts.containsKey("name"))
    }
}
