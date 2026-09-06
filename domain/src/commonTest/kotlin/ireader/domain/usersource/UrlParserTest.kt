package ireader.domain.usersource

import ireader.domain.usersource.parser.UrlParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UrlParserTest {

    private val baseUrl = "https://www.example.com"

    @Test
    fun testStandardGetUrl() {
        val template = "/search?keyword={{key}}&p={{page}}"
        val request = UrlParser.parseRequest(template, baseUrl, key = "test novel", page = 2)

        assertEquals("GET", request.method)
        assertEquals("https://www.example.com/search?keyword=test+novel&p=2", request.url)
    }

    @Test
    fun testSearchPageAndPageIndexPlaceholders() {
        val template = "https://www.example.com/list/{{searchPage}}/index_{{page-1}}.html"
        val request = UrlParser.parseRequest(template, baseUrl, page = 3)

        assertEquals("https://www.example.com/list/3/index_2.html", request.url)
    }

    @Test
    fun testPagePatternAngleBrackets() {
        // Legado supports <1,2,3> page syntax
        val template = "/novels<,index_2.html,index_3.html>"
        val req1 = UrlParser.parseRequest(template, baseUrl, page = 1)
        assertEquals("https://www.example.com/novels", req1.url)

        val req2 = UrlParser.parseRequest(template, baseUrl, page = 2)
        assertEquals("https://www.example.com/novelsindex_2.html", req2.url)

        val req4 = UrlParser.parseRequest(template, baseUrl, page = 4)
        assertEquals("https://www.example.com/novelsindex_3.html", req4.url)
    }

    @Test
    fun testPostUrlWithOptionsStringBody() {
        val template = """
            https://www.example.com/search.php,{"method": "POST", "body": "searchkey={{key}}&page={{page}}"}
        """.trimIndent()

        val request = UrlParser.parseRequest(template, baseUrl, key = "Martial", page = 1)

        assertEquals("POST", request.method)
        assertEquals("https://www.example.com/search.php", request.url)
        assertEquals("searchkey=Martial&page=1", request.body)
    }

    @Test
    fun testPostUrlWithOptionsJsonBody() {
        val template = """
            https://www.example.com/api/search,{"method": "POST", "body": {"title": "{{key}}", "page": {{page}}}}
        """.trimIndent()

        val request = UrlParser.parseRequest(template, baseUrl, key = "Sword", page = 1)

        assertEquals("POST", request.method)
        assertEquals("https://www.example.com/api/search", request.url)
        assertNotNull(request.body)
        assertTrue(request.body!!.contains(""""title": "Sword"""") || request.body!!.contains(""""title":"Sword""""))
    }

    @Test
    fun testUrlWithHeadersOption() {
        val template = """
            https://www.example.com/page,{"headers": {"User-Agent": "Mobile", "X-Custom": "Value"}}
        """.trimIndent()

        val request = UrlParser.parseRequest(template, baseUrl)

        assertEquals("GET", request.method)
        assertEquals("https://www.example.com/page", request.url)
        assertEquals("Mobile", request.headers["User-Agent"])
        assertEquals("Value", request.headers["X-Custom"])
    }

    @Test
    fun testUrlWithPipeCharset() {
        val template = "https://www.example.com/search?key={{key}}|char=gbk"
        val request = UrlParser.parseRequest(template, baseUrl, key = "test")

        assertEquals("https://www.example.com/search?key=test", request.url)
        assertEquals("gbk", request.charset)
    }
}
