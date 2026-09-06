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

    @Test
    fun testSliceIndexSelectors() {
        val html = """
            <table class="mytable">
              <tbody>
                <tr><th>Header 1</th><th>Header 2</th></tr>
                <tr><td><a href="/book/1">Novel One</a></td><td>Author 1</td></tr>
                <tr><td><a href="/book/2">Novel Two</a></td><td>Author 2</td></tr>
                <tr><td><a href="/book/3">Novel Three</a></td><td>Author 3</td></tr>
              </tbody>
            </table>
        """.trimIndent()
        val doc = Ksoup.parse(html)

        // tr[1:] should skip header row (index 0) and return the 3 novel rows
        val rows = RuleParser.getElements(doc, "class.mytable@tag.tbody@tag.tr[1:]")
        assertEquals(3, rows.size)

        // tr[0] should return the header row
        val header = RuleParser.getElements(doc, "class.mytable@tag.tbody@tag.tr[0]")
        assertEquals(1, header.size)
        assertTrue(header[0].text().contains("Header"))

        // tr[-1] should return the last row (Novel Three)
        val last = RuleParser.getElements(doc, "class.mytable@tag.tbody@tag.tr[-1]")
        assertEquals(1, last.size)
        assertTrue(last[0].text().contains("Novel Three"))

        // tr[:2] should return first 2 rows (header + Novel One)
        val firstTwo = RuleParser.getElements(doc, "class.mytable@tag.tbody@tag.tr[:2]")
        assertEquals(2, firstTwo.size)
    }

    @Test
    fun testJsRulesEvaluation() {
        val searchRowHtml = """
            <div class="col-md-10">
              <h4 class="bookTitle"><a href="/jinyin/12345.html">My Great Novel</a></h4>
              <p class="booktag"><a>Famous Author</a></p>
              <p id="bookIntro">This is a great novel intro.</p>
              <a class="text-danger">Chapter 500</a>
            </div>
        """.trimIndent()
        val row = Ksoup.parse(searchRowHtml).select(".col-md-10").first()!!

        // Test title extraction from @js:
        val nameRule = "@js:(function(){try{return String(result.select('h4.bookTitle a').first().text());}catch(e){return 'ERR1:'+e;}})()"
        assertEquals("My Great Novel", RuleParser.getString(row, nameRule))

        // Test URL extraction from @js:
        val urlRule = "@js:(function(){try{return String(result.select('h4.bookTitle a').first().attr('href'));}catch(e){return 'ERR5:'+e;}})()"
        assertEquals("/jinyin/12345.html", RuleParser.getString(row, urlRule))

        // Test author extraction from @js:
        val authorRule = "@js:(function(){try{return String(result.select('p.booktag a').first().text());}catch(e){return 'ERR2:'+e;}})()"
        assertEquals("Famous Author", RuleParser.getString(row, authorRule))

        // Test chained table cell selection from @js:
        val tableRowHtml = """
            <table>
              <tbody>
                <tr>
                  <td><a href="/jinyin/6789.html">Explore Novel</a></td>
                  <td><a>Chapter 99</a></td>
                  <td>Explore Author</td>
                  <td>Word Count</td>
                  <td>Urban Category</td>
                </tr>
              </tbody>
            </table>
        """.trimIndent()
        val tableRow = Ksoup.parse(tableRowHtml).select("tr").first()!!

        val expNameRule = "@js:(function(){try{return String(result.select('td').get(0).select('a').first().text());}catch(e){return '';}})()"
        assertEquals("Explore Novel", RuleParser.getString(tableRow, expNameRule))

        val expAuthorRule = "@js:(function(){try{return String(result.select('td').get(2).text()).trim();}catch(e){return '';}})()"
        assertEquals("Explore Author", RuleParser.getString(tableRow, expAuthorRule))

        val expKindRule = "@js:(function(){try{return String(result.select('td').get(4).text()).trim();}catch(e){return '';}})()"
        assertEquals("Urban Category", RuleParser.getString(tableRow, expKindRule))

        // Test regex match in @js:
        val metaHtml = "<div>字数：150万字 状态：连载中</div>"
        val metaDoc = Ksoup.parse(metaHtml)
        val wordCountRule = "@js:(function(){var m=String(result).match(/字数：([^<\\s]+)/);return m?m[1]:'';})()"
        assertEquals("150万字", RuleParser.getString(metaDoc, wordCountRule))

        // Test content rule with ad filtering in @js:
        val contentHtml = """
            <div id="htmlContent">
              <p>First novel paragraph.</p>
              <p>提醒您最新章节修复完成，请记住本站域名</p>
              <p>Second novel paragraph with story.</p>
              <p>金银小说网全网首发无弹窗</p>
              <p>Third novel paragraph ending the chapter.</p>
            </div>
        """.trimIndent()
        val contentDoc = Ksoup.parse(contentHtml)
        val contentRule = """
            @js:(function(){
                var html=(typeof result==='string')?result:String(result.html());
                var doc=Packages.org.jsoup.Jsoup.parse(html);
                var ps=doc.select('#htmlContent p');
                if(ps.size()==0){ps=doc.select('.panel-readcontent p');}
                var out=[];
                for(var i=0;i<ps.size();i++){
                    var t=String(ps.get(i).text()).trim();
                    if(!t)continue;
                    if(t.indexOf('提醒您')>-1&&(t.indexOf('最新章节')>-1||t.indexOf('金银')>-1))continue;
                    if(t.indexOf('金银小说')>-1)continue;
                    out.push(t);
                }
                return out.join('\n');
            })()
        """.trimIndent()
        val cleanContent = RuleParser.getString(contentDoc, contentRule)
        assertTrue(cleanContent.contains("First novel paragraph."))
        assertTrue(cleanContent.contains("Second novel paragraph with story."))
        assertTrue(cleanContent.contains("Third novel paragraph ending the chapter."))
        assertTrue(!cleanContent.contains("提醒您"))
        assertTrue(!cleanContent.contains("金银小说网"))

        // Test sibling cover extraction in @js:
        val searchItemHtml = """
            <div class="row">
              <div class="col-md-2"><img src="/covers/novel.jpg"></div>
              <div class="col-md-10">
                <h4 class="bookTitle"><a href="/jinyin/12345.html">My Great Novel</a></h4>
              </div>
            </div>
        """.trimIndent()
        val col10 = Ksoup.parse(searchItemHtml).select(".col-md-10").first()!!
        val coverRule = "@js:(function(){try{var p=result.parent();var i=result.elementSiblingIndex();var img=p.children().get(i-1).select('img').first().attr('src');return img;}catch(e){return '';}})()"
        assertEquals("/covers/novel.jpg", RuleParser.getString(col10, coverRule))
    }
}
