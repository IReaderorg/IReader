package org.ireader.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.matrix.roomigrant.GenerateRoomMigrations
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.BookCategory
import org.ireader.common_models.entities.CatalogRemote
import org.ireader.common_models.entities.Category
import org.ireader.common_models.entities.Chapter
import org.ireader.common_models.entities.Download
import org.ireader.common_models.entities.History
import org.ireader.common_models.entities.RemoteKeys
import org.ireader.common_models.entities.Update
import org.ireader.data.local.dao.BookCategoryDao
import org.ireader.data.local.dao.CatalogDao
import org.ireader.data.local.dao.CategoryDao
import org.ireader.data.local.dao.ChapterDao
import org.ireader.data.local.dao.DownloadDao
import org.ireader.data.local.dao.HistoryDao
import org.ireader.data.local.dao.LibraryBookDao
import org.ireader.data.local.dao.LibraryDao
import org.ireader.data.local.dao.RemoteKeysDao
import org.ireader.data.local.dao.UpdatesDao
import java.util.concurrent.Executors

@Database(
    entities = [
        Book::class,
        CatalogRemote::class,
        Category::class,
        Chapter::class,
        Download::class,
        History::class,
        Update::class,
        RemoteKeys::class,
        BookCategory::class
    ],
    version = 23,
    exportSchema = true,
)
@TypeConverters(DatabaseConverter::class)
@GenerateRoomMigrations
abstract class AppDatabase : RoomDatabase() {
    abstract val libraryBookDao: LibraryBookDao
    abstract val chapterDao: ChapterDao
    abstract val remoteKeysDao: RemoteKeysDao
    abstract val downloadDao: DownloadDao
    abstract val catalogDao: CatalogDao
    abstract val historyDao: HistoryDao
    abstract val updatesDao: UpdatesDao
    abstract val libraryDao: LibraryDao
    abstract val categoryDao: CategoryDao
    abstract val bookCategoryDao: BookCategoryDao

    companion object {
        const val DATABASE_NAME = "infinity_db"
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(MIGRATION_20_21(), MIGRATION_21_22(), MIGRATION_22_23())
                // prepopulate the database after onCreate was called
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Executors.newSingleThreadExecutor().execute {
                            db.execSQL("""
                                INSERT OR IGNORE INTO category VALUES (-1, "", 0, 0, 0);
                            """.trimIndent())
                            db.execSQL("""
                                INSERT OR IGNORE INTO category VALUES (-2, "", 0, 0, 0);
                            """.trimIndent())
                            db.execSQL("""
                                CREATE TRIGGER IF NOT EXISTS system_categories_deletion_trigger BEFORE DELETE ON category
                                BEGIN SELECT CASE
                                  WHEN old.id <= 0 THEN
                                    RAISE(ABORT, 'System category cant be deleted')
                                  END;
                                END
                            """.trimIndent())
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
    }
}

internal fun MIGRATION_22_23() = object : Migration(22, 23) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""DROP TABLE IF EXISTS `fonts`""")
    }
}


internal fun MIGRATION_21_22() = object : Migration(21, 22) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""CREATE TABLE IF NOT EXISTS `library_MERGE_TABLE` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sourceId` INTEGER NOT NULL, `title` TEXT NOT NULL, `key` TEXT NOT NULL, `tableId` INTEGER NOT NULL, `type` INTEGER NOT NULL, `author` TEXT NOT NULL, `description` TEXT NOT NULL, `genres` TEXT NOT NULL, `status` INTEGER NOT NULL, `cover` TEXT NOT NULL, `customCover` TEXT NOT NULL, `favorite` INTEGER NOT NULL, `lastUpdate` INTEGER NOT NULL, `lastInit` INTEGER NOT NULL, `dateAdded` INTEGER NOT NULL, `viewer` INTEGER NOT NULL, `flags` INTEGER NOT NULL)""")
        database.execSQL("""INSERT INTO `library_MERGE_TABLE` (`id`,`sourceId`,`title`,`key`,`tableId`,`type`,`author`,`description`,`genres`,`status`,`cover`,`customCover`,`favorite`,`lastUpdate`,`lastInit`,`viewer`,`flags`,`dateAdded`) SELECT `library`.`id`,`library`.`sourceId`,`library`.`title`,`library`.`key`,`library`.`tableId`,`library`.`type`,`library`.`author`,`library`.`description`,`library`.`genres`,`library`.`status`,`library`.`cover`,`library`.`customCover`,`library`.`favorite`,`library`.`lastUpdate`,`library`.`lastInit`,`library`.`viewer`,`library`.`flags`,`library`.`dataAdded` FROM `library`""")
        database.execSQL("""DROP TABLE IF EXISTS `library`""")
        database.execSQL("""ALTER TABLE `library_MERGE_TABLE` RENAME TO `library`""")
        database.execSQL("""CREATE TABLE IF NOT EXISTS `category_MERGE_TABLE` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `order` INTEGER NOT NULL, `updateInterval` INTEGER NOT NULL, `flags` INTEGER NOT NULL)""")
        database.execSQL("""INSERT INTO `category_MERGE_TABLE` (`id`,`name`,`updateInterval`,`flags`,`order`) SELECT `category`.`id`,`category`.`name`,`category`.`updateInterval`,`category`.`flags`,`category`.`sort` FROM `category`""")
        database.execSQL("""DROP TABLE IF EXISTS `category`""")
        database.execSQL("""ALTER TABLE `category_MERGE_TABLE` RENAME TO `category`""")

    }
}



internal fun MIGRATION_20_21() = object : Migration(20, 21) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""CREATE TABLE IF NOT EXISTS `history_MERGE_TABLE` (`bookId` INTEGER NOT NULL, `chapterId` INTEGER NOT NULL, `readAt` INTEGER NOT NULL, `progress` INTEGER NOT NULL, PRIMARY KEY(`bookId`, `chapterId`), FOREIGN KEY(`bookId`) REFERENCES `library`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE  )""")
        database.execSQL("""INSERT INTO `history_MERGE_TABLE` (`bookId`,`chapterId`,`readAt`, `progress`) SELECT `bookId`,`chapterId`,`readAt`, `progress` FROM `history`""")
        database.execSQL("""DROP TABLE IF EXISTS `history`""")
        database.execSQL("""ALTER TABLE `history_MERGE_TABLE` RENAME TO `history`""")
    }
}
