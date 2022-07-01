package org.ireader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.ireader.common_models.theme.CustomTheme

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

}