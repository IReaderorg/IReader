package ireader.domain.epub

import ireader.core.source.model.ImageUrl
import ireader.core.source.model.Text
import kotlin.test.Test
import kotlin.test.assertEquals

class EpubBuilderFormattingTest {

    @Test
    fun `first page repeating the chapter title is dropped`() {
        val pages = listOf(Text("Chapter 12 - The Fight"), Text("Kai ran."), Text("Chapter 12 - The Fight"))
        assertEquals(
            listOf(Text("Kai ran."), Text("Chapter 12 - The Fight")),
            EpubBuilder.withoutRepeatedTitle(pages, "Chapter 12: The Fight")
        )
    }

    @Test
    fun `first page that is not the title is kept`() {
        val pages = listOf(ImageUrl("https://example.com/a.png"), Text("Chapter 12 begins with rain."), Text("Kai ran."))
        assertEquals(pages, EpubBuilder.withoutRepeatedTitle(pages, "Chapter 12"))
    }

    @Test
    fun `blank lines separate paragraphs and single newlines become line breaks`() {
        val content = "First paragraph.\n\nRoses are red,\nviolets are blue.\n\n<System: Level up>"
        assertEquals(
            listOf(
                "    <p>First paragraph.</p>",
                "    <p>Roses are red,<br/>violets are blue.</p>",
                "    <p>&lt;System: Level up&gt;</p>",
            ).joinToString("\n"),
            EpubBuilder.paragraphsToXhtml(content)
        )
    }

    @Test
    fun `html paragraph and image lines are still emitted one per line`() {
        val content = "<p>One</p>\n<p>Two</p>\n\n<img src=\"../Images/a.png\" alt=\"Image\">"
        assertEquals(
            listOf("    <p>One</p>", "    <p>Two</p>", "    <img src=\"../Images/a.png\" alt=\"Image\"/>").joinToString("\n"),
            EpubBuilder.paragraphsToXhtml(content)
        )
    }
}
