package ireader.domain.usecases.epub

import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlContentCleanerTest {

    @Test
    fun `clean keeps paragraph breaks so exported chapters are not merged into one paragraph`() {
        val content = "First <Skill: Fireball>\n\n<img src=\"../Images/a.png\" alt=\"Image\"/>\n\nSecond   paragraph"
        assertEquals(
            "First <Skill: Fireball>\n\n<img src=\"../Images/a.png\" alt=\"Image\"/>\n\nSecond paragraph",
            HtmlContentCleaner.clean(content)
        )
    }

    @Test
    fun `clean removes ad blocks but keeps divs whose class only contains the letters ad`() {
        val html = "<div class=\"chapter-reader\">Story</div><div class=\"ad-banner\">Buy</div><div id=\"ads_top\">Buy</div>"
        assertEquals("<div class=\"chapter-reader\">Story</div>", HtmlContentCleaner.clean(html))
    }
}
