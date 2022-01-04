package ir.kazemcodes.infinity.data.local

import androidx.room.TypeConverter


class DatabaseConverter {

    @TypeConverter
    fun fromString(stringListString: String): List<String> {
        return stringListString.split(",").map { it }
    }

    @TypeConverter
    fun toString(stringList: List<String>): String {
        return stringList.joinToString(separator = ",")
    }

//    @TypeConverter
//    fun fromList(value : List<String>) = Json.encodeToString(value)
//
//    @TypeConverter
//    fun toList(value: String) = Json.decodeFromString<List<String>>(value)
}
