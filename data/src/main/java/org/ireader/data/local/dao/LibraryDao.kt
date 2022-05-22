package org.ireader.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.DownloadedBook
import org.ireader.common_models.entities.LibraryBook

@Dao
interface LibraryDao : BaseDao<org.ireader.common_models.entities.Book> {

    @Query(
        """
SELECT library.id, library.sourceId, library.`key`, library.title, library.status, library.cover,
  library.lastUpdate, COUNT(chapter.id) AS unread, MAX(history.readAt) as lastRead
    FROM library
    LEFT JOIN chapter ON library.id = chapter.bookId AND chapter.read = 0
    LEFT JOIN history ON history.bookId == library.id
    GROUP BY library.id
    HAVING library.favorite = 1
    ORDER BY
    CASE
      WHEN :sort = 'title' THEN title
      WHEN :sort = 'lastRead' THEN lastRead
      WHEN :sort = 'lastUpdated' THEN lastUpdate
      WHEN :sort = 'unread' THEN unread
      WHEN :sort = 'dateAdded' THEN dataAdded
      WHEN :sort = 'dateFetched' THEN dateFetch
      WHEN :sort = 'source' THEN sourceId
    END,
    CASE
      WHEN :sort = 'titleDesc' THEN title
      WHEN :sort = 'lastReadDesc' THEN lastRead
      WHEN :sort = 'lastUpdatedDesc' THEN lastUpdate
      WHEN :sort = 'unreadDesc' THEN unread
      WHEN :sort = 'dateAddedDesc' THEN dataAdded
      WHEN :sort = 'dateFetchedDesc' THEN dateFetch
      WHEN :sort = 'sourceDesc' THEN sourceId
    END DESC,
    CASE
      WHEN :sort = 'source' THEN title
      WHEN :sort = 'sourceDesc' THEN title 
    END
        """
    )
    fun subscribeAll(sort: String): Flow<List<LibraryBook>>

    @Query(
        """
SELECT library.id, library.sourceId, library.`key`, library.title, library.status, library.cover,
  library.lastUpdate, COUNT(chapter.id) AS unread, MAX(history.readAt) as lastRead
    FROM library
    LEFT JOIN chapter ON library.id = chapter.bookId AND chapter.read = 0
    LEFT JOIN history ON history.bookId == library.id
    GROUP BY library.id
        HAVING library.favorite = 1
    ORDER BY
    CASE
      WHEN :sort = 'title' THEN title
      WHEN :sort = 'lastRead' THEN lastRead
      WHEN :sort = 'lastUpdated' THEN lastUpdate
      WHEN :sort = 'unread' THEN unread
      WHEN :sort = 'dateAdded' THEN dataAdded
      WHEN :sort = 'dateFetched' THEN dateFetch
      WHEN :sort = 'source' THEN sourceId
    END,
    CASE
      WHEN :sort = 'titleDesc' THEN title
      WHEN :sort = 'lastReadDesc' THEN lastRead
      WHEN :sort = 'lastUpdatedDesc' THEN lastUpdate
      WHEN :sort = 'unreadDesc' THEN unread
      WHEN :sort = 'dateAddedDesc' THEN dataAdded
      WHEN :sort = 'dateFetchedDesc' THEN dateFetch
      WHEN :sort = 'sourceDesc' THEN sourceId
    END DESC,
    CASE
      WHEN :sort = 'source' THEN title
      WHEN :sort = 'sourceDesc' THEN title END
        """
    )
    suspend fun findAll(sort: String): List<LibraryBook>

    @Query(
        """
        SELECT library.id, library.sourceId, library.'key', library.title, library.status, library.cover,
          library.lastUpdate, COUNT(chapter.id) AS unread, MAX(history.readAt) as lastRead
        FROM library
        LEFT JOIN chapter ON library.id = chapter.bookId AND chapter.read = 0
        LEFT JOIN history ON history.bookId == library.id
        WHERE NOT EXISTS
          (SELECT bookcategory.bookId FROM bookcategory WHERE library.id = bookcategory.bookId)
              AND library.favorite = 1
        GROUP BY library.id
        ORDER BY
    CASE
      WHEN :sort = 'title' THEN title
      WHEN :sort = 'lastRead' THEN lastRead
      WHEN :sort = 'lastUpdated' THEN lastUpdate
      WHEN :sort = 'unread' THEN unread
      WHEN :sort = 'dateAdded' THEN dataAdded
      WHEN :sort = 'dateFetched' THEN dateFetch
      WHEN :sort = 'source' THEN sourceId
    END,
    CASE
      WHEN :sort = 'titleDesc' THEN title
      WHEN :sort = 'lastReadDesc' THEN lastRead
      WHEN :sort = 'lastUpdatedDesc' THEN lastUpdate
      WHEN :sort = 'unreadDesc' THEN unread
      WHEN :sort = 'dateAddedDesc' THEN dataAdded
      WHEN :sort = 'dateFetchedDesc' THEN dateFetch
      WHEN :sort = 'sourceDesc' THEN sourceId
    END DESC,
        CASE
          WHEN :sort = 'source' THEN title
          WHEN :sort = 'sourceDesc' THEN title
        END;
        """
    )
    fun subscribeUncategorized(sort: String): Flow<List<LibraryBook>>

    @Query(
        """
        SELECT library.id, library.sourceId, library.'key', library.title, library.status, library.cover,
          library.lastUpdate, COUNT(chapter.id) AS unread, MAX(history.readAt) as lastRead
        FROM library
        LEFT JOIN chapter ON library.id = chapter.bookId AND chapter.read = 0
        LEFT JOIN history ON history.bookId == library.id
        WHERE NOT EXISTS
          (SELECT bookcategory.bookId FROM bookcategory WHERE library.id = bookcategory.bookId)
           AND    library.favorite = 1
        GROUP BY library.id
        ORDER BY
    CASE
      WHEN :sort = 'title' THEN title
      WHEN :sort = 'lastRead' THEN lastRead
      WHEN :sort = 'lastUpdated' THEN lastUpdate
      WHEN :sort = 'unread' THEN unread
      WHEN :sort = 'dateAdded' THEN dataAdded
      WHEN :sort = 'dateFetched' THEN dateFetch
      WHEN :sort = 'source' THEN sourceId
    END,
    CASE
      WHEN :sort = 'titleDesc' THEN title
      WHEN :sort = 'lastReadDesc' THEN lastRead
      WHEN :sort = 'lastUpdatedDesc' THEN lastUpdate
      WHEN :sort = 'unreadDesc' THEN unread
      WHEN :sort = 'dateAddedDesc' THEN dataAdded
      WHEN :sort = 'dateFetchedDesc' THEN dateFetch
      WHEN :sort = 'sourceDesc' THEN sourceId
    END DESC,
        CASE
          WHEN :sort = 'source' THEN title
          WHEN :sort = 'sourceDesc' THEN title
        END;
        """
    )
    suspend fun findUncategorized(sort: String): List<LibraryBook>

    @Query(
        """
        SELECT library.id, library.sourceId, library.'key', library.title, library.status, library.cover,
          library.lastUpdate, COUNT(chapter.id) AS total,
          SUM(CASE WHEN chapter.read == 0 THEN 1 ELSE 0 END) AS unread
        FROM library
        LEFT JOIN chapter ON library.id = chapter.bookId
        GROUP BY library.id
            HAVING library.favorite = 1
        ORDER BY
        CASE WHEN :sort == 'totalChapters' THEN total END, 
        CASE WHEN :sort == 'totalChaptersDesc' THEN total END DESC
        """
    )
    fun subscribeAllWithTotalChapters(sort: String): Flow<List<LibraryBook>>

    @Query(
        """
        SELECT library.id, library.sourceId, library.'key', library.title, library.status, library.cover,
          library.lastUpdate, COUNT(chapter.id) AS total,
          SUM(CASE WHEN chapter.read == 0 THEN 1 ELSE 0 END) AS unread
        FROM library
        LEFT JOIN chapter ON library.id = chapter.bookId
        
        GROUP BY library.id
            HAVING library.favorite = 1
        ORDER BY
        CASE WHEN :sort == 'totalChapters' THEN total END, 
        CASE WHEN :sort == 'totalChaptersDesc' THEN total END DESC
        """
    )
    suspend fun findAllWithTotalChapters(sort: String): List<LibraryBook>

    @Query(
        """
        SELECT library.id, library.sourceId, library.'key', library.title, library.status, library.cover,
          library.lastUpdate, COUNT(chapter.id) AS total,
          SUM(CASE WHEN chapter.read == 0 THEN 1 ELSE 0 END) AS unread
        FROM library
        LEFT JOIN chapter ON library.id = chapter.bookId
        WHERE NOT EXISTS
          (SELECT bookcategory.bookId FROM bookcategory WHERE library.id = bookcategory.bookId)     AND library.favorite = 1
        GROUP BY library.id
        ORDER BY total
        """
    )
    suspend fun findUncategorizedWithTotalChapters(): List<LibraryBook>

    @Query(
        """
        SELECT library.id, library.sourceId, library.'key', library.title, library.status, library.cover,
          library.lastUpdate, COUNT(chapter.id) AS total,
          SUM(CASE WHEN chapter.read == 0 THEN 1 ELSE 0 END) AS unread
        FROM library
        LEFT JOIN chapter ON library.id = chapter.bookId
        WHERE NOT EXISTS
          (SELECT bookcategory.bookId FROM bookcategory WHERE library.id = bookcategory.bookId)   AND library.favorite = 1
        GROUP BY library.id
        ORDER BY total
        """
    )
    suspend fun subscribeUncategorizedWithTotalChapters(): List<LibraryBook>

    @Query(
        """
             SELECT library.id, library.sourceId, library.'key', library.title, library.status, library.cover, library.customCover, library.favorite,
          library.lastUpdate, COUNT(chapter.id) AS unread, MAX(history.readAt) as lastRead
        FROM bookcategory
        INNER JOIN library ON bookcategory.bookId = library.id
        LEFT JOIN chapter ON library.id = chapter.bookId AND chapter.read = 0
            LEFT JOIN history ON history.bookId == library.id
        WHERE bookcategory.categoryId = :categoryId AND library.favorite = 1
        GROUP BY library.id
        ORDER BY
    CASE
      WHEN :sort = 'title' THEN title
      WHEN :sort = 'lastRead' THEN lastRead
      WHEN :sort = 'lastUpdated' THEN lastUpdate
      WHEN :sort = 'unread' THEN unread
      WHEN :sort = 'dateAdded' THEN dataAdded
      WHEN :sort = 'dateFetched' THEN dateFetch
      WHEN :sort = 'source' THEN sourceId
    END,
    CASE
      WHEN :sort = 'titleDesc' THEN title
      WHEN :sort = 'lastReadDesc' THEN lastRead
      WHEN :sort = 'lastUpdatedDesc' THEN lastUpdate
      WHEN :sort = 'unreadDesc' THEN unread
      WHEN :sort = 'dateAddedDesc' THEN dataAdded
      WHEN :sort = 'dateFetchedDesc' THEN dateFetch
      WHEN :sort = 'sourceDesc' THEN sourceId
    END DESC,
        CASE
          WHEN :sort = 'source' THEN title
          WHEN :sort = 'sourceDesc' THEN title
        END
        """
    )
    fun subscribeAllInCategory(sort: String, categoryId: Long): Flow<List<LibraryBook>>

    @Query(
        """
             SELECT library.id, library.sourceId, library.'key', library.title, library.status, library.cover, library.customCover, library.favorite,
          library.lastUpdate, COUNT(chapter.id) AS unread, MAX(history.readAt) as lastRead
        FROM bookcategory
        INNER JOIN library ON bookcategory.bookId = library.id
        LEFT JOIN chapter ON library.id = chapter.bookId AND chapter.read = 0
            LEFT JOIN history ON history.bookId == library.id
        WHERE bookcategory.categoryId = :categoryId AND library.favorite = 1
        GROUP BY library.id
        ORDER BY
    CASE
      WHEN :sort = 'title' THEN title
      WHEN :sort = 'lastRead' THEN lastRead
      WHEN :sort = 'lastUpdated' THEN lastUpdate
      WHEN :sort = 'unread' THEN unread
      WHEN :sort = 'dateAdded' THEN dataAdded
      WHEN :sort = 'dateFetched' THEN dateFetch
      WHEN :sort = 'source' THEN sourceId
    END,
    CASE
      WHEN :sort = 'titleDesc' THEN title
      WHEN :sort = 'lastReadDesc' THEN lastRead
      WHEN :sort = 'lastUpdatedDesc' THEN lastUpdate
      WHEN :sort = 'unreadDesc' THEN unread
      WHEN :sort = 'dateAddedDesc' THEN dataAdded
      WHEN :sort = 'dateFetchedDesc' THEN dateFetch
      WHEN :sort = 'sourceDesc' THEN sourceId
    END DESC,
        CASE
          WHEN :sort = 'source' THEN title
          WHEN :sort = 'sourceDesc' THEN title
        END
        """
    )
    suspend fun findAllInCategory(sort: String, categoryId: Long): List<LibraryBook>

    @Query(
        """
        SELECT library.id, library.sourceId, library.'key', library.title, library.status, library.cover, library.customCover, library.favorite,
          library.lastUpdate, COUNT(chapter.id) AS total,
          SUM(CASE WHEN chapter.read == 0 THEN 1 ELSE 0 END) AS unread
        FROM bookcategory
        INNER JOIN library ON bookcategory.bookId = library.id
        LEFT JOIN chapter ON library.id = chapter.bookId
        WHERE bookcategory.categoryId = :categoryId AND library.favorite = 1
        GROUP BY library.id
        ORDER BY total;
        """
    )
    fun subscribeAllInCategoryWithTotalChapters(categoryId: Long): Flow<List<LibraryBook>>

    @Query(
        """
        SELECT library.id, library.sourceId, library.'key', library.title, library.status, library.cover, library.customCover, library.favorite,
          library.lastUpdate, COUNT(chapter.id) AS total,
          SUM(CASE WHEN chapter.read == 0 THEN 1 ELSE 0 END) AS unread
        FROM bookcategory
        INNER JOIN library ON bookcategory.bookId = library.id
        LEFT JOIN chapter ON library.id = chapter.bookId
        WHERE bookcategory.categoryId = :categoryId AND library.favorite = 1
        GROUP BY library.id
        ORDER BY total;
        """
    )
    fun findAllInCategoryWithTotalChapters(categoryId: Long): Flow<List<LibraryBook>>


    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT library.id,COUNT(chapter.id) as totalChapters,SUM(chapter.content > 0) as totalDownloadedChapter
        FROM library
        LEFT JOIN chapter ON library.id = chapter.bookId
        GROUP BY library.id
        HAVING totalChapters == totalDownloadedChapter AND library.favorite = 1
        """
    )
    fun findDownloadedBooks(): List<DownloadedBook>
}