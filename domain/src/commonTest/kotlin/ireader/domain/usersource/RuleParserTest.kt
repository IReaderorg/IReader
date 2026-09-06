package ireader.domain.usersource

import com.fleeksoft.ksoup.Ksoup
import ireader.domain.usersource.parser.RuleParser
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RuleParserTest {

    private val sampleHtml = """
        <html>
        <body>
            <div id="header">
                <h1 class="title">My Novel Title</h1>
                <span class="author">Author Name</span>
            </div>
            <div class="book-list">
                <div class="item">
                    <a href="/book/101" class="name">Book One</a>
                    <span class="latest">Chapter 50</span>
                </div>
                <div class="item">
                    <a href="/book/102" class="name">Book Two</a>
                    <span class="latest">Chapter 100</span>
                </div>
            </div>
            <div id="content">
                Line 1
                <p>Paragraph 1</p>
                Line 2
                <p>Paragraph 2</p>
            </div>
            <div class="meta">
                <p>Word count: 50000</p>
                <p>Status: Completed</p>
            </div>
        </body>
        </html>
    """.trimIndent()

    @Test
    fun testLegadoPseudoSelectors() {
        val doc = Ksoup.parse(sampleHtml)

        assertEquals("My Novel Title", RuleParser.getString(doc, "class.title"))
        assertEquals("Author Name", RuleParser.getString(doc, "span.author"))
        assertEquals("My Novel Title", RuleParser.getString(doc, "tag.h1"))
        assertEquals("My Novel Title", RuleParser.getString(doc, "id.header@class.title"))
    }

    @Test
    fun testChainedAtSelectors() {
        val doc = Ksoup.parse(sampleHtml)

        // Multi-level @ selector extracting attribute
        val href = RuleParser.getString(doc, "class.book-list@class.item.0@tag.a@href")
        assertEquals("/book/101", href)

        // Extracting text from last item
        val lastBook = RuleParser.getString(doc, "class.book-list@class.item.-1@class.name@text")
        assertEquals("Book Two", lastBook)
    }

    @Test
    fun testTextNodesExtraction() {
        val doc = Ksoup.parse(sampleHtml)
        val textNodes = RuleParser.getString(doc, "id.content@textNodes")
        assertTrue(textNodes.contains("Line 1"))
        assertTrue(textNodes.contains("Line 2"))
    }

    @Test
    fun testFallbackRules() {
        val doc = Ksoup.parse(sampleHtml)
        val result = RuleParser.getString(doc, "class.nonexistent||class.title")
        assertEquals("My Novel Title", result)
    }

    @Test
    fun testRegexRule() {
        val doc = Ksoup.parse(sampleHtml)
        val result = RuleParser.getString(doc, "class.title##^My ##")
        assertEquals("Novel Title", result)
    }

    @Test
    fun testElementSelection() {
        val doc = Ksoup.parse(sampleHtml)
        val items = RuleParser.getElements(doc, "class.book-list@class.item")
        assertEquals(2, items.size)

        // Check reverse selection with '-' prefix
        val reversed = RuleParser.getElements(doc, "-class.book-list@class.item")
        assertEquals(2, reversed.size)
        assertEquals("/book/102", RuleParser.getString(reversed[0], "tag.a@href"))
    }

    @Test
    fun testJsonPathExtraction() {
        val jsonString = """
            {
              "code": 0,
              "data": {
                "total": 2,
                "list": [
                  {
                    "bookId": "1001",
                    "title": "First Web Novel",
                    "author": "Author A",
                    "cover": "https://img.com/1.jpg"
                  },
                  {
                    "bookId": "1002",
                    "title": "Second Web Novel",
                    "author": "Author B",
                    "cover": "https://img.com/2.jpg"
                  }
                ]
              }
            }
        """.trimIndent()

        val json = Json { ignoreUnknownKeys = true }
        val root = json.parseToJsonElement(jsonString)

        val list = RuleParser.getJsonElements(root, "$.data.list[*]")
        assertEquals(2, list.size)

        val item0 = list[0]
        assertEquals("First Web Novel", RuleParser.getString(item0, "$.title"))
        assertEquals("Author A", RuleParser.getString(item0, "$.author"))
        assertEquals("https://img.com/1.jpg", RuleParser.getString(item0, "$.cover"))
        assertEquals("1001", RuleParser.getString(item0, "$.bookId"))

        // Also test root string extraction directly from json string
        assertEquals("First Web Novel", RuleParser.getStringFromJson(jsonString, "$.data.list[0].title"))
    }
}
