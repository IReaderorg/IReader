package ireader.data

import data.DatabaseMigrations
import ireader.data.category.categoryWithCountMapper
import ireader.data.core.createDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

class BookCategoryDuplicateTest {

    @Test
    fun `inserting the same book category pair twice keeps one row`() {
        val driver = createTestDriver()
        ir.kazemcodes.infinityreader.Database.Schema.create(driver)
        val database = createDatabase(driver)

        database.bookcategoryQueries.insert(1L, 10L)
        database.bookcategoryQueries.insert(1L, 10L)

        assertEquals(1, database.bookcategoryQueries.findAll().executeAsList().size)
    }

    @Test
    fun `restoring a category assignment again does not double its book count`() {
        val driver = createTestDriver()
        ir.kazemcodes.infinityreader.Database.Schema.create(driver)
        val database = createDatabase(driver)

        database.categoryQueries.insert("Fiction", 0L, 0L)
        val categoryId = database.categoryQueries.selectLastInsertedRowId().executeAsOne()

        database.bookcategoryQueries.insert(1L, categoryId)
        database.bookcategoryQueries.insert(1L, categoryId)

        val counts = database.categoryQueries.findAllWithCount(categoryWithCountMapper).executeAsList()
        assertEquals(1, counts.single { it.category.id == categoryId }.bookCount)
    }

    @Test
    fun `migration removes duplicate pairs created by earlier restores`() {
        val driver = createTestDriver()
        ir.kazemcodes.infinityreader.Database.Schema.create(driver)
        // Simulate an older database that was created before the unique index existed.
        driver.execute(null, "DROP INDEX IF EXISTS idx_bookcategories_book_category_unique", 0)
        val database = createDatabase(driver)

        database.bookcategoryQueries.insert(1L, 10L)
        database.bookcategoryQueries.insert(1L, 10L)
        database.bookcategoryQueries.insert(2L, 10L)
        assertEquals(3, database.bookcategoryQueries.findAll().executeAsList().size)

        DatabaseMigrations.migrate(driver, 38)

        assertEquals(2, database.bookcategoryQueries.findAll().executeAsList().size)
        // The unique index must now prevent the duplicate from coming back.
        database.bookcategoryQueries.insert(1L, 10L)
        assertEquals(2, database.bookcategoryQueries.findAll().executeAsList().size)
    }
}
