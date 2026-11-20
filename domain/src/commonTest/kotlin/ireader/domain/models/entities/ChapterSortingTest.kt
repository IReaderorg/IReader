package ireader.domain.models.entities

import kotlin.test.Test
import kotlin.test.assertEquals

class ChapterSortingTest {

    private fun chapters(vararg names: String): List<Chapter> =
        names.mapIndexed { index, name ->
            Chapter(id = index.toLong() + 1, bookId = 1L, key = name, name = name)
        }

    @Test
    fun `name sort orders numeric parts numerically`() {
        val sorted = chapters("c20", "c1", "c11", "c2", "c12", "c10", "c3")
            .sortedWith(ChapterSorting.ByName)

        assertEquals(
            listOf("c1", "c2", "c3", "c10", "c11", "c12", "c20"),
            sorted.map { it.name },
        )
    }

    @Test
    fun `name sort handles prefixes and multiple numeric parts`() {
        val sorted = chapters(
            "Chapter 2 Extra",
            "Chapter 10",
            "Chapter 2",
            "Chapter 1",
        ).sortedWith(ChapterSorting.ByName)

        assertEquals(
            listOf("Chapter 1", "Chapter 2", "Chapter 2 Extra", "Chapter 10"),
            sorted.map { it.name },
        )
    }

    @Test
    fun `chapter number sort falls back to number embedded in the name`() {
        val sorted = chapters("c20", "c1", "c11", "c2", "c10")
            .sortedWith(ChapterSorting.ByNumber)

        assertEquals(
            listOf("c1", "c2", "c10", "c11", "c20"),
            sorted.map { it.name },
        )
    }

    @Test
    fun `explicit chapter number takes precedence over the name`() {
        val chapters = listOf(
            Chapter(id = 1, bookId = 1L, key = "a", name = "c1", number = 20f),
            Chapter(id = 2, bookId = 1L, key = "b", name = "c20", number = 1f),
        )

        assertEquals(listOf("c20", "c1"), chapters.sortedWith(ChapterSorting.ByNumber).map { it.name })
    }

    @Test
    fun `legacy chapters all tied at zero need source order normalization`() {
        val chapters = chapters("c1", "c2", "c3")

        assertEquals(true, chapters.hasStaleSourceOrder())
    }

    @Test
    fun `unique source orders do not need normalization`() {
        val chapters = chapters("c1", "c2", "c3")
            .mapIndexed { index, chapter -> chapter.copy(sourceOrder = index.toLong()) }

        assertEquals(false, chapters.hasStaleSourceOrder())
    }

    @Test
    fun `source sort follows the order reported by the source`() {
        val chapters = listOf(
            Chapter(id = 1, bookId = 1L, key = "a", name = "Chapter 20", sourceOrder = 2L, number = 20f),
            Chapter(id = 2, bookId = 1L, key = "b", name = "Chapter 1", sourceOrder = 0L, number = 1f),
            Chapter(id = 3, bookId = 1L, key = "c", name = "Chapter 10", sourceOrder = 1L, number = 10f),
        )

        assertEquals(
            listOf("Chapter 1", "Chapter 10", "Chapter 20"),
            chapters.sortedWith(ChapterSorting.BySource).map { it.name },
        )
    }
}
