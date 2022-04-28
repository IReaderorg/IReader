package org.ireader.data

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.ireader.data.local.AppDatabase
import org.ireader.data.local.MIGRATION_10_11
import org.ireader.data.local.MIGRATION_11_12
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private lateinit var database: SupportSQLiteDatabase

    private val TEST_DB = "migration-test"

    // Array of all migrations
    private val ALL_MIGRATIONS = arrayOf(
        MIGRATION_11_12, MIGRATION_10_11
    )

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate11to12() {
        database = helper.createDatabase(TEST_DB, 11).apply {

            // (id,sourceId,link,title,author,description,genres,status,cover,customCover,favorite,lastUpdated,lastRead,dataAdded,viewer,flags)
            execSQL(
                """
                INSERT INTO library  VALUES (0,0,'google.com','test book','myself','this is test','',0,'','',0,0,0,0,0,0)
                """.trimIndent()
            )

            close()
        }

        // ADDED a tableId
        database = helper.runMigrationsAndValidate(TEST_DB, 12, true, MIGRATION_11_12)
        val resultCursor = database.query("SELECT * FROM library")
        assertTrue(resultCursor.moveToFirst())

        val tableIdIndex = resultCursor.getColumnIndex("tableId")

        val tableIdFromDatabase = resultCursor.getLong(tableIdIndex)
        assertThat(tableIdFromDatabase).isEqualTo(0)
    }

    @Test
    fun migrateAll() {
        helper.createDatabase(TEST_DB, 12).apply {
            close()
        }

        // Open latest version of the database. Room will validate the schema
        // once all migrations execute.
        Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java,
            TEST_DB
        ).addMigrations(MIGRATION_11_12, MIGRATION_10_11).build().apply {
            openHelper.writableDatabase.close()
        }
    }
}
