package ireader.domain.usersource

import io.ktor.client.HttpClient
import ireader.domain.usersource.engine.UserSourceEngine
import ireader.domain.usersource.model.UserSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserSourceEngineTest {

    @Test
    fun testExploreCategoriesParsingInListings() {
        val multiLineExplore = """
            玄幻魔法::https://www.fdxrz.com/fenlei/1_{{page}}.html
            武侠修真::https://www.fdxrz.com/fenlei/2_{{page}}.html
            都市言情::https://www.fdxrz.com/fenlei/3_{{page}}.html
        """.trimIndent()

        val source = UserSource(
            sourceUrl = "https://www.fdxrz.com",
            sourceName = "金银小说网",
            exploreUrl = multiLineExplore
        )

        val client = HttpClient()
        val engine = UserSourceEngine(source, client)
        val listings = engine.getListings()

        // 2 standard listings (Popular, Latest) + 3 category listings
        assertEquals(5, listings.size)
        assertEquals("Popular", listings[0].name)
        assertEquals("Latest", listings[1].name)
        assertEquals("玄幻魔法", listings[2].name)
        assertEquals("武侠修真", listings[3].name)
        assertEquals("都市言情", listings[4].name)

        val catListing = listings[2] as UserSourceEngine.CategoryListing
        assertEquals("https://www.fdxrz.com/fenlei/1_{{page}}.html", catListing.categoryUrl)
    }

    @Test
    fun testExploreJsonArrayParsingInListings() {
        val jsonExplore = """
            [
              {"title": "Action", "url": "https://example.com/action?p={{page}}"},
              {"title": "Comedy", "url": "https://example.com/comedy?p={{page}}"}
            ]
        """.trimIndent()

        val source = UserSource(
            sourceUrl = "https://example.com",
            sourceName = "Test Source",
            exploreUrl = jsonExplore
        )

        val client = HttpClient()
        val engine = UserSourceEngine(source, client)
        val listings = engine.getListings()

        assertEquals(4, listings.size)
        assertEquals("Action", listings[2].name)
        assertEquals("Comedy", listings[3].name)
    }
}
