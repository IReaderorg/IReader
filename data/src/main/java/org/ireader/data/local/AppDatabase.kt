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
import org.ireader.common_models.entities.FontEntity
import org.ireader.common_models.entities.History
import org.ireader.common_models.entities.RemoteKeys
import org.ireader.common_models.entities.Update
import org.ireader.data.local.dao.BookCategoryDao
import org.ireader.data.local.dao.CatalogDao
import org.ireader.data.local.dao.CategoryDao
import org.ireader.data.local.dao.ChapterDao
import org.ireader.data.local.dao.DownloadDao
import org.ireader.data.local.dao.FontDao
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
        FontEntity::class,
        BookCategory::class
    ],
    version = 20,
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
    abstract val fontDao: FontDao

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
                // prepopulate the database after onCreate was called
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // insert the data on the IO Thread
                        Executors.newSingleThreadExecutor().execute {
                            getInstance(context).categoryDao.insertDate(systemCategories)
                        }
                    }
                })
                .addMigrations(*AppDatabase_Migrations.build())
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Executors.newSingleThreadExecutor().execute {

                        }
                    }
                })
                .build()

        val systemCategories = listOf<Category>(
            Category(id = Category.ALL_ID, "", Category.ALL_ID.toInt(), 0, 0),
            Category(id = Category.UNCATEGORIZED_ID, "", Category.UNCATEGORIZED_ID.toInt(), 0, 0),
        )
    }
}

val MIGRATION_19_20 = object : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
                    CREATE TABLE chapter_new (
            id INTEGER NOT NULL PRIMARY KEY,
    bookId INTEGER NOT NULL,
    `key` TEXT NOT NULL,
    name TEXT NOT NULL,
    translator TEXT,
    read INTEGER NOT NULL,
    bookmark INTEGER NOT NULL,
    number REAL NOT NULL,
    sourceOrder INTEGER NOT NULL,
    content TEXT NOT NULL,
    dateFetch INTEGER NOT NULL,
    dateUpload INTEGER NOT NULL);
        """.trimIndent()
        )

        database.execSQL(
            """
             INSERT INTO chapter_new (id, 
        bookId,name,read,bookmark,dateUpload,dateFetch,sourceOrder,content,number,translator) SELECT id, 
        bookId,name,read,bookmark,dateUpload,dateFetch,sourceOrder,content,number,translator FROM chapter
        """.trimIndent()
        )

        database.execSQL(
            """
               DROP TABLE chapter
        """.trimIndent()
        )
        database.execSQL(
            """
    ALTER TABLE chapter_new RENAME TO chapter
        """.trimIndent()
        )

        database.execSQL(
            """
            CREATE TABLE history_new (
            bookId INTEGER NOT NULL PRIMARY KEY,
            chapterId INTEGER NOT NULL PRIMARY KEY,
            readAt INTEGER NOT NULL,
            progress INTEGER NOT NULL);
        """.trimIndent()
        )
        database.execSQL(
            """
            INSERT INTO history_new (bookId,chapterId,readAt) SELECT bookId,chapterId,readAt FROM history
        """.trimIndent()
        )
        database.execSQL(
            """
               DROP TABLE history
        """.trimIndent()
        )
        database.execSQL(
            """
    ALTER TABLE history_new RENAME TO history
        """.trimIndent()
        )
    }
}
