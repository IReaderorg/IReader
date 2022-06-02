package org.ireader.data

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.ireader.data.local.AppDatabase
import org.ireader.data.local.AppDatabase_Migrations
import org.ireader.data.local.MIGRATION_20_21
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
        MIGRATION_20_21
    )

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate20to21() {
        database = helper.createDatabase(TEST_DB, 20).apply {

            // (id,sourceId,link,title,author,description,genres,status,cover,customCover,favorite,lastUpdated,lastRead,dataAdded,viewer,flags)
            execSQL(
                """
                INSERT INTO library(`id`,`sourceId`,`title`,`key`,`tableId`,`type`,`author`,`description`,`genres`,`status`,`cover`,`customCover`,`favorite`,`lastUpdate`,`lastInit`,`dataAdded`,`viewer`,`flags`) VALUES(10,10,"test","test.com",0,0,"author","desc","",0,"cover","customCOver",0,0,0,0,0,0)
                """.trimIndent()
            )

            close()
        }

        // ADDED a tableId
        database = helper.runMigrationsAndValidate(TEST_DB, 21, true, MIGRATION_20_21)
        val resultCursor = database.query("SELECT * FROM library")
        assertTrue(resultCursor.moveToFirst())

        print("db result is  $resultCursor")

        val tableIdIndex = resultCursor.getColumnIndex("title")

        val title = resultCursor.getString(tableIdIndex)
        assertThat(title).isEqualTo("test")
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
        ).addMigrations(*AppDatabase_Migrations.build()).build().apply {
            openHelper.writableDatabase.close()
        }
    }
}
