package ir.kazemcodes.infinity.library_feature.data

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.library_feature.domain.util.JsonParser

@ProvidedTypeConverter
class Converters(
    private val jsonParser: JsonParser
) {
    @TypeConverter
    fun fromBooksJson(json: String): List<Book> {
        return jsonParser.fromJson<ArrayList<Book>>(
            json,
            object : TypeToken<ArrayList<Book>>(){}.type
        ) ?: emptyList()
    }

    @TypeConverter
    fun toBooksJson(meanings: List<Book>): String {
        return jsonParser.toJson(
            meanings,
            object : TypeToken<ArrayList<Book>>(){}.type
        ) ?: "[]"
    }
}