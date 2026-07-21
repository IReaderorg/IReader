package ireader.data.category

import ireader.domain.models.entities.Category
import kotlin.test.Test
import kotlin.test.assertEquals

class CategoryMapperTest {

    @Test
    fun categoryMapperShouldMapAllFields() {
        // categoryMapper: (Long, String, Long, Long) -> Category
        val category = categoryMapper(
            1L,          // id
            "Favorites", // name
            5L,          // order
            0L           // flags
        )

        assertEquals(1L, category.id)
        assertEquals("Favorites", category.name)
        assertEquals(5L, category.order)
        assertEquals(0L, category.flags)
    }

    @Test
    fun categoryMapperShouldHandleEmptyName() {
        val category = categoryMapper(1L, "", 0L, 0L)

        assertEquals(1L, category.id)
        assertEquals("", category.name)
        assertEquals(0L, category.order)
    }

    @Test
    fun categoryMapperShouldHandleLargeValues() {
        val category = categoryMapper(
            Long.MAX_VALUE, "Very Long Category Name",
            Long.MAX_VALUE, Long.MAX_VALUE
        )

        assertEquals(Long.MAX_VALUE, category.id)
        assertEquals("Very Long Category Name", category.name)
        assertEquals(Long.MAX_VALUE, category.order)
        assertEquals(Long.MAX_VALUE, category.flags)
    }

    @Test
    fun categoryMapperShouldHandleSpecialCharacters() {
        val category = categoryMapper(
            1L, "Favorites & Reading 📚", 1L, 0L
        )

        assertEquals("Favorites & Reading 📚", category.name)
    }
}
