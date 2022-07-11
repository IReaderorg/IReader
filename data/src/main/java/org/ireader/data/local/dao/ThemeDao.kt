package org.ireader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.ireader.common_models.theme.CustomTheme
import org.ireader.common_models.theme.ReaderTheme

@Dao
interface ThemeDao :BaseDao<CustomTheme> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertThemes(items: List<CustomTheme>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTheme(item: CustomTheme) : Long

    @Query("SELECT * FROM theme_table")
    fun subscribe() : kotlinx.coroutines.flow.Flow<List<CustomTheme>>

    @Query("SELECT * FROM theme_table WHERE id == :themeId")
    fun find(themeId:Int) : kotlinx.coroutines.flow.Flow<CustomTheme>

    @Query("DELETE FROM theme_table WHERE isDefault = 0")
    fun deleteAll()

}

@Dao
interface ReaderThemeDao :BaseDao<ReaderTheme> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertThemes(items: List<ReaderTheme>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTheme(item: ReaderTheme) : Long

    @Query("SELECT * FROM reader_theme_table")
    fun subscribe() : kotlinx.coroutines.flow.Flow<List<ReaderTheme>>

    @Query("SELECT * FROM reader_theme_table WHERE id == :themeId")
    fun find(themeId:Int) : kotlinx.coroutines.flow.Flow<ReaderTheme>

    @Query("DELETE FROM reader_theme_table WHERE isDefault = 0")
    fun deleteAll()

}