package ireader.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.matrix.roomigrant.GenerateRoomMigrations
import ireader.common.models.entities.Book
import ireader.common.models.entities.BookCategory
import ireader.common.models.entities.CatalogRemote
import ireader.common.models.entities.Category
import ireader.common.models.entities.Chapter
import ireader.common.models.entities.Download
import ireader.common.models.entities.History
import ireader.common.models.entities.Update
import ireader.common.models.theme.CustomTheme
import ireader.common.models.theme.ReaderTheme
import ireader.data.local.dao.BookCategoryDao
import ireader.data.local.dao.CatalogDao
import ireader.data.local.dao.CategoryDao
import ireader.data.local.dao.ChapterDao
import ireader.data.local.dao.DownloadDao
import ireader.data.local.dao.HistoryDao
import ireader.data.local.dao.LibraryBookDao
import ireader.data.local.dao.LibraryDao
import ireader.data.local.dao.ReaderThemeDao
import ireader.data.local.dao.RemoteKeysDao
import ireader.data.local.dao.ThemeDao
import ireader.data.local.dao.UpdatesDao
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
        BookCategory::class,
        CustomTheme::class,
        ReaderTheme::class
    ],
    version = 27,
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
    abstract val themeDao: ThemeDao
    abstract val readerThemeDao: ReaderThemeDao

    companion object {
        private const val DATABASE_NAME = "infinity_db"
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
                .addMigrations(
                    MIGRATION_20_21(),
                    MIGRATION_21_22(),
                    MIGRATION_22_23(),
                    MIGRATION_23_24(),
                    MIGRATION_24_25(),
                    MIGRATION_25_26(),
                    MIGRATION_26_27()
                )
                // prepopulate the database after onCreate was called
                .addCallback(object : Callback() {

                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Executors.newSingleThreadExecutor().execute {
                            db.execSQL(
                                """
                                INSERT OR IGNORE INTO category VALUES (-1, "", 0, 0, 0);
                                """.trimIndent()
                            )
                            db.execSQL(
                                """
                                INSERT OR IGNORE INTO category VALUES (-2, "", 0, 0, 0);
                                """.trimIndent()
                            )
                            db.execSQL(
                                """
                                CREATE TRIGGER IF NOT EXISTS system_categories_deletion_trigger BEFORE DELETE ON category
                                BEGIN SELECT CASE
                                  WHEN old.id <= 0 THEN
                                    RAISE(ABORT, 'System category cant be deleted')
                                  END;
                                END
                                """.trimIndent()
                            )
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
    }
}

internal fun MIGRATION_24_25() = object : Migration(24, 25) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS `theme_table`")
        database.execSQL("CREATE TABLE IF NOT EXISTS `theme_table` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `isDefault` INTEGER NOT NULL, `light-primary` INTEGER NOT NULL, `light-onPrimary` INTEGER NOT NULL, `light-primaryContainer` INTEGER NOT NULL, `light-onPrimaryContainer` INTEGER NOT NULL, `light-inversePrimary` INTEGER NOT NULL, `light-secondary` INTEGER NOT NULL, `light-onSecondary` INTEGER NOT NULL, `light-secondaryContainer` INTEGER NOT NULL, `light-onSecondaryContainer` INTEGER NOT NULL, `light-tertiary` INTEGER NOT NULL, `light-onTertiary` INTEGER NOT NULL, `light-tertiaryContainer` INTEGER NOT NULL, `light-onTertiaryContainer` INTEGER NOT NULL, `light-background` INTEGER NOT NULL, `light-onBackground` INTEGER NOT NULL, `light-surface` INTEGER NOT NULL, `light-onSurface` INTEGER NOT NULL, `light-surfaceVariant` INTEGER NOT NULL, `light-onSurfaceVariant` INTEGER NOT NULL, `light-surfaceTint` INTEGER NOT NULL, `light-inverseSurface` INTEGER NOT NULL, `light-inverseOnSurface` INTEGER NOT NULL, `light-error` INTEGER NOT NULL, `light-onError` INTEGER NOT NULL, `light-errorContainer` INTEGER NOT NULL, `light-onErrorContainer` INTEGER NOT NULL, `light-outline` INTEGER NOT NULL, `dark-primary` INTEGER NOT NULL, `dark-onPrimary` INTEGER NOT NULL, `dark-primaryContainer` INTEGER NOT NULL, `dark-onPrimaryContainer` INTEGER NOT NULL, `dark-inversePrimary` INTEGER NOT NULL, `dark-secondary` INTEGER NOT NULL, `dark-onSecondary` INTEGER NOT NULL, `dark-secondaryContainer` INTEGER NOT NULL, `dark-onSecondaryContainer` INTEGER NOT NULL, `dark-tertiary` INTEGER NOT NULL, `dark-onTertiary` INTEGER NOT NULL, `dark-tertiaryContainer` INTEGER NOT NULL, `dark-onTertiaryContainer` INTEGER NOT NULL, `dark-background` INTEGER NOT NULL, `dark-onBackground` INTEGER NOT NULL, `dark-surface` INTEGER NOT NULL, `dark-onSurface` INTEGER NOT NULL, `dark-surfaceVariant` INTEGER NOT NULL, `dark-onSurfaceVariant` INTEGER NOT NULL, `dark-surfaceTint` INTEGER NOT NULL, `dark-inverseSurface` INTEGER NOT NULL, `dark-inverseOnSurface` INTEGER NOT NULL, `dark-error` INTEGER NOT NULL, `dark-onError` INTEGER NOT NULL, `dark-errorContainer` INTEGER NOT NULL, `dark-onErrorContainer` INTEGER NOT NULL, `dark-outline` INTEGER NOT NULL, `light-extra-bars` INTEGER NOT NULL, `light-extra-onBars` INTEGER NOT NULL, `light-extra-isBarLight` INTEGER NOT NULL, `dark-extra-bars` INTEGER NOT NULL, `dark-extra-onBars` INTEGER NOT NULL, `dark-extra-isBarLight` INTEGER NOT NULL)")
        database.execSQL(
            """
                DROP TRIGGER IF EXISTS system_themes_deletion_trigger
            """.trimIndent()
        )
        database.execSQL("CREATE TABLE IF NOT EXISTS `reader_theme_table` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `backgroundColor` INTEGER NOT NULL, `onTextColor` INTEGER NOT NULL, `isDefault` INTEGER NOT NULL)")
    }
}
internal fun MIGRATION_26_27() = object : Migration(26, 27) {
    override fun migrate(database: SupportSQLiteDatabase) {

        //database.execSQL("CREATE TABLE IF NOT EXISTS `chapter` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `bookId` INTEGER NOT NULL, `key` TEXT NOT NULL, `name` TEXT NOT NULL, `read` INTEGER NOT NULL, `bookmark` INTEGER NOT NULL, `dateUpload` INTEGER NOT NULL, `dateFetch` INTEGER NOT NULL, `sourceOrder` INTEGER NOT NULL, `content` TEXT NOT NULL, `number` REAL NOT NULL, `translator` TEXT NOT NULL, FOREIGN KEY(`bookId`) REFERENCES `library`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")

    }
}

internal fun MIGRATION_23_24() = object : Migration(23, 24) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """CREATE TABLE IF NOT EXISTS `theme_table` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `isDefault` INTEGER NOT NULL, `light-primary` INTEGER NOT NULL, `light-onPrimary` INTEGER NOT NULL, `light-primaryContainer` INTEGER NOT NULL, `light-onPrimaryContainer` INTEGER NOT NULL, `light-inversePrimary` INTEGER NOT NULL, `light-secondary` INTEGER NOT NULL, `light-onSecondary` INTEGER NOT NULL, `light-secondaryContainer` INTEGER NOT NULL, `light-onSecondaryContainer` INTEGER NOT NULL, `light-tertiary` INTEGER NOT NULL, `light-onTertiary` INTEGER NOT NULL, `light-tertiaryContainer` INTEGER NOT NULL, `light-onTertiaryContainer` INTEGER NOT NULL, `light-background` INTEGER NOT NULL, `light-onBackground` INTEGER NOT NULL, `light-surface` INTEGER NOT NULL, `light-onSurface` INTEGER NOT NULL, `light-surfaceVariant` INTEGER NOT NULL, `light-onSurfaceVariant` INTEGER NOT NULL, `light-surfaceTint` INTEGER NOT NULL, `light-inverseSurface` INTEGER NOT NULL, `light-inverseOnSurface` INTEGER NOT NULL, `light-error` INTEGER NOT NULL, `light-onError` INTEGER NOT NULL, `light-errorContainer` INTEGER NOT NULL, `light-onErrorContainer` INTEGER NOT NULL, `light-outline` INTEGER NOT NULL, `dark-primary` INTEGER NOT NULL, `dark-onPrimary` INTEGER NOT NULL, `dark-primaryContainer` INTEGER NOT NULL, `dark-onPrimaryContainer` INTEGER NOT NULL, `dark-inversePrimary` INTEGER NOT NULL, `dark-secondary` INTEGER NOT NULL, `dark-onSecondary` INTEGER NOT NULL, `dark-secondaryContainer` INTEGER NOT NULL, `dark-onSecondaryContainer` INTEGER NOT NULL, `dark-tertiary` INTEGER NOT NULL, `dark-onTertiary` INTEGER NOT NULL, `dark-tertiaryContainer` INTEGER NOT NULL, `dark-onTertiaryContainer` INTEGER NOT NULL, `dark-background` INTEGER NOT NULL, `dark-onBackground` INTEGER NOT NULL, `dark-surface` INTEGER NOT NULL, `dark-onSurface` INTEGER NOT NULL, `dark-surfaceVariant` INTEGER NOT NULL, `dark-onSurfaceVariant` INTEGER NOT NULL, `dark-surfaceTint` INTEGER NOT NULL, `dark-inverseSurface` INTEGER NOT NULL, `dark-inverseOnSurface` INTEGER NOT NULL, `dark-error` INTEGER NOT NULL, `dark-onError` INTEGER NOT NULL, `dark-errorContainer` INTEGER NOT NULL, `dark-onErrorContainer` INTEGER NOT NULL, `dark-outline` INTEGER NOT NULL, `light-extra-bars` INTEGER NOT NULL, `light-extra-onBars` INTEGER NOT NULL, `light-extra-isBarLight` INTEGER NOT NULL, `dark-extra-bars` INTEGER NOT NULL, `dark-extra-onBars` INTEGER NOT NULL, `dark-extra-isBarLight` INTEGER NOT NULL)"""
        )

        database.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS system_themes_deletion_trigger BEFORE DELETE ON theme_table
            BEGIN SELECT CASE
              WHEN old.isDefault == 1 THEN
                RAISE(ABORT, 'System theme cant be deleted')
              END;
            END
            """.trimIndent()
        )
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
internal fun MIGRATION_25_26() = object : Migration(25, 26) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""ALTER TABLE `theme_table` ADD `light-outlineVariant` INTEGER NOT NULL DEFAULT 0""")
        database.execSQL("""ALTER TABLE `theme_table` ADD `light-scrim` INTEGER NOT NULL DEFAULT 0""")
        database.execSQL("""ALTER TABLE `theme_table` ADD `dark-outlineVariant` INTEGER NOT NULL DEFAULT 0""")
        database.execSQL("""ALTER TABLE `theme_table` ADD `dark-scrim` INTEGER NOT NULL DEFAULT 0""")
    }
}