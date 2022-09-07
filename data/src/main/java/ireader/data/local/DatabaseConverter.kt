package ireader.data.local

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ireader.core.api.source.model.Page

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


    @TypeConverter
    fun fromPage(values: List<Page>): String  {
        return Json.encodeToString(values)
    }
    @TypeConverter
    fun toPage(values: String): List<Page>  {
        return Json.decodeFromString(values)
    }


}
