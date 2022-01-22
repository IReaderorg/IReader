package ir.kazemcodes.infinity.core.data.local

import androidx.room.TypeConverter


class DatabaseConverter {

    /**
     * i used the $%&$ as separator instead of ',', because it convertor
     * detect the separator incorrectly
     */
    @TypeConverter
    fun fromString(stringListString: String): List<String> {
        return stringListString.split("$%&$").map { it }
    }

    @TypeConverter
    fun toString(stringList: List<String>): String {
        return stringList.joinToString(separator = "$%&$")
    }


}
