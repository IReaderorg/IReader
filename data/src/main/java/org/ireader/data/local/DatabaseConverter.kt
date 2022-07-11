package org.ireader.data.local

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.TypeConverter

class DatabaseConverter {

    @TypeConverter
    fun fromString(stringListString: String): List<String> {
        return stringListString.split("$%&$").map { it }
    }

    @TypeConverter
    fun toString(stringList: List<String>): String {
        return stringList.joinToString(separator = "$%&$")
    }

    @TypeConverter
    fun fromColorType(value: Color): Int = value.toArgb()

    @TypeConverter
    fun toColorType(value: Int): Color = Color(value)
}
