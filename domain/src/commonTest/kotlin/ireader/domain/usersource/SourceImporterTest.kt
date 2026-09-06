package ireader.domain.usersource

import ireader.domain.usersource.importer.SourceImporter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SourceImporterTest {

    private val importer = SourceImporter()

    @Test
    fun testImportStandardJsonArray() {
        val json = """
            [
              {
                "bookSourceName": "Test Source 1",
                "bookSourceUrl": "https://example1.com",
                "enabled": true,
                "ruleSearch": {
                  "bookList": ".book-item",
                  "name": ".title"
                }
              },
              {
                "bookSourceName": "Test Source 2",
                "bookSourceUrl": "https://example2.com",
                "enabled": false
              }
            ]
        """.trimIndent()

        val result = importer.importFromJson(json)
        assertTrue(result is SourceImporter.ImportResult.Success, "Expected success but was $result")
        assertEquals(2, result.sources.size)
        assertEquals("Test Source 1", result.sources[0].sourceName)
        assertEquals("https://example1.com", result.sources[0].sourceUrl)
        assertEquals(".book-item", result.sources[0].ruleSearch.bookList)
        assertEquals(true, result.sources[0].enabled)
        assertEquals(false, result.sources[1].enabled)
    }

    @Test
    fun testImportSingleSourceJsonObject() {
        val json = """
            {
              "bookSourceName": "Single Source",
              "bookSourceUrl": "https://single.com",
              "bookSourceGroup": "Chinese",
              "enabled": true,
              "ruleSearch": {
                "name": "h1.title"
              }
            }
        """.trimIndent()

        val result = importer.importFromJson(json)
        assertTrue(result is SourceImporter.ImportResult.Success, "Expected success but was $result")
        assertEquals(1, result.sources.size)
        assertEquals("Single Source", result.sources[0].sourceName)
        assertEquals("h1.title", result.sources[0].ruleSearch.name)
    }

    @Test
    fun testImportWrappedSourcesObject() {
        val json = """
            {
              "title": "Community Sources",
              "description": "Exported sources",
              "sources": [
                {
                  "bookSourceName": "Wrapped 1",
                  "bookSourceUrl": "https://wrapped1.com"
                },
                {
                  "bookSourceName": "Wrapped 2",
                  "bookSourceUrl": "https://wrapped2.com"
                }
              ]
            }
        """.trimIndent()

        val result = importer.importFromJson(json)
        assertTrue(result is SourceImporter.ImportResult.Success, "Expected success for wrapped sources but was $result")
        assertEquals(2, result.sources.size)
        assertEquals("Wrapped 1", result.sources[0].sourceName)
        assertEquals("Wrapped 2", result.sources[1].sourceName)
    }

    @Test
    fun testImportStringifiedRules() {
        // Many Legado sources serialize rule objects as escaped JSON strings
        val json = """
            [
              {
                "bookSourceName": "String Rules Source",
                "bookSourceUrl": "https://stringrules.com",
                "ruleSearch": "{\"bookList\": \".bookbox\", \"name\": \"class.bookname\", \"author\": \"class.author\"}",
                "ruleBookInfo": "{\"intro\": \"id.intro\"}",
                "ruleToc": "{\"chapterList\": \"-tag.dd\", \"isReverse\": true}",
                "ruleContent": "{\"content\": \"id.content\"}"
              }
            ]
        """.trimIndent()

        val result = importer.importFromJson(json)
        assertTrue(result is SourceImporter.ImportResult.Success, "Expected success with stringified rules but was $result")
        assertEquals(1, result.sources.size)
        val source = result.sources[0]
        assertEquals(".bookbox", source.ruleSearch.bookList)
        assertEquals("class.bookname", source.ruleSearch.name)
        assertEquals("id.intro", source.ruleBookInfo.intro)
        assertEquals("-tag.dd", source.ruleToc.chapterList)
        assertTrue(source.ruleToc.isReverse)
        assertEquals("id.content", source.ruleContent.content)
    }

    @Test
    fun testImportIntegerAndStringEnabled() {
        val json = """
            [
              {
                "bookSourceName": "Int Enabled",
                "bookSourceUrl": "https://int.com",
                "enabled": 1
              },
              {
                "bookSourceName": "Int Disabled",
                "bookSourceUrl": "https://intdisabled.com",
                "enabled": 0
              },
              {
                "bookSourceName": "String Enabled",
                "bookSourceUrl": "https://stringenabled.com",
                "enabled": "true"
              }
            ]
        """.trimIndent()

        val result = importer.importFromJson(json)
        assertTrue(result is SourceImporter.ImportResult.Success, "Expected success with non-boolean enabled but was $result")
        assertEquals(3, result.sources.size)
        assertEquals(true, result.sources[0].enabled)
        assertEquals(false, result.sources[1].enabled)
        assertEquals(true, result.sources[2].enabled)
    }

    @Test
    fun testParseImportUrlCdnSupport() {
        val blobUrl = "https://github.com/XIU2/Yuedu/blob/master/shuyuan"
        val parsed = importer.parseImportUrl(blobUrl)
        assertNotNull(parsed)
        assertTrue(parsed.contains("raw.githubusercontent.com") || parsed.contains("jsdelivr.net"))
    }

    @Test
    fun testParseImportUrlYioveAndYueduProtocol() {
        // Direct Yiove website URL
        val yioveUrl = "https://shuyuan.yiove.com/book-source/81b68e2f-b84b-4cba-b667-19899fae1a2d"
        val resolved = importer.parseImportUrl(yioveUrl)
        assertEquals("https://shuyuan-api.yiove.com/import/book-source/81b68e2f-b84b-4cba-b667-19899fae1a2d", resolved)

        // yuedu:// scheme with encoded src
        val yueduUrl = "yuedu://booksource/importonline?src=https%3A%2F%2Fshuyuan-api.yiove.com%2Fimport%2Fbook-source%2F81b68e2f-b84b-4cba-b667-19899fae1a2d"
        val resolvedYuedu = importer.parseImportUrl(yueduUrl)
        assertEquals("https://shuyuan-api.yiove.com/import/book-source/81b68e2f-b84b-4cba-b667-19899fae1a2d", resolvedYuedu)

        // yuedu:// scheme wrapping frontend web URL
        val yueduWebUrl = "yuedu://booksource/importonline?src=https%3A%2F%2Fshuyuan.yiove.com%2Fbook-source%2F81b68e2f-b84b-4cba-b667-19899fae1a2d"
        val resolvedYueduWeb = importer.parseImportUrl(yueduWebUrl)
        assertEquals("https://shuyuan-api.yiove.com/import/book-source/81b68e2f-b84b-4cba-b667-19899fae1a2d", resolvedYueduWeb)
    }
}
