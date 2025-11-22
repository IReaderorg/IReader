package ireader.domain.js.converter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class JSPluginAnalyzerASTTest {
    
    private val analyzer = JSPluginAnalyzerAST()
    
    @Test
    fun `test analyze simple plugin`() {
        val jsCode = """
            {
                id: "test-plugin",
                name: "Test Plugin",
                version: "1.0.0",
                lang: "en",
                site: "https://example.com",
                icon: "src/icon.png"
            }
        """.trimIndent()
        
        val analyzed = analyzer.analyze(jsCode)
        
        assertEquals("Test Plugin", analyzed.metadata.name)
        assertEquals("test-plugin", analyzed.metadata.id)
        assertEquals("1.0.0", analyzed.metadata.version)
        assertEquals("en", analyzed.metadata.lang)
        assertEquals("https://example.com", analyzed.metadata.baseUrl)
        assertEquals("src/icon.png", analyzed.metadata.icon)
    }
    
    @Test
    fun `test analyze plugin with search function`() {
        val jsCode = """
            {
                id: "test",
                name: "Test",
                site: "https://example.com"
            }
            
            async function searchNovels(query, page) {
                const url = `${'$'}{this.site}/search?q=${'$'}{query}&page=${'$'}{page}`;
                const html = await fetchApi(url);
                return ${'$'}(html).select('.novel-item').map(el => ({
                    title: ${'$'}(el).find('.title').text(),
                    url: ${'$'}(el).find('a').attr('href')
                }));
            }
        """.trimIndent()
        
        val analyzed = analyzer.analyze(jsCode)
        
        assertNotNull(analyzed.searchPattern)
        assertEquals("Test", analyzed.metadata.name)
        
        val search = analyzed.searchPattern!!
        assertEquals(".novel-item", search.selector)
        assertEquals(".title", search.titleSelector)
    }
    
    @Test
    fun `test analyze plugin with details function`() {
        val jsCode = """
            {
                id: "test",
                name: "Test",
                site: "https://example.com"
            }
            
            async function getDetails(url) {
                const html = await fetchApi(url);
                const doc = ${'$'}(html);
                return {
                    author: doc.select('.author').text(),
                    description: doc.select('.description').text(),
                    genres: doc.select('.genre').map(el => ${'$'}(el).text()),
                    status: doc.select('.status').text()
                };
            }
        """.trimIndent()
        
        val analyzed = analyzer.analyze(jsCode)
        
        assertNotNull(analyzed.detailsPattern)
        
        val details = analyzed.detailsPattern!!
        assertEquals(".author", details.authorSelector)
        assertEquals(".description", details.descriptionSelector)
        assertEquals(".genre", details.genresSelector)
        assertEquals(".status", details.statusSelector)
    }
    
    @Test
    fun `test analyze plugin with content function`() {
        val jsCode = """
            {
                id: "test",
                name: "Test",
                site: "https://example.com"
            }
            
            async function getContent(url) {
                const html = await fetchApi(url);
                const paragraphs = ${'$'}(html).select('.chapter-content p');
                return paragraphs.map(p => ${'$'}(p).text()).join('\n\n');
            }
        """.trimIndent()
        
        val analyzed = analyzer.analyze(jsCode)
        
        assertNotNull(analyzed.contentPattern)
        
        val content = analyzed.contentPattern!!
        assertTrue(content.selector.contains("chapter-content") || content.selector.contains("content"))
    }
    
    @Test
    fun `test analyze minified plugin`() {
        val jsCode = """{id:"wuxiaworld",name:"WuxiaWorld",version:"1.0.0",lang:"en",site:"https://wuxiaworld.com",icon:"src/wuxiaworld.png",searchNovels:async function(e){const t=await fetchApi(`${'$'}{this.site}/search?q=${'$'}{e}`);return ${'$'}(t).select(".novel-item").map(e=>({title:${'$'}(e).find(".title").text(),url:${'$'}(e).find("a").attr("href")}))}}"""
        
        val analyzed = analyzer.analyze(jsCode)
        
        assertEquals("WuxiaWorld", analyzed.metadata.name)
        assertEquals("wuxiaworld", analyzed.metadata.id)
        assertEquals("https://wuxiaworld.com", analyzed.metadata.baseUrl)
        assertNotNull(analyzed.searchPattern)
    }
    
    @Test
    fun `test analyze plugin without search function`() {
        val jsCode = """
            {
                id: "test",
                name: "Test",
                site: "https://example.com"
            }
        """.trimIndent()
        
        val analyzed = analyzer.analyze(jsCode)
        
        assertEquals("Test", analyzed.metadata.name)
        assertNull(analyzed.searchPattern)
    }
    
    @Test
    fun `test metadata extraction with fallbacks`() {
        val jsCode = """
            {
                id: "test-id"
            }
        """.trimIndent()
        
        val analyzed = analyzer.analyze(jsCode)
        
        // Should generate name from ID
        assertEquals("Test Id", analyzed.metadata.name)
        assertEquals("test-id", analyzed.metadata.id)
        // Should have default values
        assertEquals("1.0.0", analyzed.metadata.version)
        assertEquals("en", analyzed.metadata.lang)
    }
    
    @Test
    fun `test URL extraction from function body`() {
        val jsCode = """
            async function searchNovels(query) {
                const url = this.site + "/api/search?q=" + query;
                return fetchApi(url);
            }
        """.trimIndent()
        
        val analyzed = analyzer.analyze(jsCode)
        
        assertNotNull(analyzed.searchPattern)
        assertTrue(analyzed.searchPattern!!.urlTemplate.contains("search"))
    }
    
    @Test
    fun `test selector extraction with multiple patterns`() {
        val jsCode = """
            async function searchNovels(query) {
                const html = await fetchApi(query);
                return ${'$'}(html).select('.book-item, .novel-item').map(el => ({
                    title: ${'$'}(el).find('h3, .title').text()
                }));
            }
        """.trimIndent()
        
        val analyzed = analyzer.analyze(jsCode)
        
        assertNotNull(analyzed.searchPattern)
        // Should find one of the selectors
        val selector = analyzed.searchPattern!!.selector
        assertTrue(selector.contains("book-item") || selector.contains("novel-item") || selector.contains("item"))
    }
}
